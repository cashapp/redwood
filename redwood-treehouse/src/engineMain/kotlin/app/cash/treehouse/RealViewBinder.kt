/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.treehouse

import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.protocol.widget.ProtocolDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/** Displays Redwood widgets to a [TreehouseView]. */
public class RealViewBinder(
  private val dispatchers: TreehouseDispatchers,
  private val adapter: ViewBinder.Adapter,
) : ViewBinder {
  override fun codeLoading(view: TreehouseView<*>) {
    adapter.codeLoading(view)
  }

  override fun bind(
    scope: CoroutineScope,
    content: ZiplineTreehouseUi,
    view: TreehouseView<*>,
    json: Json,
    isInitialCode: Boolean,
  ): ViewBinding {
    val result = RealViewBinding(scope, content, view, json, isInitialCode)
    content.start(result.diffSinkService)
    return result
  }

  /** The host app's side of a Treehouse UI. */
  private inner class RealViewBinding(
    val scope: CoroutineScope,
    val content: ZiplineTreehouseUi,
    val view: TreehouseView<*>,
    json: Json,
    val isInitialCode: Boolean,
  ) : ViewBinding {
    val eventSink: EventSink = EventSink { event ->
      // Send UI events on the zipline dispatcher.
      scope.launch(dispatchers.zipline) {
        content.sendEvent(event)
      }
    }

    val widgetFactory = adapter.widgetFactory(view, json)

    @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
    val display = ProtocolDisplay(
      root = view.protocolDisplayRoot as DiffConsumingWidget<Any>,
      factory = widgetFactory as DiffConsumingWidget.Factory<Any>,
      eventSink = eventSink,
    )

    val diffSinkService = object : DiffSinkService {
      private var firstDiff = true

      override fun sendDiff(diff: Diff) {
        // Receive UI updates on the main dispatcher.
        scope.launch(dispatchers.main) {
          if (firstDiff) {
            firstDiff = false

            when {
              isInitialCode -> adapter.beforeInitialCode(view)
              else -> adapter.beforeUpdatedCode(view)
            }
          }

          display.sendDiff(diff)
        }
      }
    }

    override fun cancel() {
      content.close()
    }
  }
}
