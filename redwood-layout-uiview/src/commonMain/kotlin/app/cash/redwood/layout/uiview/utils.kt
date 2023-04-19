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
package app.cash.redwood.layout.uiview

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
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

// The cross platform density multiples use iOS as an anchor.
internal const val DensityMultiplier = 1.0

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

@Suppress("UNCHECKED_CAST")
internal val UIView.typedSubviews: List<UIView>
  get() = subviews as List<UIView>
