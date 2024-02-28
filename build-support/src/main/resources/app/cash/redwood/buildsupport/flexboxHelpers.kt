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
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.modifier.Flex as FlexModifier
import app.cash.redwood.layout.modifier.Grow as GrowModifier
import app.cash.redwood.layout.modifier.Height as HeightModifier
import app.cash.redwood.layout.modifier.HorizontalAlignment as HorizontalAlignmentModifier
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Shrink as ShrinkModifier
import app.cash.redwood.layout.modifier.Size as SizeModifier
import app.cash.redwood.layout.modifier.VerticalAlignment as VerticalAlignmentModifier
import app.cash.redwood.layout.modifier.Width as WidthModifier
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.AlignSelf
import app.cash.redwood.yoga.JustifyContent
import app.cash.redwood.yoga.Node

internal interface YogaFlexContainer<W : Any> : FlexContainer<W> {
  val rootNode: Node
  val density: Density

  override fun margin(margin: Margin) {
    with(rootNode) {
      with(density) {
        marginStart = margin.start.toPx().toFloat()
        marginEnd = margin.end.toPx().toFloat()
        marginTop = margin.top.toPx().toFloat()
        marginBottom = margin.bottom.toPx().toFloat()
      }
    }
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    rootNode.alignItems = crossAxisAlignment.toAlignItems()
  }

  override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    rootNode.justifyContent = mainAxisAlignment.toJustifyContent()
  }
}

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

      is WidthModifier -> with(density) {
        val width = childModifier.width.toPx().toFloat()
        requestedMinWidth = width
        requestedMaxWidth = width
      }

      is HeightModifier -> with(density) {
        val height = childModifier.height.toPx().toFloat()
        requestedMinHeight = height
        requestedMaxHeight = height
      }

      is SizeModifier -> with(density) {
        val width = childModifier.width.toPx().toFloat()
        requestedMinWidth = width
        requestedMaxWidth = width
        val height = childModifier.height.toPx().toFloat()
        requestedMinHeight = height
        requestedMaxHeight = height
      }

      is FlexModifier -> {
        val flex = childModifier.value.coerceAtLeast(0.0).toFloat()
        flexGrow = flex
        flexShrink = 1.0f
        flexBasis = if (flex > 0) 0.0f else -1.0f
      }
    }
  }
}
