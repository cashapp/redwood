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
import org.w3c.files.Blob

/**
 * Kotlin bridge into the html-to-image library. This API is incomplete and includes only the
 * features we need for snapshot testing.
 *
 * https://github.com/bubkoo/html-to-image
 */
@JsModule("html-to-image")
@JsNonModule
internal external object HtmlToImage {
  fun toBlob(
    element: Element,
    options: Options = definedExternally,
  ): Promise<Blob>

  fun toPng(
    element: Element,
    options: Options = definedExternally,
  ): Promise<String>
}

internal external interface Options {
  /** A string value for the background color, any valid CSS color value. */
  var backgroundColor: String

  /** Width and height in pixels to be applied to node before rendering. */
  var width: Int
  var height: Int

  /**
   * Allows to scale the canva's size including the elements inside to a given width and
   * height (in pixels). style
   */
  var canvasWidth: Int
  var canvasHeight: Int

  /**
   * The pixel ratio of the captured image. Default use the actual pixel ratio of the device.
   * Set 1 to use as initial-scale 1 for the image.
   */
  var pixelRatio: Double
}

internal fun Options(): Options = js("{}")
