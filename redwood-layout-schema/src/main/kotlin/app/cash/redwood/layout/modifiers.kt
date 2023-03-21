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
package app.cash.redwood.layout

import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.schema.LayoutModifier

/**
 * Configure how much of the remaining space in the layout should be assigned to this item.
 *
 * [Grow] increases the amount of remaining space to assign to this item. [value] must be non-negative (i.e. >= 0).
 *
 * https://developer.mozilla.org/en-US/docs/Web/CSS/flex-grow
 */
@LayoutModifier(1, RowScope::class, ColumnScope::class)
public data class Grow(
  val value: Double,
)

/**
 * Configure how much of the remaining space in the layout should be assigned to this item.
 *
 * [Shrink] decreases the amount of remaining space to assign to this item. [value] must be non-negative (i.e. >= 0).
 *
 * https://developer.mozilla.org/en-US/docs/Web/CSS/flex-shrink
 */
@LayoutModifier(2, RowScope::class, ColumnScope::class)
public data class Shrink(
  val value: Double,
)

/**
 * Add additional space around the item.
 */
@LayoutModifier(3, RowScope::class, ColumnScope::class)
public data class Margin(
  val margin: Margin,
)

/**
 * Set the alignment for an item along the horizontal axis.
 */
@LayoutModifier(4, ColumnScope::class)
public data class HorizontalAlignment(
  val alignment: CrossAxisAlignment,
)

/**
 * Set the alignment for an item along the vertical axis.
 */
@LayoutModifier(5, RowScope::class)
public data class VerticalAlignment(
  val alignment: CrossAxisAlignment,
)
