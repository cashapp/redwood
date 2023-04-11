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

import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.widget.DiffConsumingNode
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.widget.Widget
import app.cash.zipline.ZiplineScope
import app.cash.zipline.withScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

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
    val hostConfiguration: HostConfiguration,
  ) : ViewState

  class Bound(
    val view: TreehouseView,
  ) : ViewState
}

private sealed interface CodeState<A : AppService> {
  class Idle<A : AppService> : CodeState<A>

  class Running<A : AppService>(
    val viewContentCodeBinding: ViewContentCodeBinding<A>,
    val hasDiffs: Boolean = false,
  ) : CodeState<A>
}

internal class TreehouseAppContent<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val source: TreehouseContentSource<A>,
  private val codeListener: CodeListener,
) : Content {
  private val dispatchers = treehouseApp.dispatchers

  private val stateFlow = MutableStateFlow<State<A>>(State(ViewState.None, CodeState.Idle()))

  override fun preload(hostConfiguration: HostConfiguration) {
    treehouseApp.dispatchers.checkUi()
    val previousState = stateFlow.value

    check(previousState.viewState is ViewState.None)

    val nextViewState = ViewState.Preloading(hostConfiguration)

    // Start the code if necessary.
    val ziplineSession = treehouseApp.ziplineSession
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && ziplineSession != null -> {
        CodeState.Running(
          startViewCodeContentBinding(
            ziplineSession,
            MutableStateFlow(nextViewState.hostConfiguration),
          ),
        )
      }
      else -> previousState.codeState
    }

    // Ask to get notified when code is ready.
    treehouseApp.boundContents += this

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  override fun bind(view: TreehouseView) {
    treehouseApp.dispatchers.checkUi()
    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    if (previousViewState is ViewState.Bound && previousViewState.view == view) return // Idempotent.
    check(previousViewState is ViewState.None || previousViewState is ViewState.Preloading)

    val nextViewState = ViewState.Bound(view)

    // Start the code if necessary.
    val ziplineSession = treehouseApp.ziplineSession
    val nextCodeState = when {
      previousState.codeState is CodeState.Idle && ziplineSession != null -> {
        CodeState.Running(
          startViewCodeContentBinding(ziplineSession, nextViewState.view.hostConfiguration),
        )
      }
      else -> previousState.codeState
    }

    // Ask to get notified when code is ready.
    if (previousViewState is ViewState.None) {
      treehouseApp.boundContents += this
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

      it.codeState is CodeState.Running && it.codeState.hasDiffs
    }
  }

  override fun unbind() {
    treehouseApp.dispatchers.checkUi()

    val previousState = stateFlow.value
    val previousViewState = previousState.viewState

    if (previousViewState is ViewState.None) return // Idempotent.

    val nextViewState = ViewState.None
    val nextCodeState = CodeState.Idle<A>()

    // Cancel the code if necessary.
    treehouseApp.boundContents.remove(this)
    if (previousState.codeState is CodeState.Running) {
      previousState.codeState.viewContentCodeBinding.cancel()
    }

    stateFlow.value = State(nextViewState, nextCodeState)
  }

  internal fun receiveZiplineSession(next: ZiplineSession<A>) {
    treehouseApp.dispatchers.checkUi()

    val previousState = stateFlow.value
    val viewState = previousState.viewState
    val previousCodeState = previousState.codeState

    val hostConfiguration = when (viewState) {
      is ViewState.Preloading -> MutableStateFlow(viewState.hostConfiguration)
      is ViewState.Bound -> viewState.view.hostConfiguration
      else -> error("unexpected receiveZiplineSession with no view bound and no preload")
    }

    val nextCodeState = CodeState.Running(
      startViewCodeContentBinding(next, hostConfiguration),
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
    ziplineSession: ZiplineSession<A>,
    firstHostConfiguration: StateFlow<HostConfiguration>,
  ): ViewContentCodeBinding<A> {
    dispatchers.checkUi()

    return ViewContentCodeBinding(
      app = treehouseApp,
      appScope = treehouseApp.appScope,
      eventPublisher = treehouseApp.eventPublisher,
      contentSource = source,
      codeListener = codeListener,
      stateFlow = stateFlow,
      session = ziplineSession,
      firstHostConfiguration = firstHostConfiguration,
    ).apply {
      start(ziplineSession)
    }
  }
}

/**
 * Connects a [TreehouseView], a [TreehouseContentSource], and a [ZiplineSession].
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
  val app: TreehouseApp<A>,
  val appScope: CoroutineScope,
  val eventPublisher: EventPublisher,
  val contentSource: TreehouseContentSource<A>,
  val codeListener: CodeListener,
  val stateFlow: MutableStateFlow<State<A>>,
  session: ZiplineSession<A>,
  firstHostConfiguration: StateFlow<HostConfiguration>,
) : EventSink, DiffSinkService {
  private val hostConfigurationFlow = SequentialStateFlow(firstHostConfiguration)

  private val isInitialLaunch: Boolean = session.isInitialLaunch

  private val json = session.zipline.json

  private val bindingScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))

  /** Only accessed on [TreehouseDispatchers.ui]. Null before [initView] and after [cancel]. */
  private var viewOrNull: TreehouseView? = null

  /** Only accessed on [TreehouseDispatchers.ui]. Null before [initView] and after [cancel]. */
  private var bridgeOrNull: ProtocolBridge<*>? = null

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private val ziplineScope = ZiplineScope()

  /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
  private var treehouseUiOrNull: ZiplineTreehouseUi? = null

  /** Only accessed on [TreehouseDispatchers.ui]. Empty after [initView]. */
  private val diffsAwaitingInitView = ArrayDeque<Diff>()

  /** Diffs applied to the UI. Only accessed on [TreehouseDispatchers.ui]. */
  var diffCount = 0

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var canceled = false

  private var initViewCalled: Boolean = false

  fun initView(view: TreehouseView) {
    app.dispatchers.checkUi()

    require(!initViewCalled)
    initViewCalled = true

    if (canceled) return

    viewOrNull = view
    @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
    bridgeOrNull = ProtocolBridge(
      container = view.children as Widget.Children<Any>,
      factory = view.widgetSystem.widgetFactory(
        json = json,
        protocolMismatchHandler = eventPublisher.protocolMismatchHandler(app),
      ) as DiffConsumingNode.Factory<Any>,
      eventSink = this,
    )

    // Apply all the diffs received before we had a view to apply them to.
    while (true) {
      val diff = diffsAwaitingInitView.removeFirstOrNull() ?: break
      receiveDiffOnUiDispatcher(diff)
    }
  }

  /** Send an event from the UI to Zipline. */
  override fun sendEvent(event: Event) {
    // Send UI events on the zipline dispatcher.
    bindingScope.launch(app.dispatchers.zipline) {
      val treehouseUi = treehouseUiOrNull ?: return@launch
      treehouseUi.sendEvent(event)
    }
  }

  /** Send a diff from Zipline to the UI. */
  override fun sendDiff(diff: Diff) {
    // Receive UI updates on the UI dispatcher.
    bindingScope.launch(app.dispatchers.ui) {
      receiveDiffOnUiDispatcher(diff)
    }
  }

  private fun receiveDiffOnUiDispatcher(diff: Diff) {
    val view = viewOrNull
    val bridge = bridgeOrNull

    if (canceled) {
      return
    }

    if (view == null || bridge == null) {
      if (diffsAwaitingInitView.isEmpty()) {
        // Unblock coroutines suspended on TreehouseAppContent.awaitContent().
        val currentState = stateFlow.value
        if (
          currentState.codeState is CodeState.Running &&
          currentState.codeState.viewContentCodeBinding == this
        ) {
          stateFlow.value = State(
            currentState.viewState,
            CodeState.Running(this, hasDiffs = true),
          )
        }
      }

      diffsAwaitingInitView += diff
      return
    }

    if (diffCount++ == 0) {
      view.reset()
      codeListener.onCodeLoaded(view, isInitialLaunch)
    }

    bridge.sendDiff(diff)
  }

  fun start(session: ZiplineSession<A>) {
    bindingScope.launch(app.dispatchers.zipline) {
      val scopedAppService = session.appService.withScope(ziplineScope)
      val treehouseUi = contentSource.get(scopedAppService)
      treehouseUiOrNull = treehouseUi
      treehouseUi.start(
        diffSink = this@ViewContentCodeBinding,
        hostConfigurations = hostConfigurationFlow.toFlowWithInitialValue(),
      )
    }
  }

  fun cancel() {
    app.dispatchers.checkUi()
    canceled = true
    viewOrNull = null
    bridgeOrNull = null
    appScope.launch(app.dispatchers.zipline) {
      treehouseUiOrNull = null
      bindingScope.cancel()
      ziplineScope.close()
    }
  }
}
