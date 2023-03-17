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

internal interface Binding {
  fun cancel()
}

/** A widget awaiting a [ZiplineSession]. */
internal object LoadingBinding : Binding {
  override fun cancel() {
  }
}

/**
 * Connects a single widget, a single [TreehouseView.boundContentSource], and a single
 * [ZiplineSession].
 *
 * Canceled if the code changes, the widget's content changes, or the widget is detached from
 * screen.
 *
 * This aggressively manages the lifecycle of the widget, breaking widget reachability when the
 * binding is canceled. It uses a single [ZiplineScope] for all Zipline services consumed by this
 * binding.
 */
internal class RealBinding<A : AppService>(
  val app: TreehouseApp<A>,
  val appScope: CoroutineScope,
  val eventPublisher: EventPublisher,
  val contentSource: TreehouseContentSource<A>,
  session: ZiplineSession<A>,
  view: TreehouseView<A>,
) : Binding, EventSink, DiffSinkService {
  private val isInitialLaunch: Boolean = session.isInitialLaunch

  private val bindingScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))

  /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
  private var viewOrNull: TreehouseView<A>? = view

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
  private var firstDiff = true

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

      if (firstDiff) {
        firstDiff = false
        view.reset()
        view.codeListener.onCodeLoaded(isInitialLaunch)
      }

      bridge.sendDiff(diff)
    }
  }

  fun start(session: ZiplineSession<A>, view: TreehouseView<A>) {
    bindingScope.launch(app.dispatchers.zipline) {
      val scopedAppService = session.appService.withScope(ziplineScope)
      val treehouseUi = contentSource.get(scopedAppService)
      treehouseUiOrNull = treehouseUi
      treehouseUi.start(
        diffSink = this@RealBinding,
        hostConfigurations = view.hostConfiguration.toFlowWithInitialValue(),
      )
    }
  }

  override fun cancel() {
    viewOrNull = null
    bridgeOrNull = null
    appScope.launch(app.dispatchers.zipline) {
      treehouseUiOrNull = null
      bindingScope.cancel()
      ziplineScope.close()
    }
  }
}
