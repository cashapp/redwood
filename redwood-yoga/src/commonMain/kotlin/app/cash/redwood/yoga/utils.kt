/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.YGSize
import app.cash.redwood.yoga.internal.enums.YGAlign
import app.cash.redwood.yoga.internal.enums.YGFlexDirection
import app.cash.redwood.yoga.internal.enums.YGJustify
import app.cash.redwood.yoga.internal.enums.YGMeasureMode
import app.cash.redwood.yoga.internal.interfaces.YGMeasureFunc

internal fun FlexDirection.toYoga() = when (this) {
  FlexDirection.Row -> YGFlexDirection.YGFlexDirectionRow
  FlexDirection.RowReverse -> YGFlexDirection.YGFlexDirectionRowReverse
  FlexDirection.Column -> YGFlexDirection.YGFlexDirectionColumn
  FlexDirection.ColumnReverse -> YGFlexDirection.YGFlexDirectionColumnReverse
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

internal fun AlignItems.toYoga() = when (this) {
  AlignItems.FlexStart -> YGAlign.YGAlignFlexStart
  AlignItems.FlexEnd -> YGAlign.YGAlignFlexEnd
  AlignItems.Center -> YGAlign.YGAlignCenter
  AlignItems.Baseline -> YGAlign.YGAlignBaseline
  AlignItems.Stretch -> YGAlign.YGAlignStretch
  else -> throw AssertionError()
}

internal fun AlignSelf.toYoga() = when (this) {
  AlignSelf.FlexStart -> YGAlign.YGAlignFlexStart
  AlignSelf.FlexEnd -> YGAlign.YGAlignFlexEnd
  AlignSelf.Center -> YGAlign.YGAlignCenter
  AlignSelf.Baseline -> YGAlign.YGAlignBaseline
  AlignSelf.Stretch -> YGAlign.YGAlignStretch
  AlignSelf.Auto -> YGAlign.YGAlignAuto
  else -> throw AssertionError()
}

internal class MeasureCallbackCompat(val callback: MeasureCallback) : YGMeasureFunc {
  override fun invoke(
    node: YGNode,
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
  ) = callback.measure(
    node = Node(node),
    width = width,
    widthMode = widthMode.toYoga(),
    height = height,
    heightMode = heightMode.toYoga()
  ).toYoga()
}

internal fun YGMeasureMode.toYoga() = when (this) {
  YGMeasureMode.YGMeasureModeAtMost -> MeasureMode.AtMost
  YGMeasureMode.YGMeasureModeExactly -> MeasureMode.Exactly
  YGMeasureMode.YGMeasureModeUndefined -> MeasureMode.Undefined
}

internal fun Size.toYoga() = YGSize(width, height)
