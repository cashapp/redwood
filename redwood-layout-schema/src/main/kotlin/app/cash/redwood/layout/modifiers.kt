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
import app.cash.redwood.schema.LayoutModifier

/** Grow the node relative to [value] along the main axis. */
@LayoutModifier(1)
public data class GrowLayoutModifier(
  val value: Int,
)

/** Shrink the node relative to [value] along the main axis. */
@LayoutModifier(2)
public data class ShrinkLayoutModifier(
  val value: Int,
)

/** Set the alignment for a node along the horizontal axis. */
@LayoutModifier(3)
public data class HorizontalAlignmentLayoutModifier(
  val alignment: CrossAxisAlignment,
)

/** Set the alignment for a node along the vertical axis. */
@LayoutModifier(4)
public data class VerticalAlignmentLayoutModifier(
  val alignment: CrossAxisAlignment,
)
