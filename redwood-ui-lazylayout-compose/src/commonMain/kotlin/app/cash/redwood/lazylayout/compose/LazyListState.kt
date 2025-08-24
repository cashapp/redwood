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
import app.cash.redwood.lazylayout.api.ScrollItemIndex

/**
 * Creates a [LazyListState] that is remembered across compositions.
 */
@Composable
public fun rememberLazyListState(
  strategy: LoadingStrategy = ScrollOptimizedLoadingStrategy(),
): LazyListState {
  return rememberSaveable(saver = saver) {
    LazyListState(strategy)
  }
}

/** The default [Saver] implementation for [LazyListState]. */
private val saver: Saver<LazyListState, *> = Saver(
  save = { it.strategy.firstVisibleIndex },
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
public open class LazyListState(
  public val strategy: LoadingStrategy = ScrollOptimizedLoadingStrategy(),
) {
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

  /** Perform a programmatic scroll. */
  public fun programmaticScroll(
    firstIndex: Int,
    animated: Boolean,
    clobberUserScroll: Boolean = true,
  ) {
    require(firstIndex >= 0)
    if (!clobberUserScroll && userScrolled) return

    strategy.scrollTo(firstIndex)

    val previous = programmaticScrollIndex
    this.programmaticScrollIndex = ScrollItemIndex(
      id = previous.id + 1,
      index = firstIndex,
      animated = animated,
    )
  }

  /** React to a user-initiated scroll. */
  public fun onUserScroll(firstIndex: Int, lastIndex: Int) {
    if (firstIndex > 0) {
      userScrolled = true
    }

    strategy.onUserScroll(firstIndex, lastIndex)
  }

  public fun loadRange(itemCount: Int): IntRange {
    return strategy.loadRange(itemCount)
  }
}
