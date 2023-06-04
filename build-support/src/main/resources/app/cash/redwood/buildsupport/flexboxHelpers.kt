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
package com.example

import app.cash.redwood.Modifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.AlignSelf
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.FlexItem
import app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexGrow
import app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexShrink
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.Spacing
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.flexbox.isVertical
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.modifier.Grow as GrowModifier
import app.cash.redwood.layout.modifier.HorizontalAlignment as HorizontalAlignmentModifier
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Shrink as ShrinkModifier
import app.cash.redwood.layout.modifier.VerticalAlignment as VerticalAlignmentModifier
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGJustify
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.Yoga

internal fun MainAxisAlignment.toJustifyContent() = when (this) {
  MainAxisAlignment.Start -> JustifyContent.FlexStart
  MainAxisAlignment.Center -> JustifyContent.Center
  MainAxisAlignment.End -> JustifyContent.FlexEnd
  MainAxisAlignment.SpaceBetween -> JustifyContent.SpaceBetween
  MainAxisAlignment.SpaceAround -> JustifyContent.SpaceAround
  MainAxisAlignment.SpaceEvenly -> JustifyContent.SpaceEvenly
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignItems() = when (this) {
  CrossAxisAlignment.Start -> AlignItems.FlexStart
  CrossAxisAlignment.Center -> AlignItems.Center
  CrossAxisAlignment.End -> AlignItems.FlexEnd
  CrossAxisAlignment.Stretch -> AlignItems.Stretch
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignSelf() = when (this) {
  CrossAxisAlignment.Start -> AlignSelf.FlexStart
  CrossAxisAlignment.Center -> AlignSelf.Center
  CrossAxisAlignment.End -> AlignSelf.FlexEnd
  CrossAxisAlignment.Stretch -> AlignSelf.Stretch
  else -> throw AssertionError()
}

internal fun Margin.toSpacing(density: Density) = with(density) {
  Spacing(
    left = start.toPx(),
    right = end.toPx(),
    top = top.toPx(),
    bottom = bottom.toPx(),
  )
}

internal fun newFlexItem(
  direction: FlexDirection,
  density: Density,
  modifier: Modifier,
  measurable: Measurable,
): FlexItem {
  var flexGrow = DefaultFlexGrow
  var flexShrink = DefaultFlexShrink
  var spacing = Spacing.Zero
  var alignSelf = AlignSelf.Auto
  modifier.forEach { m ->
    when (m) {
      is GrowModifier -> {
        flexGrow = m.value
      }
      is ShrinkModifier -> {
        flexShrink = m.value
      }
      is MarginModifier -> {
        spacing = m.margin.toSpacing(density)
      }
      is HorizontalAlignmentModifier -> if (direction.isVertical) {
        alignSelf = m.alignment.toAlignSelf()
      }
      is VerticalAlignmentModifier -> if (direction.isHorizontal) {
        alignSelf = m.alignment.toAlignSelf()
      }
    }
  }
  return FlexItem(
    flexGrow = flexGrow,
    flexShrink = flexShrink,
    margin = spacing,
    alignSelf = alignSelf,
    measurable = measurable,
  )
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

internal fun YGNode.applyModifier(parentModifier: Modifier, density: Density) {
  parentModifier.forEach { childModifier ->
    when (childModifier) {
      is GrowModifier -> {
        Yoga.YGNodeStyleSetFlexGrow(this, childModifier.value.toFloat())
      }
      is ShrinkModifier -> {
        Yoga.YGNodeStyleSetFlexShrink(this, childModifier.value.toFloat())
      }
      is MarginModifier -> {
        Yoga.YGNodeStyleSetMargin(
          node = this,
          edge = YGEdge.YGEdgeLeft,
          points = with(density) { childModifier.margin.start.toPx() }.toFloat(),
        )
        Yoga.YGNodeStyleSetMargin(
          node = this,
          edge = YGEdge.YGEdgeRight,
          points = with(density) { childModifier.margin.end.toPx() }.toFloat(),
        )
        Yoga.YGNodeStyleSetMargin(
          node = this,
          edge = YGEdge.YGEdgeTop,
          points = with(density) { childModifier.margin.top.toPx() }.toFloat(),
        )
        Yoga.YGNodeStyleSetMargin(
          node = this,
          edge = YGEdge.YGEdgeBottom,
          points = with(density) { childModifier.margin.bottom.toPx() }.toFloat(),
        )
      }
      is HorizontalAlignmentModifier -> {
        Yoga.YGNodeStyleSetAlignSelf(this, childModifier.alignment.toYoga())
      }
      is VerticalAlignmentModifier -> {
        Yoga.YGNodeStyleSetAlignSelf(this, childModifier.alignment.toYoga())
      }
    }
  }
}
