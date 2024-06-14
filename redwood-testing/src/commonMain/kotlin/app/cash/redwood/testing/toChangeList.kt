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
package app.cash.redwood.testing

import app.cash.redwood.protocol.SnapshotChangeList
import app.cash.redwood.protocol.guest.DefaultProtocolGuest
import app.cash.redwood.protocol.guest.ProtocolWidgetSystemFactory
import app.cash.redwood.protocol.guest.guestRedwoodVersion
import kotlinx.serialization.json.Json

/**
 * Encode this snapshot of widget values into a list of changes which can be serialized and
 * later applied to the UI to recreate the structure and state.
 */
public fun List<WidgetValue>.toChangeList(
  factory: ProtocolWidgetSystemFactory,
  json: Json = Json.Default,
): SnapshotChangeList {
  val guest = DefaultProtocolGuest(
    // Use latest guest version as the host version to avoid any compatibility behavior.
    hostVersion = guestRedwoodVersion,
    json = json,
    widgetSystemFactory = factory,
  )
  for ((index, child) in withIndex()) {
    guest.root.insert(index, child.toWidget(guest.widgetSystem))
  }
  return SnapshotChangeList(guest.takeChanges())
}

/**
 * Encode this widget value snapshot into a list of changes which can be serialized and
 * later applied to the UI to recreate the structure and state.
 */
public fun WidgetValue.toChangeList(
  factory: ProtocolWidgetSystemFactory,
  json: Json = Json.Default,
): SnapshotChangeList {
  return listOf(this).toChangeList(factory, json)
}
