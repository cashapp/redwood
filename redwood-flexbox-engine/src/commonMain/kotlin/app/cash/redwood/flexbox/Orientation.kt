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
package app.cash.redwood.flexbox

internal fun FlexDirection.toOrientation(): Orientation {
  if (this == FlexDirection.Row || this == FlexDirection.RowReverse) {
    return Orientation.Horizontal
  } else {
    return Orientation.Vertical
  }
}

/**
 * An interface to perform operations along the main/cross axis without knowledge
 * of the underlying [FlexDirection].
 */
internal sealed interface Orientation {
  fun mainPaddingStart(padding: Spacing): Int
  fun mainPaddingEnd(padding: Spacing): Int
  fun crossPaddingStart(padding: Spacing): Int
  fun crossPaddingEnd(padding: Spacing): Int

  fun mainSize(node: FlexNode): Int
  fun crossSize(node: FlexNode): Int
  fun mainMeasuredSize(node: FlexNode): Int
  fun crossMeasuredSize(node: FlexNode): Int

  fun mainMarginStart(node: FlexNode): Int
  fun mainMarginEnd(node: FlexNode): Int
  fun crossMarginStart(node: FlexNode): Int
  fun crossMarginEnd(node: FlexNode): Int

  object Horizontal : Orientation {
    override fun mainPaddingStart(padding: Spacing) = padding.start
    override fun mainPaddingEnd(padding: Spacing) = padding.end
    override fun crossPaddingStart(padding: Spacing) = padding.top
    override fun crossPaddingEnd(padding: Spacing) = padding.bottom
    override fun mainSize(node: FlexNode) = node.measurable.width
    override fun crossSize(node: FlexNode) = node.measurable.height
    override fun mainMeasuredSize(node: FlexNode) = node.measuredWidth
    override fun crossMeasuredSize(node: FlexNode) = node.measuredHeight
    override fun mainMarginStart(node: FlexNode) = node.margin.start
    override fun mainMarginEnd(node: FlexNode) = node.margin.end
    override fun crossMarginStart(node: FlexNode) = node.margin.top
    override fun crossMarginEnd(node: FlexNode) = node.margin.bottom
  }

  object Vertical : Orientation {
    override fun mainPaddingStart(padding: Spacing) = padding.top
    override fun mainPaddingEnd(padding: Spacing) = padding.bottom
    override fun crossPaddingStart(padding: Spacing) = padding.start
    override fun crossPaddingEnd(padding: Spacing) = padding.end
    override fun mainSize(node: FlexNode) = node.measurable.height
    override fun crossSize(node: FlexNode) = node.measurable.width
    override fun mainMeasuredSize(node: FlexNode) = node.measuredHeight
    override fun crossMeasuredSize(node: FlexNode) = node.measuredWidth
    override fun mainMarginStart(node: FlexNode) = node.margin.top
    override fun mainMarginEnd(node: FlexNode) = node.margin.bottom
    override fun crossMarginStart(node: FlexNode) = node.margin.start
    override fun crossMarginEnd(node: FlexNode) = node.margin.end
  }
}
