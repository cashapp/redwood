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

import app.cash.redwood.LayoutModifier
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
import app.cash.redwood.layout.Grow as GrowModifier
import app.cash.redwood.layout.HorizontalAlignment as HorizontalAlignmentModifier
import app.cash.redwood.layout.Margin as MarginModifier
import app.cash.redwood.layout.Shrink as ShrinkModifier
import app.cash.redwood.layout.VerticalAlignment as VerticalAlignmentModifier
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Margin

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

internal fun Margin.toSpacing(density: Double) = Spacing(
  left = density * left.toDouble(),
  right = density * right.toDouble(),
  top = density * top.toDouble(),
  bottom = density * bottom.toDouble(),
)

internal fun newFlexItem(
  direction: FlexDirection,
  density: Double,
  layoutModifiers: LayoutModifier,
  measurable: Measurable,
): FlexItem {
  var flexGrow = DefaultFlexGrow
  var flexShrink = DefaultFlexShrink
  var spacing = Spacing.Zero
  var alignSelf = AlignSelf.Auto
  layoutModifiers.forEach { modifier ->
    when (modifier) {
      is GrowModifier -> {
        flexGrow = modifier.value
      }
      is ShrinkModifier -> {
        flexShrink = modifier.value
      }
      is MarginModifier -> {
        spacing = modifier.margin.toSpacing(density)
      }
      is HorizontalAlignmentModifier -> if (direction.isVertical) {
        alignSelf = modifier.alignment.toAlignSelf()
      }
      is VerticalAlignmentModifier -> if (direction.isHorizontal) {
        alignSelf = modifier.alignment.toAlignSelf()
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
