/*
 * Copyright 2021 The Android Open Source Project
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
package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.redwood.lazylayout.compose.layout.LazyLayoutIntervalContent
import app.cash.redwood.lazylayout.compose.layout.MutableIntervalList

// Copied from https://github.com/androidx/androidx/blob/5c0f6611fe87e4ed29b1e5881e084581283169c1/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/lazy/LazyListIntervalContent.kt
// Removed support for keys, content types, and sticky headers.

internal class LazyListIntervalContent(
  content: LazyListScope.() -> Unit,
) : LazyLayoutIntervalContent<LazyListInterval>(), LazyListScope {
  override val intervals: MutableIntervalList<LazyListInterval> = MutableIntervalList()

  init {
    apply(content)
  }

  override fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    intervals.addInterval(
      count,
      LazyListInterval(
        item = itemContent,
      ),
    )
  }

  override fun item(content: @Composable () -> Unit) {
    intervals.addInterval(
      1,
      LazyListInterval(
        item = { content() },
      ),
    )
  }
}

internal class LazyListInterval(
  val item: @Composable (index: Int) -> Unit,
) : LazyLayoutIntervalContent.Interval
