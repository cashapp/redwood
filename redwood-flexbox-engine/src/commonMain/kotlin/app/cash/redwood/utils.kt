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
package app.cash.redwood

import app.cash.redwood.Node.Companion.MatchParent
import app.cash.redwood.Node.Companion.WrapContent
import kotlin.math.max
import kotlin.math.min

internal fun packLong(lower: Int, higher: Int): Long {
  return (higher.toLong() shl 32) or (lower.toLong() and 0xFFFFFFFFL)
}

internal fun unpackLower(value: Long): Int {
  return value.toInt()
}

internal fun unpackHigher(value: Long): Int {
  return (value shr 32).toInt()
}

/**
 * The count of the views whose visibilities are not gone in this flex line.
 */
internal val Line.itemCountVisible: Int
  get() = itemCount - invisibleItemCount

/**
 * Returns true if the main axis is horizontal, false otherwise.
 */
internal val FlexDirection.isMainAxisHorizontal: Boolean
  get() = this == FlexDirection.Row || this == FlexDirection.RowReverse

internal fun MeasureSpec.Companion.getChildMeasureSpec(
  spec: MeasureSpec,
  padding: Int,
  childDimension: Int,
): MeasureSpec {
  val size = max(0, spec.size - padding)
  var resultSize = 0
  var resultMode = MeasureSpecMode.Unspecified
  when (val mode = spec.mode) {
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
      // Child wants a specific size... so be it
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
      // Child wants a specific size... let them have it
      resultSize = childDimension
      resultMode = MeasureSpecMode.Exactly
    } else if (childDimension == MatchParent) {
      // Child wants to be our size... find out how big it should be
      resultSize = size
      resultMode = MeasureSpecMode.Unspecified
    } else if (childDimension == WrapContent) {
      // Child wants to determine its own size.... find out how big it should be
      resultSize = size
      resultMode = MeasureSpecMode.Unspecified
    }
    else -> error("unknown MeasureSpecMode: $mode")
  }
  return from(resultSize, resultMode)
}

internal fun MeasureSpec.Companion.resolveSize(size: Int, measureSpec: MeasureSpec): Int {
  return when (val mode = measureSpec.mode) {
    MeasureSpecMode.AtMost -> min(measureSpec.size, size)
    MeasureSpecMode.Exactly -> measureSpec.size
    MeasureSpecMode.Unspecified -> size
    else -> error("unknown MeasureSpecMode: $mode")
  }
}
