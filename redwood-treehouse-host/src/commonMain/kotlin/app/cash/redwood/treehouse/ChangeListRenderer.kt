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
import app.cash.redwood.protocol.SnapshotChangeList
import app.cash.redwood.protocol.host.HostProtocolAdapter
import app.cash.redwood.protocol.host.ProtocolMismatchHandler
import app.cash.redwood.protocol.host.UiEventSink
import app.cash.redwood.protocol.host.hostRedwoodVersion
import kotlinx.serialization.json.Json

/**
 * Renders a [SnapshotChangeList] into a target view by creating all of the widgets and
 * assigning their properties.
 *
 * The rendered widgets are not interactive.
 */
public class ChangeListRenderer<W : Any>(
  private val json: Json,
) {
  private val refuseAllEvents = UiEventSink { event ->
    throw IllegalStateException("unexpected event: $event")
  }

  public fun render(
    view: TreehouseView<W>,
    changeList: SnapshotChangeList,
  ) {
    view.children.remove(0, view.children.widgets.size)

    val hostAdapter = HostProtocolAdapter(
      // Use latest host version as the guest version to avoid any compatibility behavior.
      guestVersion = hostRedwoodVersion,
      container = view.children,
      factory = view.widgetSystem.widgetFactory(
        json,
        ProtocolMismatchHandler.Throwing,
      ),
      eventSink = refuseAllEvents,
      leakDetector = LeakDetector.none(),
    )
    hostAdapter.sendChanges(changeList.changes)
  }
}
