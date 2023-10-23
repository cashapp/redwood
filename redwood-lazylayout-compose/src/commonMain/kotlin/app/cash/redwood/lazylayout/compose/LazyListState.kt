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
  save = { it.firstIndex },
  restore = {
    LazyListState().apply {
      programmaticScroll(it)
    }
  },
)

public open class LazyListState {
  /**
   * Update this to trigger a programmatic scroll. Typically this is updated exactly once, when the
   * previous scroll state is restored.
   */
  public var programmaticScrollIndex: Int by mutableStateOf(0)
    private set

  /** Bounds of what the user is looking at. Everything else is placeholders! */
  public var firstIndex: Int by mutableStateOf(0)
    private set
  public var lastIndex: Int by mutableStateOf(0)
    private set

  /** Perform a programmatic scroll. */
  public fun programmaticScroll(index: Int) {
    require(index >= 0)
    require(programmaticScrollIndex == 0) { "unexpected double restoreIndex()" }

    this.programmaticScrollIndex = index

    val delta = (lastIndex - firstIndex)
    this.firstIndex = index
    this.lastIndex = index + delta
  }

  /** React to a user-initiated scroll. */
  public fun onUserScroll(firstIndex: Int, lastIndex: Int) {
    this.firstIndex = firstIndex
    this.lastIndex = lastIndex
  }
}
