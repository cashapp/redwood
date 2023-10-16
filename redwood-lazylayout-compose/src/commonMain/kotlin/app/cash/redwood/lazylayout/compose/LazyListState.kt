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
  return rememberSaveable(saver = LazyListState.Saver) {
    LazyListState()
  }
}

public class LazyListState {
  /** We only restore the scroll position once. */
  private var hasRestoredScrollPosition = false

  /** The scroll position to restore. */
  private var restoredIndex: Int = -1

  /**
   * The value published to the host platform. This starts as 0 and changes exactly once to
   * trigger exactly one scroll.
   */
  public var scrollItemIndex: Int by mutableStateOf(0)
    internal set

  private var firstVisibleItemIndex: Int = 0
  private var lastVisibleItemIndex: Int = 0

  public fun restoreIndex(index: Int) {
    require(index >= 0)

    if (this.restoredIndex != -1) return
    this.restoredIndex = index

    // Scroll to the target item.
    if (hasRestoredScrollPosition) {
      scrollItemIndex = restoredIndex
    }
  }

  public fun maybeRestoreScrollPosition() {
    if (this.hasRestoredScrollPosition) return
    this.hasRestoredScrollPosition = true

    // Scroll to the target item.
    if (restoredIndex != -1) {
      scrollItemIndex = restoredIndex
    }
  }

  public fun onScrolled(firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) {
    this.firstVisibleItemIndex = firstVisibleItemIndex
    this.lastVisibleItemIndex = lastVisibleItemIndex
  }

  public companion object {
    /**
     * The default [Saver] implementation for [LazyListState].
     */
    public val Saver: Saver<LazyListState, *> = Saver(
      save = { it.firstVisibleItemIndex },
      restore = {
        LazyListState().apply {
          restoreIndex(it)
        }
      },
    )
  }
}
