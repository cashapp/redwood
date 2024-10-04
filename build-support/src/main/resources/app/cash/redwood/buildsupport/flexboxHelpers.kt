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

/**
 * Updates this Node to match the configuration specified by [parentModifier].
 *
 * Note that we must reset properties on [Node] to their default values if they are absent from
 * [parentModifier]. Otherwise the effects of modifiers persist after the modifiers are removed.
 *
 * Also note that we shouldn't mutate properties on a node unless they've actually changed. The
 * node internally keeps a dirty flag, and we perform a potentially-expensive layout each time the
 * dirty flag is set. This restriction prevents us from resetting each property to its default value
 * and then applying each modifier.
 *
 * Also also note that `Float.NaN` is used by these properties, and that `Float.NaN != Float.NaN`.
 * So even deciding whether a value has changed is tricky.
 *
 * Returns true if the node became dirty as a consequence of this call.
 */
internal fun Node.applyModifier(parentModifier: Modifier, density: Density): Boolean {
  val wasDirty = isDirty()

  // Avoid unnecessary mutations to the Node because it marks itself dirty its properties change.
  val oldMarginStart = marginStart
  var newMarginStart = Float.NaN
  val oldMarginEnd = marginEnd
  var newMarginEnd = Float.NaN
  val oldMarginTop = marginTop
  var newMarginTop = Float.NaN
  val oldMarginBottom = marginBottom
  var newMarginBottom = Float.NaN
  val oldAlignSelf = alignSelf
  var newAlignSelf = AlignSelf.Auto
  val oldRequestedMinWidth = requestedMinWidth
  var newRequestedMinWidth = Float.NaN
  val oldRequestedMaxWidth = requestedMaxWidth
  var newRequestedMaxWidth = Float.NaN
  val oldRequestedMinHeight = requestedMinHeight
  var newRequestedMinHeight = Float.NaN
  val oldRequestedMaxHeight = requestedMaxHeight
  var newRequestedMaxHeight = Float.NaN
  val oldFlexGrow = flexGrow
  var newFlexGrow = 0f
  val oldFlexShrink = flexShrink
  var newFlexShrink = 0f
  val oldFlexBasis = flexBasis
  var newFlexBasis = -1f

  parentModifier.forEachScoped { childModifier ->
    when (childModifier) {
      is GrowModifier -> {
        newFlexGrow = childModifier.value.toFloat()
      }

      is ShrinkModifier -> {
        newFlexShrink = childModifier.value.toFloat()
      }

      is MarginModifier -> with(density) {
        newMarginStart = childModifier.margin.start.toPx().toFloat()
        newMarginEnd = childModifier.margin.end.toPx().toFloat()
        newMarginTop = childModifier.margin.top.toPx().toFloat()
        newMarginBottom = childModifier.margin.bottom.toPx().toFloat()
      }

      is HorizontalAlignmentModifier -> {
        newAlignSelf = childModifier.alignment.toAlignSelf()
      }

      is VerticalAlignmentModifier -> {
        newAlignSelf = childModifier.alignment.toAlignSelf()
      }

      is WidthModifier -> with(density) {
        val width = childModifier.width.toPx().toFloat()
        newRequestedMinWidth = width
        newRequestedMaxWidth = width
      }

      is HeightModifier -> with(density) {
        val height = childModifier.height.toPx().toFloat()
        newRequestedMinHeight = height
        newRequestedMaxHeight = height
      }

      is SizeModifier -> with(density) {
        val width = childModifier.width.toPx().toFloat()
        newRequestedMinWidth = width
        newRequestedMaxWidth = width
        val height = childModifier.height.toPx().toFloat()
        newRequestedMinHeight = height
        newRequestedMaxHeight = height
      }

      is FlexModifier -> {
        val flex = childModifier.value.coerceAtLeast(0.0).toFloat()
        newFlexGrow = flex
        newFlexShrink = 1.0f
        newFlexBasis = if (flex > 0) 0.0f else -1.0f
      }
    }
  }

  if (newMarginStart neq oldMarginStart) marginStart = newMarginStart
  if (newMarginEnd neq oldMarginEnd) marginEnd = newMarginEnd
  if (newMarginTop neq oldMarginTop) marginTop = newMarginTop
  if (newMarginBottom neq oldMarginBottom) marginBottom = newMarginBottom
  if (newAlignSelf != oldAlignSelf) alignSelf = newAlignSelf
  if (newRequestedMinWidth neq oldRequestedMinWidth) requestedMinWidth = newRequestedMinWidth
  if (newRequestedMaxWidth neq oldRequestedMaxWidth) requestedMaxWidth = newRequestedMaxWidth
  if (newRequestedMinHeight neq oldRequestedMinHeight) requestedMinHeight = newRequestedMinHeight
  if (newRequestedMaxHeight neq oldRequestedMaxHeight) requestedMaxHeight = newRequestedMaxHeight
  if (newFlexGrow neq oldFlexGrow) flexGrow = newFlexGrow
  if (newFlexShrink neq oldFlexShrink) flexShrink = newFlexShrink
  if (newFlexBasis neq oldFlexBasis) flexBasis = newFlexBasis

  return !wasDirty && isDirty()
}

/**
 * This is like `!=` except that it's reflexive for Float.NaN.
 * https://en.wikipedia.org/wiki/NaN#Comparison_with_NaN
 */
private infix fun Float.neq(other: Float): Boolean {
  return this != other && (this == this || other == other)
}
