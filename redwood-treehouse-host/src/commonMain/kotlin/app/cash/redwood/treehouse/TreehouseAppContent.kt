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

import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.protocol.widget.ProtocolFactory
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.Widget
import app.cash.zipline.ZiplineApiMismatchException
import app.cash.zipline.ZiplineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
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

  class Preloading(
    val onBackPressedDispatcher: OnBackPressedDispatcher,
    val uiConfiguration: UiConfiguration,
  ) : ViewState

  class Bound(
    val view: TreehouseView<*>,
  ) : ViewState
}

private sealed interface CodeState<A : AppService> {
  class Idle<A : AppService> : CodeState<A>

  class Running<A : AppService>(
    val viewContentCodeBinding: ViewContentCodeBinding<A>,
    val hasChanges: Boolean = false,
  ) : CodeState<A>
}

internal class TreehouseAppContent<A : AppService>(
  private val codeHost: CodeHost<A>,
  private val dispatchers: TreehouseDispatchers,
  private val appScope: CoroutineScope,
  private val eventPublisher: EventPublisher,
  private val codeListener: CodeListener,
  private val source: TreehouseContentSource<A>,
) : Content, CodeHost.Listener<A> {
  private val stateFlow = MutableStateFlow<State<A>>(State(ViewState.None, CodeState.Idle()))

  override fun preload(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    uiConfiguration: UiConfiguration,
  ) {
    dispatchers.checkUi()
    val previousState = stateFlow.value

    check(previousState.viewState is ViewState.None)

    val nextViewState = ViewState.Preloading(onBackPressedDispatcher, uiConfiguration)

    // Start the code if necessary.
    val codeSession = codeHost.session
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && codeSession != null -> {
        CodeState.Running(
          startViewCodeContentBinding(
            codeSession = codeSession,
            isInitialLaunch = true,
            onBackPressedDispatcher = onBackPressedDispatcher,
            firstUiConfiguration = MutableStateFlow(nextViewState.uiConfiguration),
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
    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    if (previousViewState is ViewState.Bound && previousViewState.view == view) return // Idempotent.
    check(previousViewState is ViewState.None || previousViewState is ViewState.Preloading)

    val nextViewState = ViewState.Bound(view)

    // Start the code if necessary.
    val codeSession = codeHost.session
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && codeSession != null -> {
        CodeState.Running(
          startViewCodeContentBinding(
            codeSession = codeSession,
            isInitialLaunch = true,
            onBackPressedDispatcher = nextViewState.view.onBackPressedDispatcher,
            firstUiConfiguration = nextViewState.view.uiConfiguration,
          ),
        )
      }
      else -> previousState.codeState
    }

    // Ask to get notified when code is ready.
    if (previousViewState is ViewState.None) {
      codeHost.addListener(this)
    }

    // Make sure we're showing something in the view; either loaded code or a spinner to show that
    // code is coming.
    when (nextCodeState) {
      is CodeState.Idle -> codeListener.onInitialCodeLoading(view)
      is CodeState.Running -> nextCodeState.viewContentCodeBinding.initView(view)
    }

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  override suspend fun awaitContent() {
    stateFlow.first {
      if (it.viewState is ViewState.None) {
        throw CancellationException("unbound while awaiting content")
      }

      it.codeState is CodeState.Running && it.codeState.hasChanges
    }
  }

  override fun unbind() {
    dispatchers.checkUi()

    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    if (previousViewState is ViewState.None) return // Idempotent.

    val nextViewState = ViewState.None
    val nextCodeState = CodeState.Idle<A>()

    // Cancel the code if necessary.
    codeHost.removeListener(this)
    if (previousState.codeState is CodeState.Running) {
      previousState.codeState.viewContentCodeBinding.cancel()
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
      is ViewState.Preloading -> MutableStateFlow(viewState.uiConfiguration)
      is ViewState.Bound -> viewState.view.uiConfiguration
      else -> error("unexpected receiveCodeSession with no view bound and no preload")
    }

    val nextCodeState = CodeState.Running(
      startViewCodeContentBinding(
        codeSession = next,
        isInitialLaunch = previousCodeState is CodeState.Idle,
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
      previousCodeState.viewContentCodeBinding.cancel()
    }

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

    return ViewContentCodeBinding(
      codeHost = codeHost,
      dispatchers = dispatchers,
      appScope = appScope,
      eventPublisher = eventPublisher,
      contentSource = source,
      codeListener = codeListener,
      stateFlow = stateFlow,
      isInitialLaunch = isInitialLaunch,
      json = codeSession.json,
      onBackPressedDispatcher = onBackPressedDispatcher,
      firstUiConfiguration = firstUiConfiguration,
    ).apply {
      start(codeSession)
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
  val codeHost: CodeHost<A>,
  val dispatchers: TreehouseDispatchers,
  val appScope: CoroutineScope,
  val eventPublisher: EventPublisher,
  val contentSource: TreehouseContentSource<A>,
  val codeListener: CodeListener,
  val stateFlow: MutableStateFlow<State<A>>,
  private val isInitialLaunch: Boolean,
  private val json: Json,
  private val onBackPressedDispatcher: OnBackPressedDispatcher,
  firstUiConfiguration: StateFlow<UiConfiguration>,
) : EventSink, ChangesSinkService, TreehouseView.SaveCallback, ZiplineTreehouseUi.Host {
  private val uiConfigurationFlow = SequentialStateFlow(firstUiConfiguration)

  private val bindingScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))

  /** Only accessed on [TreehouseDispatchers.ui]. Null before [initView] and after [cancel]. */
  private var viewOrNull: TreehouseView<*>? = null

  /** Only accessed on [TreehouseDispatchers.ui]. Null before [initView] and after [cancel]. */
  private var bridgeOrNull: ProtocolBridge<*>? = null

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private val serviceScope = codeHost.newServiceScope()

  /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
  private var treehouseUiOrNull: ZiplineTreehouseUi? = null

  /** Only accessed on [TreehouseDispatchers.ui]. Empty after [initView]. */
  private val changesAwaitingInitView = ArrayDeque<List<Change>>()

  /** Changes applied to the UI. Only accessed on [TreehouseDispatchers.ui]. */
  var changesCount = 0

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

    @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
    bridgeOrNull = ProtocolBridge(
      container = view.children as Widget.Children<Any>,
      factory = view.widgetSystem.widgetFactory(
        json = json,
        protocolMismatchHandler = eventPublisher.widgetProtocolMismatchHandler,
      ) as ProtocolFactory<Any>,
      eventSink = this,
    )

    // Apply all the changes received before we had a view to apply them to.
    while (true) {
      val changes = changesAwaitingInitView.removeFirstOrNull() ?: break
      receiveChangesOnUiDispatcher(changes)
    }
  }

  /** Send an event from the UI to Zipline. */
  override fun sendEvent(event: Event) {
    // Send UI events on the zipline dispatcher.
    bindingScope.launch(dispatchers.zipline) {
      val treehouseUi = treehouseUiOrNull ?: return@launch
      treehouseUi.sendEvent(event)
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
    val view = viewOrNull
    val bridge = bridgeOrNull

    if (canceled) {
      return
    }

    if (view == null || bridge == null) {
      if (changesAwaitingInitView.isEmpty()) {
        // Unblock coroutines suspended on TreehouseAppContent.awaitContent().
        val currentState = stateFlow.value
        if (
          currentState.codeState is CodeState.Running &&
          currentState.codeState.viewContentCodeBinding == this
        ) {
          stateFlow.value = State(
            currentState.viewState,
            CodeState.Running(this, hasChanges = true),
          )
        }
      }

      changesAwaitingInitView += changes
      return
    }

    if (changesCount++ == 0) {
      view.reset()
      codeListener.onCodeLoaded(view, isInitialLaunch)
    }

    bridge.sendChanges(changes)
  }

  fun start(session: CodeSession<A>) {
    bindingScope.launch(dispatchers.zipline) {
      val scopedAppService = serviceScope.apply(session.appService)
      val treehouseUi = contentSource.get(scopedAppService)
      treehouseUiOrNull = treehouseUi
      stateSnapshot = viewOrNull?.stateSnapshotId?.let {
        codeHost.stateStore.get(it.value.orEmpty())
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
    callback: OnBackPressedCallbackService,
  ): CancellableService {
    dispatchers.checkZipline()
    val cancellable = onBackPressedDispatcher.addCallback(
      object : OnBackPressedCallback(callback.isEnabled) {
        override fun handleOnBackPressed() {
          bindingScope.launch(dispatchers.zipline) {
            callback.handleOnBackPressed()
          }
        }
      },
    )

    return object : CancellableService {
      override fun cancel() {
        dispatchers.checkZipline()
        cancellable.cancel()
      }

      override fun close() {
        cancel()
      }
    }
  }

  override fun performSave(id: String) {
    appScope.launch(dispatchers.zipline) {
      val state = treehouseUiOrNull?.snapshotState() ?: return@launch
      codeHost.stateStore.put(id, state)
    }
  }

  fun cancel() {
    dispatchers.checkUi()
    canceled = true
    viewOrNull?.saveCallback = null
    viewOrNull = null
    bridgeOrNull = null
    appScope.launch(dispatchers.zipline) {
      treehouseUiOrNull = null
      bindingScope.cancel()
      serviceScope.close()
    }
  }
}
