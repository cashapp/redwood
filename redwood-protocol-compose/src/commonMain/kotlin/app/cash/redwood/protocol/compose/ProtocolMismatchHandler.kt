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

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import kotlin.jvm.JvmField

/**
 * Handler invoked when the protocol sent from the widget display to Compose encounters unknown
 * entities. This usually occurs when either the Compose-side or the widget-side was generated from
 * a newer schema than the other, or if their schemas were changed in an incompatible way.
 */
public interface ProtocolMismatchHandler {
  /** Handle a request to process an unknown event [tag] for the specified widget [widgetTag]. */
  public fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag)

  /** Handle an event whose node [id] is unknown. */
  public fun onUnknownEventNode(id: Id, tag: EventTag)

  public companion object {
    /** A [ProtocolMismatchHandler] which throws [IllegalArgumentException] for all callbacks. */
    @JvmField
    public val Throwing: ProtocolMismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag) {
        throw IllegalArgumentException(
          "Unknown event tag ${tag.value} for widget tag ${widgetTag.value}",
        )
      }

      override fun onUnknownEventNode(id: Id, tag: EventTag) {
        throw IllegalArgumentException(
          "Unknown node ID ${id.value} for event with tag ${tag.value}",
        )
      }
    }

    /** A [ProtocolMismatchHandler] which prints out info for all callbacks. */
    @JvmField
    public val Logging: ProtocolMismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownEvent(
        widgetTag: WidgetTag,
        tag: EventTag,
      ) {
        println("Unknown event tag ${tag.value} for widget tag ${widgetTag.value}")
      }

      override fun onUnknownEventNode(
        id: Id,
        tag: EventTag,
      ) {
        println("Unknown node ID ${id.value} for event with tag ${tag.value}")
      }
    }
  }
}
