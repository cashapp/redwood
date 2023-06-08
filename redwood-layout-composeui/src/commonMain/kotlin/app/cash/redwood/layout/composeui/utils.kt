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
import app.cash.redwood.ui.Dp as RedwoodDp
import app.cash.redwood.ui.toPlatformDp
import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size

internal class ComposeMeasureCallback(
  private val measurable: Measurable
  ) : MeasureCallback {
  lateinit var placeable: Placeable
    private set

  override fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size {
    val constraints = measureSpecsToConstraints(width, widthMode, height, heightMode)
    this.placeable = measurable.measure(constraints)
    return Size(placeable.width.toFloat(), placeable.height.toFloat())
  }
}

internal fun measureSpecsToConstraints(
  width: Float,
  widthMode: MeasureMode,
  height: Float,
  heightMode: MeasureMode,
): Constraints {
  val minWidth: Int
  val maxWidth: Int
  when (widthMode) {
    MeasureMode.Exactly -> {
      minWidth = width.toInt()
      maxWidth = width.toInt()
    }
    MeasureMode.AtMost -> {
      minWidth = 0
      maxWidth = width.toInt()
    }
    MeasureMode.Undefined -> {
      minWidth = 0
      maxWidth = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  val minHeight: Int
  val maxHeight: Int
  when (heightMode) {
    MeasureMode.Exactly -> {
      minHeight = height.toInt()
      maxHeight = height.toInt()
    }
    MeasureMode.AtMost -> {
      minHeight = 0
      maxHeight = height.toInt()
    }
    MeasureMode.Undefined -> {
      minHeight = 0
      maxHeight = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}

internal fun RedwoodDp.toDp(): Dp {
  return Dp(toPlatformDp().toFloat())
}
