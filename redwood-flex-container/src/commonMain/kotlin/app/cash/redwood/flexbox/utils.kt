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

import app.cash.redwood.flexbox.Measurable.Companion.MatchParent
import app.cash.redwood.flexbox.Measurable.Companion.WrapContent

internal fun AlignSelf.toAlignItems() = when (this) {
  AlignSelf.FlexStart -> AlignItems.FlexStart
  AlignSelf.FlexEnd -> AlignItems.FlexEnd
  AlignSelf.Center -> AlignItems.Center
  AlignSelf.Baseline -> AlignItems.Baseline
  AlignSelf.Stretch -> AlignItems.Stretch
  else -> throw AssertionError()
}

internal fun FlexItem.measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec) {
  val (width, height) = measurable.measure(widthSpec, heightSpec)
  this.width = width.coerceIn(measurable.minWidth, measurable.maxWidth)
  this.height = height.coerceIn(measurable.minHeight, measurable.maxHeight)
}

internal fun FlexItem.layout(left: Double, top: Double, right: Double, bottom: Double) {
  this.left = left
  this.top = top
  this.right = right
  this.bottom = bottom
}

internal inline fun <T> List<T>.forEachIndices(block: (T) -> Unit) {
  for (index in indices) block(get(index))
}

/** The index of the last child included in this flex line or -1 if the flex line is empty. */
internal val FlexLine.lastIndex: Int
  get() = if (itemCount > 0) firstIndex + itemCount - 1 else -1

/** The range of item indices covered by this flex line. */
internal inline val FlexLine.indices: IntRange
  get() = firstIndex..lastIndex

/** The largest main size of all flex lines. */
internal fun List<FlexLine>.getLargestMainSize(): Double {
  return if (isEmpty()) 0.0 else maxOf { it.mainSize }
}

/** The sum of the cross sizes of all flex lines. */
internal fun List<FlexLine>.getSumOfCrossSize(): Double {
  return sumOf { it.crossSize }
}

internal fun MeasureSpec.Companion.getChildMeasureSpec(
  spec: MeasureSpec,
  padding: Double,
  childDimension: Double,
): MeasureSpec {
  val size = maxOf(0.0, spec.size - padding)
  var resultSize = 0.0
  var resultMode = MeasureSpecMode.Unspecified
  when (spec.mode) {
    MeasureSpecMode.Exactly -> if (childDimension >= 0) {
      resultSize = childDimension
      resultMode = MeasureSpecMode.Exactly
    } else if (childDimension == MatchParent) {
      // Child wants to be our size. So be it.
      resultSize = size
      resultMode = MeasureSpecMode.Exactly
    } else if (childDimension == WrapContent) {
      // Child wants to determine its own size. It can't be bigger than us.
      resultSize = size
      resultMode = MeasureSpecMode.AtMost
    }
    MeasureSpecMode.AtMost -> if (childDimension >= 0) {
      // Child wants a specific size... so be it.
      resultSize = childDimension
      resultMode = MeasureSpecMode.Exactly
    } else if (childDimension == MatchParent) {
      // Child wants to be our size, but our size is not fixed.
      // Constrain child to not be bigger than us.
      resultSize = size
      resultMode = MeasureSpecMode.AtMost
    } else if (childDimension == WrapContent) {
      // Child wants to determine its own size. It can't be bigger than us.
      resultSize = size
      resultMode = MeasureSpecMode.AtMost
    }
    MeasureSpecMode.Unspecified -> if (childDimension >= 0) {
      // Child wants a specific size... let them have it.
      resultSize = childDimension
      resultMode = MeasureSpecMode.Exactly
    } else if (childDimension == MatchParent) {
      // Child wants to be our size... find out how big it should be.
      resultSize = size
      resultMode = MeasureSpecMode.Unspecified
    } else if (childDimension == WrapContent) {
      // Child wants to determine its own size... find out how big it should be.
      resultSize = size
      resultMode = MeasureSpecMode.Unspecified
    }
    else -> throw AssertionError()
  }
  return from(resultSize, resultMode)
}
