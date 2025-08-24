/*
 * Copyright 2023 The Android Open Source Project
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
package app.cash.redwood.lazylayout.compose.layout

// Copied from https://github.com/androidx/androidx/blob/5c0f6611fe87e4ed29b1e5881e084581283169c1/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/lazy/layout/LazyLayoutIntervalContent.kt
// Removed support for keys and content types.

/**
 * Common parts backing the interval-based content of lazy layout defined through `item` DSL.
 */
internal abstract class LazyLayoutIntervalContent<Interval : LazyLayoutIntervalContent.Interval> {
  abstract val intervals: IntervalList<Interval>

  /**
   * The total amount of items in all the intervals.
   */
  val itemCount: Int get() = intervals.size

  /**
   * Runs a [block] on the content of the interval associated with the provided [globalIndex]
   * with providing a local index in the given interval.
   */
  inline fun <T> withInterval(
    globalIndex: Int,
    block: (localIntervalIndex: Int, content: Interval) -> T
  ): T {
    val interval = intervals[globalIndex]
    val localIntervalIndex = globalIndex - interval.startIndex
    return block(localIntervalIndex, interval.value)
  }

  /**
   * Common content of individual intervals in `item` DSL of lazy layouts.
   */
  interface Interval
}
