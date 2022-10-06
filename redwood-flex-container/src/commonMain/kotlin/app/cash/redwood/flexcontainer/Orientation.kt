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
package app.cash.redwood.flexcontainer

internal fun FlexDirection.toOrientation(): Orientation {
  return if (isHorizontal) Orientation.Horizontal else Orientation.Vertical
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

  fun mainSize(node: FlexItem): Int
  fun crossSize(node: FlexItem): Int
  fun mainMeasuredSize(node: FlexItem): Int
  fun crossMeasuredSize(node: FlexItem): Int

  fun mainMarginStart(node: FlexItem): Int
  fun mainMarginEnd(node: FlexItem): Int
  fun crossMarginStart(node: FlexItem): Int
  fun crossMarginEnd(node: FlexItem): Int

  object Horizontal : Orientation {
    override fun mainPaddingStart(padding: Spacing) = padding.start
    override fun mainPaddingEnd(padding: Spacing) = padding.end
    override fun crossPaddingStart(padding: Spacing) = padding.top
    override fun crossPaddingEnd(padding: Spacing) = padding.bottom
    override fun mainSize(node: FlexItem) = node.measurable.width
    override fun crossSize(node: FlexItem) = node.measurable.height
    override fun mainMeasuredSize(node: FlexItem) = node.measuredWidth
    override fun crossMeasuredSize(node: FlexItem) = node.measuredHeight
    override fun mainMarginStart(node: FlexItem) = node.margin.start
    override fun mainMarginEnd(node: FlexItem) = node.margin.end
    override fun crossMarginStart(node: FlexItem) = node.margin.top
    override fun crossMarginEnd(node: FlexItem) = node.margin.bottom
  }

  object Vertical : Orientation {
    override fun mainPaddingStart(padding: Spacing) = padding.top
    override fun mainPaddingEnd(padding: Spacing) = padding.bottom
    override fun crossPaddingStart(padding: Spacing) = padding.start
    override fun crossPaddingEnd(padding: Spacing) = padding.end
    override fun mainSize(node: FlexItem) = node.measurable.height
    override fun crossSize(node: FlexItem) = node.measurable.width
    override fun mainMeasuredSize(node: FlexItem) = node.measuredHeight
    override fun crossMeasuredSize(node: FlexItem) = node.measuredWidth
    override fun mainMarginStart(node: FlexItem) = node.margin.top
    override fun mainMarginEnd(node: FlexItem) = node.margin.bottom
    override fun crossMarginStart(node: FlexItem) = node.margin.start
    override fun crossMarginEnd(node: FlexItem) = node.margin.end
  }
}
