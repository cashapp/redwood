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

internal fun FlexDirection.toDirection(): Direction {
  if (this == FlexDirection.Row || this == FlexDirection.RowReverse) {
    return Direction.Horizontal
  } else {
    return Direction.Vertical
  }
}

/**
 * An interface to perform operations along the main/cross axis without knowledge
 * of the underlying [FlexDirection].
 */
internal sealed interface Direction {
  fun mainPaddingStart(padding: Spacing): Int
  fun mainPaddingEnd(padding: Spacing): Int
  fun crossPaddingStart(padding: Spacing): Int
  fun crossPaddingEnd(padding: Spacing): Int

  fun mainSize(node: Node): Int
  fun crossSize(node: Node): Int
  fun mainMeasuredSize(node: Node): Int
  fun crossMeasuredSize(node: Node): Int

  fun mainMarginStart(node: Node): Int
  fun mainMarginEnd(node: Node): Int
  fun crossMarginStart(node: Node): Int
  fun crossMarginEnd(node: Node): Int

  object Horizontal : Direction {
    override fun mainPaddingStart(padding: Spacing) = padding.start
    override fun mainPaddingEnd(padding: Spacing) = padding.end
    override fun crossPaddingStart(padding: Spacing) = padding.top
    override fun crossPaddingEnd(padding: Spacing) = padding.bottom
    override fun mainSize(node: Node) = node.measurable.width
    override fun crossSize(node: Node) = node.measurable.height
    override fun mainMeasuredSize(node: Node) = node.measuredWidth
    override fun crossMeasuredSize(node: Node) = node.measuredHeight
    override fun mainMarginStart(node: Node) = node.margin.start
    override fun mainMarginEnd(node: Node) = node.margin.end
    override fun crossMarginStart(node: Node) = node.margin.top
    override fun crossMarginEnd(node: Node) = node.margin.bottom
  }

  object Vertical : Direction {
    override fun mainPaddingStart(padding: Spacing) = padding.top
    override fun mainPaddingEnd(padding: Spacing) = padding.bottom
    override fun crossPaddingStart(padding: Spacing) = padding.start
    override fun crossPaddingEnd(padding: Spacing) = padding.end
    override fun mainSize(node: Node) = node.measurable.height
    override fun crossSize(node: Node) = node.measurable.width
    override fun mainMeasuredSize(node: Node) = node.measuredHeight
    override fun crossMeasuredSize(node: Node) = node.measuredWidth
    override fun mainMarginStart(node: Node) = node.margin.top
    override fun mainMarginEnd(node: Node) = node.margin.bottom
    override fun crossMarginStart(node: Node) = node.margin.start
    override fun crossMarginEnd(node: Node) = node.margin.end
  }
}
