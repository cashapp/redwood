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
import app.cash.redwood.treehouse.Content.State
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.Widget
import app.cash.zipline.ZiplineApiMismatchException
import app.cash.zipline.ZiplineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json

private class InternalState<A : AppService>(
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
  val loadCount: Int
  val lastUncaughtException: Throwable?

  class Idle<A : AppService>(
    override val loadCount: Int,
    override val lastUncaughtException: Throwable?,
  ) : CodeState<A>

  class Running<A : AppService>(
    override val loadCount: Int,
    override val lastUncaughtException: Throwable?,
    val viewContentCodeBinding: ViewContentCodeBinding<A>,
    val changesAwaitingInitViewSize: Int = 0,
    val deliveredChangeCount: Int = 0,
  ) : CodeState<A>

  fun asState() = State(
    attached = this is Running,
    loadCount = loadCount,
    deliveredChangeCount = (this as? Running)?.deliveredChangeCount ?: 0,
    uncaughtException = lastUncaughtException,
  )
}

internal class TreehouseAppContent<A : AppService>(
  private val codeHost: CodeHost<A>,
  private val dispatchers: TreehouseDispatchers,
  private val source: TreehouseContentSource<A>,
  private val leakDetector: LeakDetector,
) : Content,
  CodeHost.Listener<A>,
  CodeSession.Listener<A> {
  private val internalStateFlow = MutableStateFlow<InternalState<A>>(
    InternalState(ViewState.None, CodeState.Idle(0, null)),
  )

  private val externalStateFlow = MutableStateFlow(
    State(
      loadCount = 0,
      attached = false,
      deliveredChangeCount = 0,
    ),
  )

  override val state: StateFlow<State>
    get() = externalStateFlow

  override fun preload(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    uiConfiguration: StateFlow<UiConfiguration>,
  ) {
    dispatchers.checkUi()
    val previousState = internalStateFlow.value

    if (previousState.viewState == ViewState.Preloading(onBackPressedDispatcher, uiConfiguration)) {
      return // Idempotent.
    }

    check(previousState.viewState is ViewState.None)

    val nextViewState = ViewState.Preloading(onBackPressedDispatcher, uiConfiguration)

    // Start the code if necessary.
    val codeSession = codeHost.codeSession
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && codeSession != null -> {
        val newLoadCount = previousState.codeState.loadCount + 1
        CodeState.Running(
          loadCount = newLoadCount,
          lastUncaughtException = previousState.codeState.lastUncaughtException,
          viewContentCodeBinding = startViewCodeContentBinding(
            codeSession = codeSession,
            onBackPressedDispatcher = onBackPressedDispatcher,
            firstUiConfiguration = uiConfiguration,
          ),
        )
      }

      else -> previousState.codeState
    }

    // Ask to get notified when code is ready.
    codeHost.addListener(this)

    internalStateFlow.value = InternalState(nextViewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
  }

  override fun bind(view: TreehouseView<*>) {
    dispatchers.checkUi()

    if (internalStateFlow.value.viewState == ViewState.Bound(view)) return // Idempotent.

    preload(view.onBackPressedDispatcher, view.uiConfiguration)

    val previousState = internalStateFlow.value
    val previousViewState = previousState.viewState
    val previousCodeState = previousState.codeState

    check(previousViewState is ViewState.Preloading)

    val nextViewState = ViewState.Bound(view)

    // Make sure we're showing something in the view; either loaded code or a spinner to show that
    // code is coming.
    when (previousCodeState) {
      is CodeState.Idle -> view.showLoading()
      is CodeState.Running -> {
        previousCodeState.viewContentCodeBinding.initView(view, mustUpdateView = true)
      }
    }

    val nextCodeState = internalStateFlow.value.codeState
    internalStateFlow.value = InternalState(nextViewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
  }

  override fun unbind() {
    dispatchers.checkUi()

    val previousState = internalStateFlow.value
    val previousViewState = previousState.viewState
    val previousCodeState = previousState.codeState

    if (previousViewState is ViewState.None) return // Idempotent.

    val nextViewState = ViewState.None
    val nextCodeState = CodeState.Idle<A>(
      loadCount = previousCodeState.loadCount,
      lastUncaughtException = previousCodeState.lastUncaughtException,
    )

    // Cancel the code if necessary.
    codeHost.removeListener(this)
    if (previousCodeState is CodeState.Running) {
      val binding = previousCodeState.viewContentCodeBinding
      binding.cancel(null)
      binding.codeSession.removeListener(this)
    }

    internalStateFlow.value = InternalState(nextViewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
  }

  override fun codeSessionChanged(next: CodeSession<A>) {
    dispatchers.checkUi()

    val previousState = internalStateFlow.value
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

    val newLoadCount = previousCodeState.loadCount + 1
    val nextCodeState = CodeState.Running(
      loadCount = newLoadCount,
      lastUncaughtException = previousState.codeState.lastUncaughtException,
      viewContentCodeBinding = startViewCodeContentBinding(
        codeSession = next,
        onBackPressedDispatcher = onBackPressedDispatcher,
        firstUiConfiguration = uiConfiguration,
      ),
    )

    // If we have a view, tell the new binding about it.
    if (viewState is ViewState.Bound) {
      nextCodeState.viewContentCodeBinding.initView(viewState.view, mustUpdateView = false)
    }

    // If we replaced an old binding, cancel that old binding.
    if (previousCodeState is CodeState.Running) {
      val binding = previousCodeState.viewContentCodeBinding
      binding.cancel(null)
      binding.codeSession.removeListener(this)
    }

    internalStateFlow.value = InternalState(viewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
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

    val previousState = internalStateFlow.value
    val viewState = previousState.viewState
    val previousCodeState = previousState.codeState

    // This listener should only fire if we're actively running code.
    require(previousCodeState is CodeState.Running)

    // Cancel the UI binding to the canceled code.
    val binding = previousCodeState.viewContentCodeBinding
    binding.cancel(exception)
    binding.codeSession.removeListener(this)

    val nextCodeState = CodeState.Idle<A>(
      loadCount = previousCodeState.loadCount,
      lastUncaughtException = exception,
    )
    internalStateFlow.value = InternalState(viewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
  }

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  private fun startViewCodeContentBinding(
    codeSession: CodeSession<A>,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    firstUiConfiguration: StateFlow<UiConfiguration>,
  ): ViewContentCodeBinding<A> {
    dispatchers.checkUi()
    codeSession.addListener(this)

    return ViewContentCodeBinding(
      codeHost = codeHost,
      dispatchers = dispatchers,
      eventPublisher = codeSession.eventPublisher,
      contentSource = source,
      internalStateFlow = internalStateFlow,
      externalStateFlow = externalStateFlow,
      codeSession = codeSession,
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
  codeHost: CodeHost<A>,
  val dispatchers: TreehouseDispatchers,
  val eventPublisher: EventPublisher,
  contentSource: TreehouseContentSource<A>,
  val internalStateFlow: MutableStateFlow<InternalState<A>>,
  val externalStateFlow: MutableStateFlow<State>,
  val codeSession: CodeSession<A>,
  private val onBackPressedDispatcher: OnBackPressedDispatcher,
  firstUiConfiguration: StateFlow<UiConfiguration>,
  private val leakDetector: LeakDetector,
) : ChangesSinkService,
  TreehouseView.SaveCallback,
  ZiplineTreehouseUi.Host {

  /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
  private var codeHostOrNull: CodeHost<A>? = codeHost

  private val stateStore: StateStore = codeHost.stateStore

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

  fun initView(
    view: TreehouseView<*>,
    mustUpdateView: Boolean,
  ) {
    dispatchers.checkUi()

    require(!initViewCalled)
    initViewCalled = true

    if (canceled) return

    viewOrNull = view

    view.saveCallback = this

    // Apply all the changes received before we had a view to apply them to.
    var hasChanges = false
    while (true) {
      val changes = changesAwaitingInitView.removeFirstOrNull() ?: break
      hasChanges = true
      receiveChangesOnUiDispatcher(changes)
    }

    if (mustUpdateView && !hasChanges) {
      view.showLoading()
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

    // If this is the first change, clear the previous content. This could be the previous children,
    // a Loading widget, or a Crashed widget.
    if (deliveredChangeCount++ == 0) {
      view.children.remove(0, view.children.widgets.size)
    }
    updateChangeCount()

    hostAdapter.sendChanges(changes)
  }

  /** Unblock coroutines suspended on TreehouseAppContent.awaitContent(). */
  private fun updateChangeCount() {
    val state = internalStateFlow.value
    val codeState = state.codeState as? CodeState.Running ?: return

    // Don't mutate state if this binding is out of date.
    if (codeState.viewContentCodeBinding != this) return

    // Clear the previous run's uncaught exception when content is ready.
    val lastUncaughtException = when {
      deliveredChangeCount > 0 -> null
      else -> codeState.lastUncaughtException
    }

    val nextCodeState = CodeState.Running(
      loadCount = codeState.loadCount,
      lastUncaughtException = lastUncaughtException,
      viewContentCodeBinding = this,
      changesAwaitingInitViewSize = changesAwaitingInitView.size,
      deliveredChangeCount = deliveredChangeCount,
    )
    internalStateFlow.value = InternalState(state.viewState, nextCodeState)
    externalStateFlow.value = nextCodeState.asState()
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

    hostAdapterOrNull?.close()
    hostAdapterOrNull = null
    viewOrNull?.let { view ->
      if (exception != null) view.showCrashed(exception, codeHostOrNull!!)
      view.saveCallback = null
    }
    viewOrNull = null
    codeHostOrNull = null
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

private fun <W : Any> TreehouseView<W>.showLoading() {
  children.remove(0, children.widgets.size)
  children.insert(0, dynamicContentWidgetFactory.Loading())
}

private fun <W : Any> TreehouseView<W>.showCrashed(
  exception: Throwable,
  codeHost: CodeHost<*>,
) {
  children.remove(0, children.widgets.size)
  children.insert(
    index = 0,
    widget = dynamicContentWidgetFactory.Crashed()
      .apply {
        uncaughtException(exception)
        restart(codeHost::restart)
      },
  )
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
