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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

internal class TreehouseAppContent<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val source: TreehouseContentSource<A>,
  private val codeListener: CodeListener,
) : Content {
  private val dispatchers = treehouseApp.dispatchers

  /** How many times this content has received fresh code. */
  private var codeLoadCount = 0

  private var view: TreehouseView? = null
  private var viewContentCodeBinding: ViewContentCodeBinding<A>? = null

  override fun bind(view: TreehouseView) {
    treehouseApp.dispatchers.checkUi()

    // Binding the bound view does nothing. This is necessary so that listeners don't need to
    // independently track the bound/unbound state.
    if (this.view == view) return

    require(this.view == null)
    this.view = view

    treehouseApp.boundContents += this
    receiveZiplineSession(treehouseApp.ziplineSession, false)
  }

  override fun unbind() {
    treehouseApp.dispatchers.checkUi()

    if (view == null) return // unbind() is idempotent.

    treehouseApp.boundContents.remove(this)
    viewContentCodeBinding?.cancel()
    view = null
    viewContentCodeBinding = null
  }

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  internal fun receiveZiplineSession(
    ziplineSession: ZiplineSession<A>?,
    codeChanged: Boolean,
  ) {
    dispatchers.checkUi()

    val previous = viewContentCodeBinding
    if (!codeChanged && previous != null) return // No change.

    if (ziplineSession != null) {
      // We have code. Launch the treehouse UI.
      val view = this.view!!
      viewContentCodeBinding = ViewContentCodeBinding(
        app = treehouseApp,
        appScope = treehouseApp.appScope,
        eventPublisher = treehouseApp.eventPublisher,
        contentSource = source,
        codeListener = codeListener,
        session = ziplineSession,
        view = view,
      ).apply {
        start(ziplineSession, view)
      }
    } else {
      // We don't have code yet. Let the CodeListener show a loading spinner or something.
      if (codeLoadCount == 0) {
        codeListener.onInitialCodeLoading()
      }
      viewContentCodeBinding = null
    }

    codeLoadCount++
    previous?.cancel()
  }
}

/**
 * Connects a [TreehouseView], a [TreehouseContentSource], and a [ZiplineSession].
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
  session: ZiplineSession<A>,
  view: TreehouseView,
) : EventSink, DiffSinkService {
  private val isInitialLaunch: Boolean = session.isInitialLaunch

  private val bindingScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))

  /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
  private var viewOrNull: TreehouseView? = view

  /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
  @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
  private var bridgeOrNull: ProtocolBridge<*>? = ProtocolBridge(
    container = view.children as Widget.Children<Any>,
    factory = view.widgetSystem.widgetFactory(
      app = app,
      json = session.zipline.json,
      protocolMismatchHandler = eventPublisher.protocolMismatchHandler(app),
    ) as DiffConsumingNode.Factory<Any>,
    eventSink = this,
  )

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private val ziplineScope = ZiplineScope()

  /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
  private var treehouseUiOrNull: ZiplineTreehouseUi? = null

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var diffCount = 0

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
      val view = viewOrNull ?: return@launch
      val bridge = bridgeOrNull ?: return@launch

      if (diffCount++ == 0) {
        view.reset()
        codeListener.onCodeLoaded(isInitialLaunch)
      }

      bridge.sendDiff(diff)
    }
  }

  fun start(session: ZiplineSession<A>, view: TreehouseView) {
    bindingScope.launch(app.dispatchers.zipline) {
      val scopedAppService = session.appService.withScope(ziplineScope)
      val treehouseUi = contentSource.get(scopedAppService)
      treehouseUiOrNull = treehouseUi
      treehouseUi.start(
        diffSink = this@ViewContentCodeBinding,
        hostConfigurations = view.hostConfiguration.toFlowWithInitialValue(),
      )
    }
  }

  fun cancel() {
    viewOrNull = null
    bridgeOrNull = null
    appScope.launch(app.dispatchers.zipline) {
      treehouseUiOrNull = null
      bindingScope.cancel()
      ziplineScope.close()
    }
  }
}
