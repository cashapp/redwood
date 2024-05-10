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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.cash.redwood.lazylayout.api.ScrollItemIndex

private const val DEFAULT_PRELOAD_ITEM_COUNT = 15
private const val SCROLL_IN_PROGRESS_PRELOAD_ITEM_COUNT = 5
private const val PRIMARY_PRELOAD_ITEM_COUNT = 20
private const val SECONDARY_PRELOAD_ITEM_COUNT = 10

private const val DEFAULT_SCROLL_INDEX = -1

/**
 * Creates a [LazyListState] that is remembered across compositions.
 */
@Composable
public fun rememberLazyListState(): LazyListState {
  return rememberSaveable(saver = saver) {
    LazyListState()
  }
}

/** The default [Saver] implementation for [LazyListState]. */
private val saver: Saver<LazyListState, *> = Saver(
  save = { it.firstIndex },
  restore = {
    LazyListState().apply {
      programmaticScroll(firstIndex = it, animated = false, clobberUserScroll = false)
    }
  },
)

/**
 * A state object that can be hoisted to control and observe scrolling.
 *
 * In most cases, this will be created via [rememberLazyListState].
 */
public open class LazyListState {
  /**
   * Update this to trigger a programmatic scroll. This may be updated multiple times, including
   * when the previous scroll state is restored.
   */
  public var programmaticScrollIndex: ScrollItemIndex by mutableStateOf(
    ScrollItemIndex(id = 0, index = 0, animated = false),
  )
    private set

  /** Once we receive a user scroll, we limit which programmatic scrolls we apply. */
  private var userScrolled = false

  /** Bounds of what the user is looking at. Everything else is placeholders! */
  internal var firstIndex: Int by mutableIntStateOf(0)
  private var lastIndex: Int by mutableIntStateOf(0)

  internal var preloadItems: Boolean = true

  public var defaultPreloadItemCount: Int = DEFAULT_PRELOAD_ITEM_COUNT
  public var scrollInProgressPreloadItemCount: Int = SCROLL_IN_PROGRESS_PRELOAD_ITEM_COUNT
  public var primaryPreloadItemCount: Int = PRIMARY_PRELOAD_ITEM_COUNT
  public var secondaryPreloadItemCount: Int = SECONDARY_PRELOAD_ITEM_COUNT

  private var firstIndexFromPrevious1: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)
  private var firstIndexFromPrevious2: Int by mutableIntStateOf(DEFAULT_SCROLL_INDEX)

  /** Perform a programmatic scroll. */
  public fun programmaticScroll(
    firstIndex: Int,
    animated: Boolean,
    clobberUserScroll: Boolean = true,
  ) {
    require(firstIndex >= 0)
    if (!clobberUserScroll && userScrolled) return

    val previous = programmaticScrollIndex
    this.programmaticScrollIndex = ScrollItemIndex(
      id = previous.id + 1,
      index = firstIndex,
      animated = animated,
    )

    val delta = (lastIndex - this.firstIndex)
    this.firstIndex = firstIndex
    this.lastIndex = firstIndex + delta
  }

  /** React to a user-initiated scroll. */
  public fun onUserScroll(firstIndex: Int, lastIndex: Int) {
    if (firstIndex > 0) {
      userScrolled = true
    }

    this.firstIndex = firstIndex
    this.lastIndex = lastIndex
  }

  public fun loadRange(itemCount: Int): IntRange {
    val preloadBeforeItemCount: Int
    val preloadAfterItemCount: Int

    when {
      // Ignore preloads.
      !preloadItems -> {
        preloadBeforeItemCount = 0
        preloadAfterItemCount = 0
      }

      // Scrolling down.
      firstIndexFromPrevious1 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious1 < firstIndex -> {
        preloadBeforeItemCount = 0
        preloadAfterItemCount = scrollInProgressPreloadItemCount
      }

      // Scrolling up.
      firstIndexFromPrevious1 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious1 > firstIndex -> {
        preloadBeforeItemCount = scrollInProgressPreloadItemCount
        preloadAfterItemCount = 0
      }

      // Stopped scrolling down.
      firstIndexFromPrevious2 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious2 < firstIndex -> {
        preloadBeforeItemCount = secondaryPreloadItemCount
        preloadAfterItemCount = primaryPreloadItemCount
      }

      // Stopped scrolling up.
      firstIndexFromPrevious2 != DEFAULT_SCROLL_INDEX && firstIndexFromPrevious2 > firstIndex -> {
        preloadBeforeItemCount = primaryPreloadItemCount
        preloadAfterItemCount = secondaryPreloadItemCount
      }

      // New.
      else -> {
        preloadBeforeItemCount = defaultPreloadItemCount
        preloadAfterItemCount = defaultPreloadItemCount
      }
    }

    // TODO(dylan+jwilson): If we're contiguous with our previous loaded range,
    //  don't rush to remove things from the previous range.
    val begin = (firstIndex - preloadBeforeItemCount).coerceAtLeast(0)
    val end = (lastIndex + preloadAfterItemCount).coerceAtMost(itemCount).coerceAtLeast(0)

    this.firstIndexFromPrevious2 = firstIndexFromPrevious1
    this.firstIndexFromPrevious1 = firstIndex

    return begin until end
  }
}
