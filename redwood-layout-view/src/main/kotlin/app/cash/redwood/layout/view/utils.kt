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
package app.cash.redwood.layout.view

import android.view.View
import app.cash.redwood.flexbox.Constraints
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.Size
import app.cash.redwood.flexbox.hasBoundedHeight
import app.cash.redwood.flexbox.hasBoundedWidth
import app.cash.redwood.flexbox.hasFixedHeight
import app.cash.redwood.flexbox.hasFixedWidth

// Android uses 2.75 as a density scale for most recent Pixel devices and iOS
// uses 3. This aligns the two so the generic values used by Redwood layout are
// visually similar on both platforms.
internal const val DensityMultiplier = 1.1

internal fun measureSpecsToConstraints(widthSpec: Int, heightSpec: Int): Constraints {
  val minWidth: Double
  val maxWidth: Double
  val minHeight: Double
  val maxHeight: Double

  val widthSize = View.MeasureSpec.getSize(widthSpec).toDouble()
  when (View.MeasureSpec.getMode(widthSpec)) {
    View.MeasureSpec.EXACTLY -> {
      minWidth = widthSize
      maxWidth = widthSize
    }
    View.MeasureSpec.AT_MOST -> {
      minWidth = 0.0
      maxWidth = widthSize
    }
    else -> {
      minWidth = 0.0
      maxWidth = Constraints.Infinity
    }
  }

  val heightSize = View.MeasureSpec.getSize(heightSpec).toDouble()
  when (View.MeasureSpec.getMode(heightSpec)) {
    View.MeasureSpec.EXACTLY -> {
      minHeight = heightSize
      maxHeight = heightSize
    }
    View.MeasureSpec.AT_MOST -> {
      minHeight = 0.0
      maxHeight = heightSize
    }
    else -> {
      minHeight = 0.0
      maxHeight = Constraints.Infinity
    }
  }

  return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}

internal val Constraints.widthSpec: Int
  get() = when {
    hasFixedWidth -> View.MeasureSpec.makeMeasureSpec(maxWidth.toInt(), View.MeasureSpec.EXACTLY)
    hasBoundedWidth -> View.MeasureSpec.makeMeasureSpec(maxWidth.toInt(), View.MeasureSpec.AT_MOST)
    else -> View.MeasureSpec.makeMeasureSpec(maxWidth.toInt(), View.MeasureSpec.UNSPECIFIED)
  }

internal val Constraints.heightSpec: Int
  get() = when {
    hasFixedHeight -> View.MeasureSpec.makeMeasureSpec(maxHeight.toInt(), View.MeasureSpec.EXACTLY)
    hasBoundedHeight -> View.MeasureSpec.makeMeasureSpec(maxHeight.toInt(), View.MeasureSpec.AT_MOST)
    else -> View.MeasureSpec.makeMeasureSpec(maxHeight.toInt(), View.MeasureSpec.UNSPECIFIED)
  }

internal class ViewMeasurable(val view: View) : Measurable() {
  override val requestedWidth get() = view.layoutParams.width.toDouble()
  override val requestedHeight get() = view.layoutParams.height.toDouble()
  override val minWidth get() = view.minimumWidth.toDouble()
  override val minHeight get() = view.minimumHeight.toDouble()

  override fun measure(constraints: Constraints): Size {
    view.measure(constraints.widthSpec, constraints.heightSpec)
    return Size(view.measuredWidth.toDouble(), view.measuredHeight.toDouble())
  }
}
