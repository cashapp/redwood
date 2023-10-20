/*
 * Copyright (C) 2023 Square, Inc.
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
public fun rememberLazyListState(): LazyListState {
  return rememberSaveable(saver = saver) {
    LazyListState()
  }
}

/** The default [Saver] implementation for [LazyListState]. */
private val saver: Saver<LazyListState, *> = Saver(
  save = {
    println("**** saving index: ${it.indexToSave}")
    it.indexToSave
  },
  restore = {
    LazyListState().apply {
      println("**** from saver: restore index: $it")
      restoreIndex(it)
    }
  },
)

open public class LazyListState {

  /**
   * Every lazy list has a lifecycle approximately like this:
   *  - loading data (usually 0, or 1 rows)
   *  - loaded data (usually many rows)
   *
   * We only save and restore scroll positions when we have loaded data. That means we won't restore
   * at index 100 until there's 100 rows to scroll through, and we also won't save index 0 when
   * there isn't actually data loaded yet.
   *
   * This prevents us from clobbering the user's scroll position if the list recomposes while it's
   * still loading.
   */
  private var hasLoadedData = false

  /** The scroll position to restore. */
  private var restoredIndex: Int = -1

  /** If we haven't loaded data yet, save what was restored. */
  public var indexToSave: Int = -1
    private set

  /**
   * The value published to the host platform. This starts as 0 and changes exactly once to
   * trigger exactly one scroll.
   */
  public var scrollItemIndex: Int by mutableStateOf(0)
    internal set

  public var firstVisibleItemIndex: Int = 0
    private set

  public fun restoreIndex(index: Int) {
    println("**** restoreIndex: $index, this.restoredIndex: $restoredIndex, hasRestoredScrollPosition: $hasLoadedData")
    require(index >= 0)

    if (this.restoredIndex != -1) return // Idempotent.
    this.restoredIndex = index
    this.indexToSave = index

    // Scroll to the target item.
    if (hasLoadedData) {
      println("**** restoreIndex did scroll")
      scrollItemIndex = restoredIndex
    }
  }

  public fun maybeRestoreScrollPosition() {
    println("***** maybeRestoreScrollPosition: $hasLoadedData, $restoredIndex")
    //if (this.hasLoadedData) return // Idempotent.
    this.hasLoadedData = true

    // Scroll to the target item.
    if (restoredIndex != -1) {
      println("**** maybeRestoreScrollPosition did scroll")
      scrollItemIndex = restoredIndex
    }
  }

  public fun onScrolled(firstVisibleItemIndex: Int) {
    this.firstVisibleItemIndex = firstVisibleItemIndex

    if (hasLoadedData) {
      indexToSave = firstVisibleItemIndex
    }
  }
}
