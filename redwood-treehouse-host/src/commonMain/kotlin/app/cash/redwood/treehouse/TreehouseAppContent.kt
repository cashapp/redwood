/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.treehouse

import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.host.HostProtocolAdapter
import app.cash.redwood.protocol.host.ProtocolFactory
import app.cash.redwood.protocol.host.UiEvent
import app.cash.redwood.protocol.host.UiEventSink
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.Widget
import app.cash.zipline.ZiplineApiMismatchException
import app.cash.zipline.ZiplineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json

private class State<A : AppService>(
  val viewState: ViewState,
  val codeState: CodeState<A>,
) {
  init {
    require(viewState != ViewState.None || codeState !is CodeState.Running)
  }
}

private sealed interface ViewState {
  object None : ViewState

  data class Preloading(
    val onBackPressedDispatcher: OnBackPressedDispatcher,
    val uiConfiguration: StateFlow<UiConfiguration>,
  ) : ViewState

  data class Bound(
    val view: TreehouseView<*>,
  ) : ViewState
}

private sealed interface CodeState<A : AppService> {
  class Idle<A : AppService>(
    val isInitialLaunch: Boolean,
  ) : CodeState<A>

  class Running<A : AppService>(
    val viewContentCodeBinding: ViewContentCodeBinding<A>,
    val changesAwaitingInitViewSize: Int = 0,
    val deliveredChangeCount: Int = 0,
  ) : CodeState<A> {
    val changeCount: Int
      get() = changesAwaitingInitViewSize + deliveredChangeCount
  }
}

