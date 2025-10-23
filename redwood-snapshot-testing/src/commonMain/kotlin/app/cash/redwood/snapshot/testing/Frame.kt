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
package app.cash.redwood.snapshot.testing

/**
 * The size to render the widget to when taking a snapshot.
 *
 * If width or height is null, we take the widget's intrinsic width or height. For example, if the
 * width is 390 and the height is null, we'll ask the widget to fill a 390 pixel frame and tell us
 * its measured height.
 */
data class Frame(
  val width: Int?,
  val height: Int?,
  val pixelRatio: Double,
) {
  fun wrapHeight(): Frame {
    return copy(height = null)
  }

  fun wrapWidth(): Frame {
    return copy(width = null)
  }

  fun wrapWidthAndHeight(): Frame {
    return copy(width = null, height = null)
  }

  companion object {
    val None: Frame = Frame(width = null, height = null, pixelRatio = 1.0)

    /**
     * This is the dimensions of an iPhone 14, but it's intended to represent any 2025-era
     * smartphone with a 6" portrait screen and a 3x pixel ratio.
     */
    val Phone: Frame = Frame(width = 390, height = 844, pixelRatio = 3.0)
  }
}
