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

import kotlin.jvm.JvmField

/**
 * Handler invoked when the protocol sent from Compose to the widget display encounters unknown
 * entities. This usually occurs when either the Compose-side or the widget-side was generated from
 * a newer schema than the other, or if their schemas were changed in an incompatible way.
 */
public interface ProtocolMismatchHandler {
  /** Handle a request to create an unknown widget [kind]. */
  public fun onUnknownWidget(kind: Int)

  /** Handle a request to create an unknown layout modifier [tag]. */
  public fun onUnknownLayoutModifier(tag: Int)

  /** Handle a request to manipulate unknown children [tag] for the specified widget [kind]. */
  public fun onUnknownChildren(kind: Int, tag: UInt)

  /** Handle a request to set an unknown property [tag] for the specified widget [kind]. */
  public fun onUnknownProperty(kind: Int, tag: UInt)

  public companion object {
    /** A [ProtocolMismatchHandler] which throws [IllegalArgumentException] for all callbacks. */
    @JvmField
    public val Throwing: ProtocolMismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownWidget(kind: Int) {
        throw IllegalArgumentException("Unknown widget kind $kind")
      }

      override fun onUnknownLayoutModifier(tag: Int) {
        throw IllegalArgumentException("Unknown layout modifier tag $tag")
      }

      override fun onUnknownChildren(kind: Int, tag: UInt) {
        throw IllegalArgumentException("Unknown children tag $tag for widget kind $kind")
      }

      override fun onUnknownProperty(kind: Int, tag: UInt) {
        throw IllegalArgumentException("Unknown property tag $tag for widget kind $kind")
      }
    }
  }
}
