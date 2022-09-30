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

internal fun packInt(first: Int, second: Int): Int {
  require(isUShort(first) && isUShort(second)) {
    "Invalid value: [$first, $second]"
  }
  return ((first.toUInt().toInt() and 0x3FFF) shl 16) or
    (second.toUInt().toInt() and 0x3FFF)
}

internal val Int.first: Int get() = (this shr 16) and 0x3FFF
internal val Int.second: Int get() = this and 0x3FFF

internal fun packLong(first: Int, second: Int, third: Int, fourth: Int): Long {
  require(isUShort(first) && isUShort(second) && isUShort(third) && isUShort(fourth)) {
    "Invalid value: [$first, $second, $third, $fourth]"
  }
  return ((first.toUInt().toLong() and 0x3FFF) shl 48) or
    ((second.toUInt().toLong() and 0x3FFF) shl 32) or
    ((third.toUInt().toLong() and 0x3FFF) shl 16) or
    (fourth.toUInt().toLong() and 0x3FFF)
}

internal val Long.first: Int get() = ((this shr 48) and 0x3FFF).toInt()
internal val Long.second: Int get() = ((this shr 32) and 0x3FFF).toInt()
internal val Long.third: Int get() = ((this shr 16) and 0x3FFF).toInt()
internal val Long.fourth: Int get() = (this and 0x3FFF).toInt()

private val MaxUShort = UShort.MAX_VALUE.toInt() // 65_535

private fun isUShort(value: Int): Boolean = value in 0..MaxUShort

/**
 * The number of children who are not invisible in this flex line.
 */
internal val FlexLine.itemCountVisible: Int
  get() = itemCount - invisibleItemCount

/**
 * Call [Measurable.measure] and update [FlexNode.measuredWidth] and [FlexNode.measuredHeight]
 * with the result.
 */
internal fun FlexNode.measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec) {
  measuredSize = measurable.measure(widthSpec, heightSpec)
}

internal val FlexNode.measuredWidth: Int get() = measuredSize.width
internal val FlexNode.measuredHeight: Int get() = measuredSize.height

internal fun MeasureSpec.Companion.getChildMeasureSpec(
  spec: MeasureSpec,
  padding: Int,
  childDimension: Int,
): MeasureSpec {
  val size = maxOf(0, spec.size - padding)
  var resultSize = 0
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
