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
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.AlignSelf
import app.cash.redwood.yoga.JustifyContent
import app.cash.redwood.yoga.Node

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

internal fun Node.applyModifier(parentModifier: Modifier, density: Density) {
  parentModifier.forEach { childModifier ->
    when (childModifier) {
      is GrowModifier -> {
        flexGrow = childModifier.value.toFloat()
      }
      is ShrinkModifier -> {
        flexShrink = childModifier.value.toFloat()
      }
      is MarginModifier -> with(density) {
        marginStart = childModifier.margin.start.toPx().toFloat()
        marginEnd = childModifier.margin.end.toPx().toFloat()
        marginTop = childModifier.margin.top.toPx().toFloat()
        marginBottom = childModifier.margin.bottom.toPx().toFloat()
      }
      is HorizontalAlignmentModifier -> {
        alignSelf = childModifier.alignment.toAlignSelf()
      }
      is VerticalAlignmentModifier -> {
        alignSelf = childModifier.alignment.toAlignSelf()
      }
    }
  }
}

internal fun MainAxisAlignment.toJustifyContentOld() = when (this) {
  MainAxisAlignment.Start -> app.cash.redwood.flexbox.JustifyContent.FlexStart
  MainAxisAlignment.Center -> app.cash.redwood.flexbox.JustifyContent.Center
  MainAxisAlignment.End -> app.cash.redwood.flexbox.JustifyContent.FlexEnd
  MainAxisAlignment.SpaceBetween -> app.cash.redwood.flexbox.JustifyContent.SpaceBetween
  MainAxisAlignment.SpaceAround -> app.cash.redwood.flexbox.JustifyContent.SpaceAround
  MainAxisAlignment.SpaceEvenly -> app.cash.redwood.flexbox.JustifyContent.SpaceEvenly
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignItemsOld() = when (this) {
  CrossAxisAlignment.Start -> app.cash.redwood.flexbox.AlignItems.FlexStart
  CrossAxisAlignment.Center -> app.cash.redwood.flexbox.AlignItems.Center
  CrossAxisAlignment.End -> app.cash.redwood.flexbox.AlignItems.FlexEnd
  CrossAxisAlignment.Stretch -> app.cash.redwood.flexbox.AlignItems.Stretch
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignSelfOld() = when (this) {
  CrossAxisAlignment.Start -> app.cash.redwood.flexbox.AlignSelf.FlexStart
  CrossAxisAlignment.Center -> app.cash.redwood.flexbox.AlignSelf.Center
  CrossAxisAlignment.End -> app.cash.redwood.flexbox.AlignSelf.FlexEnd
  CrossAxisAlignment.Stretch -> app.cash.redwood.flexbox.AlignSelf.Stretch
  else -> throw AssertionError()
}

internal fun Margin.toSpacing(density: Density) = with(density) {
  app.cash.redwood.flexbox.Spacing(
    left = start.toPx(),
    right = end.toPx(),
    top = top.toPx(),
    bottom = bottom.toPx(),
  )
}

internal fun newFlexItem(
  direction: app.cash.redwood.flexbox.FlexDirection,
  density: Density,
  modifier: Modifier,
  measurable: app.cash.redwood.flexbox.Measurable,
): app.cash.redwood.flexbox.FlexItem {
  var flexGrow = app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexGrow
  var flexShrink = app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexShrink
  var spacing = app.cash.redwood.flexbox.Spacing.Zero
  var alignSelf = app.cash.redwood.flexbox.AlignSelf.Auto
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
        alignSelf = m.alignment.toAlignSelfOld()
      }
      is VerticalAlignmentModifier -> if (direction.isHorizontal) {
        alignSelf = m.alignment.toAlignSelfOld()
      }
    }
  }
  return app.cash.redwood.flexbox.FlexItem(
    flexGrow = flexGrow,
    flexShrink = flexShrink,
    margin = spacing,
    alignSelf = alignSelf,
    measurable = measurable,
  )
}
