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
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import app.cash.redwood.yoga.enums.YogaMeasureMode
import app.cash.redwood.yoga.internal.enums.YGMeasureMode

// Android uses 2.75 as a density scale for most recent Pixel devices and iOS
// uses 3. This aligns the two so the generic values used by Redwood layout are
// visually similar on both platforms.
internal const val DensityMultiplier = 1.1f

internal fun YGMeasureMode.toAndroid() = when (this) {
  YGMeasureMode.YGMeasureModeAtMost -> View.MeasureSpec.AT_MOST
  YGMeasureMode.YGMeasureModeExactly -> View.MeasureSpec.EXACTLY
  YGMeasureMode.YGMeasureModeUndefined -> View.MeasureSpec.UNSPECIFIED
}

internal fun MeasureSpec.Companion.fromAndroid(measureSpec: Int): MeasureSpec = from(
  size = View.MeasureSpec.getSize(measureSpec).toDouble(),
  mode = MeasureSpecMode.fromAndroid(View.MeasureSpec.getMode(measureSpec)),
)

internal fun MeasureSpec.toAndroid(): Int = View.MeasureSpec.makeMeasureSpec(size.toInt(), mode.toAndroid())

internal fun MeasureSpecMode.Companion.fromAndroid(mode: Int): MeasureSpecMode = when (mode) {
  View.MeasureSpec.UNSPECIFIED -> Unspecified
  View.MeasureSpec.EXACTLY -> Exactly
  View.MeasureSpec.AT_MOST -> AtMost
  else -> throw AssertionError()
}

internal fun MeasureSpecMode.toAndroid(): Int = when (this) {
  MeasureSpecMode.Unspecified -> View.MeasureSpec.UNSPECIFIED
  MeasureSpecMode.Exactly -> View.MeasureSpec.EXACTLY
  MeasureSpecMode.AtMost -> View.MeasureSpec.AT_MOST
  else -> throw AssertionError()
}

internal class ViewMeasurable(val view: View) : Measurable() {
  override val requestedWidth get() = view.layoutParams.width.toDouble()
  override val requestedHeight get() = view.layoutParams.height.toDouble()
  override val minWidth get() = view.minimumWidth.toDouble()
  override val minHeight get() = view.minimumHeight.toDouble()

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    view.measure(widthSpec.toAndroid(), heightSpec.toAndroid())
    return Size(view.measuredWidth.toDouble(), view.measuredHeight.toDouble())
  }
}
