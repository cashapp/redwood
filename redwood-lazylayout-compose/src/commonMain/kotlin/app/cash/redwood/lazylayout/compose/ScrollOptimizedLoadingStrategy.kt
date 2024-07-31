/*
 * Copyright (C) 2024 Square, Inc.
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.lazylayout.api.ScrollItemIndex

private const val DEFAULT_PRELOAD_ITEM_COUNT = 15
private const val SCROLL_IN_PROGRESS_PRELOAD_ITEM_COUNT = 5
private const val PRIMARY_PRELOAD_ITEM_COUNT = 20
private const val SECONDARY_PRELOAD_ITEM_COUNT = 10

private const val DEFAULT_SCROLL_INDEX = -1

/**
 * A loading strategy that preloads items above and below the visible range.
 *
 * When scrolling, this loads more items in the direction the user is scrolling to.
 *
 * The size of the loading window is kept small while scrolling. It grows when scrolling stops.
 *
 * This will retain already-loaded items that it wouldn't load otherwise.
 */
public class ScrollOptimizedLoadingStrategy(
  private val defaultPreloadItemCount: Int = DEFAULT_PRELOAD_ITEM_COUNT,
  private val scrollInProgressPreloadItemCount: Int = SCROLL_IN_PROGRESS_PRELOAD_ITEM_COUNT,
  private val primaryPreloadItemCount: Int = PRIMARY_PRELOAD_ITEM_COUNT,
  private val secondaryPreloadItemCount: Int = SECONDARY_PRELOAD_ITEM_COUNT,
  private val preloadItems: Boolean = true,
) : LoadingStrategy {
  /**
   * Update this to trigger a programmatic scroll. This may be updated multiple times, including
   * when the previous scroll state is restored.
   */
  public var programmaticScrollIndex: ScrollItemIndex by mutableStateOf(
    ScrollItemIndex(id = 0, index = 0, animated = false),
  )
    private set

  /** Bounds of what the user is looking at. Everything else is placeholders! */
  public override var firstIndex: Int by mutableIntStateOf(0)
    private set
  public var lastIndex: Int by mutableIntStateOf(0)
    private set

  private var firstIndexFromPrevious1: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)
  private var firstIndexFromPrevious2: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)
  private var lastIndexFromPrevious1: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)

  private var beginFromPrevious1: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)
  private var endFromPrevious1: Int by mutableStateOf(DEFAULT_SCROLL_INDEX)

  override fun scrollTo(firstIndex: Int) {
    require(firstIndex >= 0)

    val delta = (lastIndex - this.firstIndex)
    this.firstIndex = firstIndex
    this.lastIndex = firstIndex + delta
  }

  override fun onUserScroll(firstIndex: Int, lastIndex: Int) {
    this.firstIndex = firstIndex
    this.lastIndex = lastIndex
  }

  public override fun loadRange(itemCount: Int): IntRange {
    // Ensure that the range includes `firstIndex` through `lastIndex`.
    var begin = firstIndex
    var end = lastIndex

    val isScrollingDown = firstIndexFromPrevious1 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious1 < firstIndex
    val isScrollingUp = firstIndexFromPrevious1 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious1 > firstIndex
    val hasStoppedScrolling = firstIndexFromPrevious2 != DEFAULT_SCROLL_INDEX && firstIndex == firstIndexFromPrevious1
    val wasScrollingDown = firstIndexFromPrevious1 > firstIndexFromPrevious2
    val wasScrollingUp = firstIndexFromPrevious1 < firstIndexFromPrevious2

    // Expand the range depending on scroll direction.
    when {
      // Ignore preloads.
      !preloadItems -> {
        // No-op
      }

      isScrollingDown -> {
        begin -= scrollInProgressPreloadItemCount
        end += primaryPreloadItemCount
      }

      isScrollingUp -> {
        begin -= primaryPreloadItemCount
        end += scrollInProgressPreloadItemCount
      }

      hasStoppedScrolling && wasScrollingDown -> {
        begin -= secondaryPreloadItemCount
        end += primaryPreloadItemCount
      }

      hasStoppedScrolling && wasScrollingUp -> {
        begin -= primaryPreloadItemCount
        end += secondaryPreloadItemCount
      }

      // New.
      else -> {
        end += defaultPreloadItemCount
      }
    }

    // On initial load, set lastIndex to the end of the loaded window.
    if (lastIndex == 0) {
      lastIndex = end
    }

    // If we're contiguous with the previous visible window,
    // don't rush to remove things from the previous range.
    if (beginFromPrevious1 != DEFAULT_SCROLL_INDEX &&
      endFromPrevious1 != DEFAULT_SCROLL_INDEX
    ) {
      // Case one: Contiguous scroll down
      if (begin in firstIndexFromPrevious1..lastIndexFromPrevious1) {
        begin = beginFromPrevious1
      }

      // Case two: Contiguous scroll up
      if (end in firstIndexFromPrevious1..lastIndexFromPrevious1) {
        end = endFromPrevious1
      }
    }

    begin = begin.coerceIn(0, itemCount)
    end = end.coerceIn(0, itemCount)

    this.firstIndexFromPrevious2 = firstIndexFromPrevious1
    this.firstIndexFromPrevious1 = firstIndex
    this.lastIndexFromPrevious1 = lastIndex

    this.beginFromPrevious1 = begin
    this.endFromPrevious1 = end

    return begin until end
  }
}