internal class TreehouseAppContent<A : AppService>(
  private val codeHost: CodeHost<A>,
  private val dispatchers: TreehouseDispatchers,
  private val codeEventPublisher: CodeEventPublisher,
  private val source: TreehouseContentSource<A>,
  private val leakDetector: LeakDetector,
) : Content,
  CodeHost.Listener<A>,
  CodeSession.Listener<A> {
  private val stateFlow = MutableStateFlow<State<A>>(
    State(ViewState.None, CodeState.Idle(isInitialLaunch = true)),
  )

  override fun preload(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    uiConfiguration: StateFlow<UiConfiguration>,
  ) {
    dispatchers.checkUi()
    val previousState = stateFlow.value

    if (previousState.viewState == ViewState.Preloading(onBackPressedDispatcher, uiConfiguration)) return // Idempotent.
    check(previousState.viewState is ViewState.None)

    val nextViewState = ViewState.Preloading(onBackPressedDispatcher, uiConfiguration)

    // Start the code if necessary.
    val codeSession = codeHost.codeSession
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && codeSession != null -> {
        CodeState.Running(
          startViewCodeContentBinding(
            codeSession = codeSession,
            isInitialLaunch = true,
            onBackPressedDispatcher = onBackPressedDispatcher,
            firstUiConfiguration = uiConfiguration,
          ),
        )
      }

      else -> previousState.codeState
    }

    // Ask to get notified when code is ready.
    codeHost.addListener(this)

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  override fun bind(view: TreehouseView<*>) {
    dispatchers.checkUi()

    if (stateFlow.value.viewState == ViewState.Bound(view)) return // Idempotent.

    preload(view.onBackPressedDispatcher, view.uiConfiguration)

    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    check(previousViewState is ViewState.Preloading)

    val nextViewState = ViewState.Bound(view)
    val nextCodeState = previousState.codeState

    // Make sure we're showing something in the view; either loaded code or a spinner to show that
    // code is coming.
    when (nextCodeState) {
      is CodeState.Idle -> codeEventPublisher.onInitialCodeLoading(view)
      is CodeState.Running -> nextCodeState.viewContentCodeBinding.initView(view)
    }

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  override suspend fun awaitContent(untilChangeCount: Int) {
    stateFlow.first {
      if (it.viewState is ViewState.None) {
        throw CancellationException("unbound while awaiting content")
      }

      it.codeState is CodeState.Running && it.codeState.changeCount >= untilChangeCount
    }
  }

  override fun unbind() {
    dispatchers.checkUi()

    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    if (previousViewState is ViewState.None) return // Idempotent.

    val nextViewState = ViewState.None
    val nextCodeState = CodeState.Idle<A>(isInitialLaunch = true)

    // Cancel the code if necessary.
    codeHost.removeListener(this)
    if (previousState.codeState is CodeState.Running) {
      val binding = previousState.codeState.viewContentCodeBinding
      binding.cancel(null)
      binding.codeSession.removeListener(this)
    }

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  override fun codeSessionChanged(next: CodeSession<A>) {
    dispatchers.checkUi()

    val previousState = stateFlow.value
    val viewState = previousState.viewState
    val previousCodeState = previousState.codeState

    val onBackPressedDispatcher = when (viewState) {
      is ViewState.Preloading -> viewState.onBackPressedDispatcher
      is ViewState.Bound -> viewState.view.onBackPressedDispatcher
      else -> error("unexpected receiveCodeSession with no view bound and no preload")
    }

    val uiConfiguration = when (viewState) {
      is ViewState.Preloading -> viewState.uiConfiguration
      is ViewState.Bound -> viewState.view.uiConfiguration
      else -> error("unexpected receiveCodeSession with no view bound and no preload")
    }

    val nextCodeState = CodeState.Running(
      startViewCodeContentBinding(
        codeSession = next,
        isInitialLaunch = (previousCodeState as? CodeState.Idle)?.isInitialLaunch == true,
        onBackPressedDispatcher = onBackPressedDispatcher,
        firstUiConfiguration = uiConfiguration,
      ),
    )

    // If we have a view, tell the new binding about it.
    if (viewState is ViewState.Bound) {
      nextCodeState.viewContentCodeBinding.initView(viewState.view)
    }

    // If we replaced an old binding, cancel that old binding.
    if (previousCodeState is CodeState.Running) {
      val binding = previousCodeState.viewContentCodeBinding
      binding.cancel(null)
      binding.codeSession.removeListener(this)
    }

    stateFlow.value = State(viewState, nextCodeState)
  }

  override fun onUncaughtException(codeSession: CodeSession<A>, exception: Throwable) {
    codeSessionStopped(exception = exception)
  }

  override fun onStop(codeSession: CodeSession<A>) {
    codeSessionStopped(exception = null)
  }

  /**
   * If the code crashes or is unloaded, show an error on the UI and cancel the UI binding. This
   * sets the code state back to idle.
   */
  private fun codeSessionStopped(exception: Throwable?) {
    dispatchers.checkUi()

    val previousState = stateFlow.value
    val viewState = previousState.viewState
    val previousCodeState = previousState.codeState

    // This listener should only fire if we're actively running code.
    require(previousCodeState is CodeState.Running)

    // Cancel the UI binding to the canceled code.
    val binding = previousCodeState.viewContentCodeBinding
    binding.cancel(exception)
    binding.codeSession.removeListener(this)

    val nextCodeState = CodeState.Idle<A>(isInitialLaunch = false)
    stateFlow.value = State(viewState, nextCodeState)
  }

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  private fun startViewCodeContentBinding(
    codeSession: CodeSession<A>,
    isInitialLaunch: Boolean,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    firstUiConfiguration: StateFlow<UiConfiguration>,
  ): ViewContentCodeBinding<A> {
    dispatchers.checkUi()
    codeSession.addListener(this)

    return ViewContentCodeBinding(
      stateStore = codeHost.stateStore,
      dispatchers = dispatchers,
      eventPublisher = codeSession.eventPublisher,
      contentSource = source,
      codeEventPublisher = codeEventPublisher,
      stateFlow = stateFlow,
      codeSession = codeSession,
      isInitialLaunch = isInitialLaunch,
      onBackPressedDispatcher = onBackPressedDispatcher,
      firstUiConfiguration = firstUiConfiguration,
      leakDetector = leakDetector,
    ).apply {
      start()
    }
  }
}

/**
 * Connects a [TreehouseView], a [TreehouseContentSource], and a [CodeSession].
 *
 * The TreehouseView may not be known immediately, as in [Content.preload].
 *
 * Canceled by [TreehouseAppContent] if the view is unbound from its content, or if the code is
 * updated.
 *
 * This aggressively manages the lifecycle of the widget, breaking widget reachability when the
 * binding is canceled. It uses a single [ZiplineScope] for all Zipline services consumed by this
 * binding.
 */
private class ViewContentCodeBinding<A : AppService>(
  val stateStore: StateStore,
  val dispatchers: TreehouseDispatchers,
  val eventPublisher: EventPublisher,
  contentSource: TreehouseContentSource<A>,
  val codeEventPublisher: CodeEventPublisher,
  val stateFlow: MutableStateFlow<State<A>>,
  val codeSession: CodeSession<A>,
  private val isInitialLaunch: Boolean,
  private val onBackPressedDispatcher: OnBackPressedDispatcher,
  firstUiConfiguration: StateFlow<UiConfiguration>,
  private val leakDetector: LeakDetector,
) : ChangesSinkService,
  TreehouseView.SaveCallback,
  ZiplineTreehouseUi.Host {
  private val uiConfigurationFlow = SequentialStateFlow(firstUiConfiguration)

  private val bindingScope = CoroutineScope(
    codeSession.scope.coroutineContext + SupervisorJob(codeSession.scope.coroutineContext.job),
  )

  /** Only accessed on [TreehouseDispatchers.ui]. Null before [initView] and after [cancel]. */
  private var viewOrNull: TreehouseView<*>? = null

  /**
   * Only accessed on [TreehouseDispatchers.ui].
   * Null before [initView]+[receiveChangesOnUiDispatcher] and after [cancel].
   */
  private var hostAdapterOrNull: HostProtocolAdapter<*>? = null

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private val serviceScope = codeSession.newServiceScope()

  /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
  private var contentSource: TreehouseContentSource<A>? = contentSource

  /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
  private var treehouseUiOrNull: ZiplineTreehouseUi? = null

  /** Note that this is necessary to break the retain cycle between host and guest. */
  private val eventBridge = EventBridge(codeSession.json, dispatchers.zipline, bindingScope)

  /** Only accessed on [TreehouseDispatchers.ui]. Empty after [initView]. */
  private val changesAwaitingInitView = ArrayDeque<List<Change>>()

  /** Changes applied to the UI. Only accessed on [TreehouseDispatchers.ui]. */
  private var deliveredChangeCount = 0

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var canceled = false

  private var initViewCalled: Boolean = false

  /** The state to restore. Initialized in [start]. */
  override var stateSnapshot: StateSnapshot? = null

  override val uiConfigurations: StateFlow<UiConfiguration>
    get() = uiConfigurationFlow

  fun initView(view: TreehouseView<*>) {
    dispatchers.checkUi()

    require(!initViewCalled)
    initViewCalled = true

    if (canceled) return

    viewOrNull = view

    view.saveCallback = this

    // Apply all the changes received before we had a view to apply them to.
    while (true) {
      val changes = changesAwaitingInitView.removeFirstOrNull() ?: break
      receiveChangesOnUiDispatcher(changes)
    }
  }

  /** Send changes from Zipline to the UI. */
  override fun sendChanges(changes: List<Change>) {
    // Receive UI updates on the UI dispatcher.
    bindingScope.launch(dispatchers.ui) {
      receiveChangesOnUiDispatcher(changes)
    }
  }

  private fun receiveChangesOnUiDispatcher(changes: List<Change>) {
    if (canceled) {
      return
    }

    val view = viewOrNull
    if (view == null) {
      changesAwaitingInitView += changes
      updateChangeCount()
      return
    }

    var hostAdapter = hostAdapterOrNull
    if (hostAdapter == null) {
      @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
      hostAdapter = HostProtocolAdapter(
        guestVersion = codeSession.guestProtocolVersion,
        container = view.children as Widget.Children<Any>,
        factory = view.widgetSystem.widgetFactory(
          json = codeSession.json,
          protocolMismatchHandler = eventPublisher.widgetProtocolMismatchHandler,
        ) as ProtocolFactory<Any>,
        eventSink = eventBridge,
        leakDetector = leakDetector,
      )
      hostAdapterOrNull = hostAdapter
    }

    if (deliveredChangeCount++ == 0) {
      view.reset()
      codeEventPublisher.onCodeLoaded(view, isInitialLaunch)
    }
    updateChangeCount()

    hostAdapter.sendChanges(changes)
  }

  /** Unblock coroutines suspended on TreehouseAppContent.awaitContent(). */
  private fun updateChangeCount() {
    val state = stateFlow.value
    val codeState = state.codeState as? CodeState.Running ?: return

    // Don't mutate state if this binding is out of date.
    if (codeState.viewContentCodeBinding != this) return

    stateFlow.value = State(
      state.viewState,
      CodeState.Running(
        viewContentCodeBinding = this,
        changesAwaitingInitViewSize = changesAwaitingInitView.size,
        deliveredChangeCount = deliveredChangeCount,
      ),
    )
  }

  fun start() {
    bindingScope.launch(dispatchers.zipline) {
      val scopedAppService = serviceScope.apply(codeSession.appService)
      val treehouseUi = contentSource!!.get(scopedAppService)
      treehouseUiOrNull = treehouseUi
      eventBridge.delegate = treehouseUi
      stateSnapshot = viewOrNull?.stateSnapshotId?.let {
        stateStore.get(it.value.orEmpty())
      }
      try {
        treehouseUi.start(this@ViewContentCodeBinding)
      } catch (e: ZiplineApiMismatchException) {
        // Fall back to calling the function that doesn't have a back pressed dispatcher.
        treehouseUi.start(
          changesSink = this@ViewContentCodeBinding,
          uiConfigurations = uiConfigurationFlow,
          stateSnapshot = stateSnapshot,
        )
      }
    }
  }

  override fun addOnBackPressedCallback(
    onBackPressedCallbackService: OnBackPressedCallbackService,
  ): CancellableService {
    dispatchers.checkZipline()
    val cancellableJob = bindingScope.launch(dispatchers.zipline) {
      val onBackPressedCallback = object : OnBackPressedCallback(onBackPressedCallbackService.isEnabled.value) {
        override fun handleOnBackPressed() {
          bindingScope.launch(dispatchers.zipline) {
            onBackPressedCallbackService.handleOnBackPressed()
          }
        }
      }
      val cancellable = onBackPressedDispatcher.addCallback(onBackPressedCallback)
      launch {
        onBackPressedCallbackService.isEnabled.collect {
          onBackPressedCallback.isEnabled = it
        }
      }
      suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancellable.cancel() }
      }
    }

    return object : CancellableService {
      override fun cancel() {
        dispatchers.checkZipline()
        cancellableJob.cancel()
      }

      override fun close() {
        cancel()
      }
    }
  }

  override fun performSave(id: String) {
    bindingScope.launch(dispatchers.zipline) {
      val state = treehouseUiOrNull?.snapshotState() ?: return@launch
      stateStore.put(id, state)
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  fun cancel(exception: Throwable?) {
    dispatchers.checkUi()

    if (canceled) return
    canceled = true

    viewOrNull?.let { view ->
      codeEventPublisher.onCodeDetached(view, exception)
      view.saveCallback = null
    }
    viewOrNull = null
    hostAdapterOrNull?.close()
    hostAdapterOrNull = null
    eventBridge.bindingScope = null
    eventBridge.ziplineDispatcher = null
    bindingScope.launch(dispatchers.zipline, start = CoroutineStart.ATOMIC) {
      contentSource = null
      treehouseUiOrNull = null
      eventBridge.delegate = null
      serviceScope.close()
      bindingScope.cancel()
    }
  }
}

/**
 * Bridge events from the UI dispatcher to the Zipline dispatcher.
 *
 * Event sinks are in a natural retain cycle between the host and guest. We prevent unwanted
 * retain cycles by breaking the link to the delegate when the binding is canceled. This avoids
 * problems when mixing garbage-collected Kotlin objects with reference-counted Swift objects.
 */
private class EventBridge(
  private val json: Json,
  // Both properties are only accessed on the UI dispatcher and null after cancel().
  var ziplineDispatcher: CoroutineDispatcher?,
  var bindingScope: CoroutineScope?,
) : UiEventSink {
  // Only accessed on the Zipline dispatcher and null after cancel().
  var delegate: EventSink? = null

  /** Send an event from the UI to Zipline. */
  override fun sendEvent(uiEvent: UiEvent) {
    // Send UI events on the zipline dispatcher.
    val dispatcher = this.ziplineDispatcher ?: return
    val bindingScope = this.bindingScope ?: return
    bindingScope.launch(dispatcher) {
      // Perform initial serialization of event arguments into JSON model after the thread hop.
      val event = uiEvent.toProtocol(json)

      delegate?.sendEvent(event)
    }
  }
}
