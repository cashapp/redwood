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
package app.cash.redwood.protocol.widget

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import kotlin.jvm.JvmField
import kotlin.native.ObjCName

/**
 * Handler invoked when the protocol sent from Compose to the widget display encounters unknown
 * entities. This usually occurs when either the Compose-side or the widget-side was generated from
 * a newer schema than the other, or if their schemas were changed in an incompatible way.
 */
@ObjCName("ProtocolMismatchHandler", exact = true)
public interface ProtocolMismatchHandler {
  /**
   * Handle a request to create an unknown widget [tag].
   *
   * This function will be invoked every time a widget with an unknown tag is created. For example,
   * a row containing three unknown widgets will see this function invoked three times.
   * Use the [tag] to de-duplicate the callbacks if desired.
   */
  public fun onUnknownWidget(tag: WidgetTag)

  /**
   * Handle a request to create an unknown layout modifier [tag].
   *
   * This function will be invoked every time a modifier with an unknown tag is used. For example,
   * three widgets each using the unknown modifier will see this function invoked three times.
   * Modifiers are serialized as a chain, so updating an unrelated modifier in a chain which also
   * contains an unknown modifier will cause this function to be invoked again.
   * Use the [tag] to de-duplicate the callbacks if desired.
   */
  public fun onUnknownModifier(tag: ModifierTag)

  /**
   * Handle a request to manipulate unknown children [tag] for the specified [widgetTag].
   *
   * This function will be invoked every time an operation occurs on unknown children. For example,
   * three buttons inside an unknown children container will see this function invoked three times.
   * Use the [widgetTag] and [tag] combination to de-duplicate the callbacks if desired.
   */
  public fun onUnknownChildren(widgetTag: WidgetTag, tag: ChildrenTag)

  /**
   * Handle a request to set an unknown property [tag] for the specified [widgetTag].
   *
   * This function will be invoked every time an operation occurs on the property. For example,
   * a timer updating the unknown property every second will see this function invoked every second.
   * Use the [widgetTag] and [tag] combination to de-duplicate the callbacks if desired.
   */
  public fun onUnknownProperty(widgetTag: WidgetTag, tag: PropertyTag)

  public companion object {
    /** A [ProtocolMismatchHandler] which throws [IllegalArgumentException] for all callbacks. */
    @JvmField
    public val Throwing: ProtocolMismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownWidget(tag: WidgetTag) {
        throw IllegalArgumentException("Unknown widget tag ${tag.value}")
      }

      override fun onUnknownModifier(tag: ModifierTag) {
        throw IllegalArgumentException("Unknown layout modifier tag ${tag.value}")
      }

      override fun onUnknownChildren(widgetTag: WidgetTag, tag: ChildrenTag) {
        throw IllegalArgumentException(
          "Unknown children tag ${tag.value} for widget tag ${widgetTag.value}",
        )
      }

      override fun onUnknownProperty(widgetTag: WidgetTag, tag: PropertyTag) {
        throw IllegalArgumentException(
          "Unknown property tag ${tag.value} for widget tag ${widgetTag.value}",
        )
      }
    }
  }
}
