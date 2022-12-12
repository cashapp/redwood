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
package app.cash.redwood.protocol.compose

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.Json

/**
 * Exposes a [Widget.Children] and [Widget.Provider] whose changes can be captured as a [Diff] to
 * send to a remote frontend. Incoming [Event]s can also be sent to this instance and will be routed
 * to the appropriate handler.
 */
public interface ProtocolBridge : EventSink {
  /**
   * The root of the widget tree onto which [provider]-produced widgets can be added. Changes to
   * this set of children records changes into the diff as [Id.Root] and [ChildrenTag.Root].
   */
  public val root: Widget.Children<Nothing>

  /**
   * The provider of factories of widgets which record property changes into the diff and whose
   * children changes are also recorded into the diff. You **must** attach returned widgets to
   * [root] or the children of a widget in the tree beneath [root] in order for it to be tracked.
   */
  public val provider: Widget.Provider<Nothing>

  /**
   * Returns any changes to [root] or [provider]-produced widgets since the last time this function
   * was called. Returns null if no changes occurred.
   */
  public fun createDiffOrNull(): Diff?

  public interface Factory {
    /** Create a new [ProtocolBridge] with its own protocol state and set of tracked widgets. */
    public fun create(
      json: Json = Json.Default,
      mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
    ): ProtocolBridge
  }
}
