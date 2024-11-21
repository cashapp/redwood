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

import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonPrimitive

/**
 * This class pretends to be guest UI. We publish UI changes here with [addWidget], and the
 * Treehouse content propagates them to a bound [TreehouseView].
 */
class FakeZiplineTreehouseUi(
  private val name: String,
  private val eventLog: EventLog,
) : ZiplineTreehouseUi {
  private var nextWidgetId = 1
  private var messageToThrowOnNextEvent: String? = null

  private lateinit var host: ZiplineTreehouseUi.Host
  private val extraServicesToClose = mutableListOf<CancellableService>()

  override fun start(host: ZiplineTreehouseUi.Host) {
    eventLog += "$name.start()"
    this.host = host
  }

  fun addWidget(label: String) {
    val widgetId = Id(nextWidgetId++)
    host.sendChanges(
      listOf(
        Create(widgetId, WidgetTag(4)), // Button.
        PropertyChange(widgetId, WidgetTag(4), PropertyTag(1), JsonPrimitive(label)), // text.
        PropertyChange(widgetId, WidgetTag(4), PropertyTag(2), JsonPrimitive(true)), // onClick.
        PropertyChange(widgetId, WidgetTag(4), PropertyTag(3), JsonPrimitive(0u)), // color.
        ChildrenChange.Add(Id.Root, ChildrenTag.Root, widgetId, 0),
      ),
    )
  }

  fun throwOnNextEvent(message: String) {
    messageToThrowOnNextEvent = message
  }

  override fun sendEvent(event: Event) {
    eventLog += "$name.sendEvent()"

    val toThrow = messageToThrowOnNextEvent
    if (toThrow != null) {
      messageToThrowOnNextEvent = null
      throw Exception(toThrow)
    }
  }

  fun addBackHandler(isEnabled: Boolean): CancellableService {
    val result = host.addOnBackPressedCallback(object : OnBackPressedCallbackService {
      override var isEnabled = MutableStateFlow(isEnabled)

      override fun handleOnBackPressed() {
        eventLog += "$name.onBackPressed()"
      }
    })

    // Keep track of services produced by this ZiplineTreehouseUi so we can close them when this
    // service is itself closed. In production code the ZiplineScope does this automatically.
    extraServicesToClose += result

    return result
  }

  override fun close() {
    for (cancellableService in extraServicesToClose) {
      cancellableService.close()
    }
    eventLog += "$name.close()"
  }
}
