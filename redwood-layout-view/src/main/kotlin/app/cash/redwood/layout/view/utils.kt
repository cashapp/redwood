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
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGJustify
import app.cash.redwood.yoga.enums.YGMeasureMode

internal fun YGMeasureMode.toAndroid() = when (this) {
  YGMeasureMode.YGMeasureModeAtMost -> View.MeasureSpec.AT_MOST
  YGMeasureMode.YGMeasureModeExactly -> View.MeasureSpec.EXACTLY
  YGMeasureMode.YGMeasureModeUndefined -> View.MeasureSpec.UNSPECIFIED
}

internal fun FlexDirection.toYoga() = when (this) {
  FlexDirection.Row -> YGFlexDirection.YGFlexDirectionRow
  FlexDirection.RowReverse -> YGFlexDirection.YGFlexDirectionRowReverse
  FlexDirection.Column -> YGFlexDirection.YGFlexDirectionColumn
  FlexDirection.ColumnReverse -> YGFlexDirection.YGFlexDirectionColumnReverse
  else -> throw AssertionError()
}

internal fun AlignItems.toYoga() = when (this) {
  AlignItems.FlexStart -> YGAlign.YGAlignFlexStart
  AlignItems.FlexEnd -> YGAlign.YGAlignFlexEnd
  AlignItems.Center -> YGAlign.YGAlignCenter
  AlignItems.Baseline -> YGAlign.YGAlignBaseline
  AlignItems.Stretch -> YGAlign.YGAlignStretch
  else -> throw AssertionError()
}

internal fun JustifyContent.toYoga() = when (this) {
  JustifyContent.FlexStart -> YGJustify.YGJustifyFlexStart
  JustifyContent.FlexEnd -> YGJustify.YGJustifyFlexEnd
  JustifyContent.Center -> YGJustify.YGJustifyCenter
  JustifyContent.SpaceBetween -> YGJustify.YGJustifySpaceBetween
  JustifyContent.SpaceAround -> YGJustify.YGJustifySpaceAround
  JustifyContent.SpaceEvenly -> YGJustify.YGJustifySpaceEvenly
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toYoga() = when (this) {
  CrossAxisAlignment.Start -> YGAlign.YGAlignFlexStart
  CrossAxisAlignment.Center -> YGAlign.YGAlignCenter
  CrossAxisAlignment.End -> YGAlign.YGAlignFlexEnd
  CrossAxisAlignment.Stretch -> YGAlign.YGAlignStretch
  else -> throw AssertionError()
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
