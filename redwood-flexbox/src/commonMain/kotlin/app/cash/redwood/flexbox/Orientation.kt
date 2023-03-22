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
  return if (isHorizontal) Orientation.Horizontal else Orientation.Vertical
}

internal fun Orientation.mainMeasuredSizeWithMargin(node: FlexItem): Double =
  mainMeasuredSize(node) + mainMargin(node)

internal fun Orientation.crossMeasuredSizeWithMargin(node: FlexItem): Double =
  crossMeasuredSize(node) + crossMargin(node)

/**
 * An interface to perform operations along the main/cross axis without knowledge
 * of the underlying [FlexDirection].
 */
internal sealed interface Orientation {
  fun mainMargin(margin: Spacing): Double
  fun crossMargin(margin: Spacing): Double

  fun mainMargin(item: FlexItem): Double
  fun crossMargin(item: FlexItem): Double

  fun mainSize(item: FlexItem): Double
  fun crossSize(item: FlexItem): Double
  fun mainMeasuredSize(item: FlexItem): Double
  fun crossMeasuredSize(item: FlexItem): Double

  object Horizontal : Orientation {
    override fun mainMargin(margin: Spacing) = margin.left + margin.right
    override fun crossMargin(margin: Spacing) = margin.top + margin.bottom
    override fun mainMargin(item: FlexItem) = item.margin.left + item.margin.right
    override fun crossMargin(item: FlexItem) = item.margin.top + item.margin.bottom
    override fun mainSize(item: FlexItem) = item.measurable.requestedWidth
    override fun crossSize(item: FlexItem) = item.measurable.requestedHeight
    override fun mainMeasuredSize(item: FlexItem) = item.width
    override fun crossMeasuredSize(item: FlexItem) = item.height
  }

  object Vertical : Orientation {
    override fun mainMargin(margin: Spacing) = margin.top + margin.bottom
    override fun crossMargin(margin: Spacing) = margin.left + margin.right
    override fun mainMargin(item: FlexItem) = item.margin.top + item.margin.bottom
    override fun crossMargin(item: FlexItem) = item.margin.left + item.margin.right
    override fun mainSize(item: FlexItem) = item.measurable.requestedHeight
    override fun crossSize(item: FlexItem) = item.measurable.requestedWidth
    override fun mainMeasuredSize(item: FlexItem) = item.height
    override fun crossMeasuredSize(item: FlexItem) = item.width
  }
}
