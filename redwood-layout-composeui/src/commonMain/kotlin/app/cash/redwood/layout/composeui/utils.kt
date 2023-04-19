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
import androidx.compose.ui.unit.Dp
import app.cash.redwood.flexbox.Measurable as RedwoodMeasurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import app.cash.redwood.layout.api.Dp as RedwoodDp
import app.cash.redwood.layout.api.toPlatformDp

internal fun RedwoodDp.toDp(): Dp {
  return Dp(toPlatformDp().toFloat())
}

internal fun Constraints.toMeasureSpecs(): Pair<MeasureSpec, MeasureSpec> {
  val widthSpec = when {
    hasFixedWidth -> MeasureSpec.from(maxWidth.toDouble(), MeasureSpecMode.Exactly)
    hasBoundedWidth -> MeasureSpec.from(maxWidth.toDouble(), MeasureSpecMode.AtMost)
    else -> MeasureSpec.from(minWidth.toDouble(), MeasureSpecMode.Unspecified)
  }
  val heightSpec = when {
    hasFixedHeight -> MeasureSpec.from(maxHeight.toDouble(), MeasureSpecMode.Exactly)
    hasBoundedHeight -> MeasureSpec.from(maxHeight.toDouble(), MeasureSpecMode.AtMost)
    else -> MeasureSpec.from(minHeight.toDouble(), MeasureSpecMode.Unspecified)
  }
  return widthSpec to heightSpec
}

internal fun measureSpecsToConstraints(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Constraints {
  val minWidth: Int
  val maxWidth: Int
  when (widthSpec.mode) {
    MeasureSpecMode.Exactly -> {
      minWidth = widthSpec.size.toInt()
      maxWidth = widthSpec.size.toInt()
    }
    MeasureSpecMode.AtMost -> {
      minWidth = 0
      maxWidth = widthSpec.size.toInt()
    }
    MeasureSpecMode.Unspecified -> {
      minWidth = 0
      maxWidth = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  val minHeight: Int
  val maxHeight: Int
  when (heightSpec.mode) {
    MeasureSpecMode.Exactly -> {
      minHeight = heightSpec.size.toInt()
      maxHeight = heightSpec.size.toInt()
    }
    MeasureSpecMode.AtMost -> {
      minHeight = 0
      maxHeight = heightSpec.size.toInt()
    }
    MeasureSpecMode.Unspecified -> {
      minHeight = 0
      maxHeight = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}

internal class ComposeMeasurable(private val measurable: Measurable) : RedwoodMeasurable() {

  lateinit var placeable: Placeable
    private set

  override fun width(height: Double): Double {
    return measurable.minIntrinsicWidth(height.toInt()).toDouble()
  }

  override fun height(width: Double): Double {
    return measurable.minIntrinsicHeight(width.toInt()).toDouble()
  }

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    this.placeable = measurable.measure(measureSpecsToConstraints(widthSpec, heightSpec))
    return Size(placeable.width.toDouble(), placeable.height.toDouble())
  }
}
