/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.dom.testing

import kotlin.js.Promise
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement

/**
 * Kotlin bridge into the html2canvas library. This API is incomplete and includes only the
 * features we need for snapshot testing.
 *
 * https://github.com/niklasvh/html2canvas
 */
@JsModule("html2canvas")
@JsNonModule
internal external fun html2canvas(
  element: Element,
  options: Options = definedExternally,
): Promise<HTMLCanvasElement?>

internal external interface Options {
  /** Canvas background color, null for transparent. */
  var backgroundColor: String?

  /** Width and height in pixels to be applied to node before rendering. */
  var width: Int
  var height: Int

  /** Window width and height in pixels to use when rendering the element. */
  var windowWidth: Int
  var windowHeight: Int

  /**
   * The pixel ratio of the captured image. Default use the actual pixel ratio of the device.
   * Set 1 to use as initial-scale 1 for the image.
   */
  var scale: Double
}

internal fun Options(): Options = js("{}")
