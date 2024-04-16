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
package app.cash.redwood.protocol.guest

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
  /**
   * Handle a request to process an unknown event [tag] for the specified widget [widgetTag].
   *
   * This function will be invoked every time an unknown event is sent to a widget. For example,
   * three click events on a widget with no click event will see this function invoked three times.
   * Use the [widgetTag] and [tag] combination to de-duplicate the callbacks if desired.
   */
  public fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag)

  /**
   * Handle an event whose node [id] is unknown.
   *
   * This function will be invoked every time an event is sent to an unknown widget. For example,
   * clicking three times on a button that has no corresponding instance will see this function
   * invoked three times. Use the [id] and [tag] combination to de-duplicate the callbacks
   * if desired.
   */
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
  }
}
