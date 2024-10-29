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

internal data class CrossAxisAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment,
  VerticalAlignment

internal data class VerticalAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : VerticalAlignment

internal data class HorizontalAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment

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

internal fun shortText() = "Short\n".repeat(2).trim()

internal fun mediumText() = "MediumMedium\n".repeat(7).trim()

internal fun longText() = "LongLongLongLongLongLongLong\n".repeat(12).trim()
