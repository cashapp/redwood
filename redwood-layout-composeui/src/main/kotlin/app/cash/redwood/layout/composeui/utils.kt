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
package app.cash.redwood.layout.composeui

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import app.cash.redwood.flexbox.Measurable as RedwoodMeasurable
import app.cash.redwood.flexbox.Constraints as RedwoodConstraints
import app.cash.redwood.flexbox.Size

// Android uses 2.75 as a density scale for most recent Pixel devices and iOS
// uses 3. This aligns the two so the generic values used by Redwood layout are
// visually similar on both platforms.
internal const val DensityMultiplier = 1.1

internal fun Constraints.toRedwoodConstraints() = RedwoodConstraints(
  minWidth = minWidth.toDouble(),
  maxWidth = maxWidth.toDouble(),
  minHeight = minHeight.toDouble(),
  maxHeight = maxHeight.toDouble()
)

internal fun RedwoodConstraints.toComposeConstraints() = Constraints(
  minWidth = minWidth.toInt(),
  maxWidth = maxWidth.toInt(),
  minHeight = minHeight.toInt(),
  maxHeight = maxHeight.toInt()
)

internal class ComposeMeasurable(private val measurable: Measurable) : RedwoodMeasurable() {

  lateinit var placeable: Placeable
    private set

  override fun width(height: Double): Double {
    return measurable.minIntrinsicWidth(height.toInt()).toDouble()
  }

  override fun height(width: Double): Double {
    return measurable.minIntrinsicHeight(width.toInt()).toDouble()
  }

  override fun measure(constraints: RedwoodConstraints): Size {
    this.placeable = measurable.measure(constraints.toComposeConstraints())
    return Size(placeable.width.toDouble(), placeable.height.toDouble())
  }
}
