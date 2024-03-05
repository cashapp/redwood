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
@file:Suppress("ktlint:standard:property-naming")

package app.cash.redwood.layout

import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Flex
import app.cash.redwood.layout.modifier.Grow
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Shrink
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget
import kotlin.test.assertTrue

const val Transparent: Int = 0x00000000
const val Red: Int = 0xffff0000.toInt()
const val Green: Int = 0xff00ff00.toInt()
const val Blue: Int = 0xff0000ff.toInt()

fun argb(
  alpha: Int,
  red: Int,
  green: Int,
  blue: Int,
): Int {
  return (alpha shl 24) or (red shl 16) or (green shl 8) or (blue)
}

interface Color<T : Any> : Widget<T> {
  fun width(width: Dp)
  fun height(height: Dp)
  fun color(color: Int)
}

interface Text<T : Any> : Widget<T> {
  fun text(text: String)
  fun bgColor(color: Int)
}

/** We don't have assume() on kotlin.test. Tests that fail here should be skipped instead. */
fun assumeTrue(b: Boolean) {
  assertTrue(b)
}

internal data class CrossAxisAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment, VerticalAlignment

internal data class WidthImpl(
  override val width: Dp,
) : Width

internal data class HeightImpl(
  override val height: Dp,
) : Height

internal data class SizeImpl(
  override val width: Dp,
  override val height: Dp,
) : Size

internal data class MarginImpl(
  override val margin: Margin,
) : app.cash.redwood.layout.modifier.Margin {
  constructor(all: Dp = 0.dp) : this(Margin(all))
}

internal data class GrowImpl(
  override val value: Double,
) : Grow

internal data class ShrinkImpl(
  override val value: Double,
) : Shrink

internal data class FlexImpl(
  override val value: Double,
) : Flex
