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
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.schema.LayoutModifier

/** Grow the item relative to [value] along the main axis. */
@LayoutModifier(1, scopes = [RowScope::class, ColumnScope::class])
public data class Grow(
  val value: Int,
)

/** Shrink the item relative to [value] along the main axis. */
@LayoutModifier(2, scopes = [RowScope::class, ColumnScope::class])
public data class Shrink(
  val value: Int,
)

/** Add additional space around the item. */
@LayoutModifier(3, scopes = [RowScope::class, ColumnScope::class])
public data class Padding(
  val padding: Padding,
)

/** Set the alignment for an item along the horizontal axis. */
@LayoutModifier(4, scopes = [ColumnScope::class])
public data class HorizontalAlignment(
  val alignment: CrossAxisAlignment,
)

/** Set the alignment for an item along the vertical axis. */
@LayoutModifier(5, scopes = [RowScope::class])
public data class VerticalAlignment(
  val alignment: CrossAxisAlignment,
)
