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
import app.cash.redwood.ui.UiConfiguration
import kotlinx.coroutines.flow.StateFlow
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

  private lateinit var host: ZiplineTreehouseUi.Host

  override fun start(host: ZiplineTreehouseUi.Host) {
    eventLog += "$name.start()"
    this.host = host
  }

  fun addWidget(label: String) {
    val widgetId = Id(nextWidgetId++)
    host.sendChanges(
      listOf(
        Create(widgetId, WidgetTag(1)),
        PropertyChange(widgetId, PropertyTag(1), JsonPrimitive(label)),
        ChildrenChange.Add(Id.Root, ChildrenTag.Root, widgetId, 0),
      ),
    )
  }

  override fun sendEvent(event: Event) {
    eventLog += "$name.sendEvent($event)"
  }

  @Suppress("OVERRIDE_DEPRECATION")
  override fun start(
    changesSink: ChangesSinkService,
    onBackPressedDispatcher: OnBackPressedDispatcherService,
    uiConfigurations: StateFlow<UiConfiguration>,
    stateSnapshot: StateSnapshot?,
  ) {
    error("unexpected call")
  }

  @Suppress("OVERRIDE_DEPRECATION")
  override fun start(
    changesSink: ChangesSinkService,
    uiConfigurations: StateFlow<UiConfiguration>,
    stateSnapshot: StateSnapshot?,
  ) {
    error("unexpected call")
  }

  override fun close() {
    eventLog += "$name.close()"
  }
}
