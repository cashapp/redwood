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

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
public fun rememberLazyListState(
  initialFirstVisibleItemIndex: Int = 0,
): LazyListState {
  return rememberSaveable(saver = LazyListState.Saver) {
    LazyListState(
      initialFirstVisibleItemIndex,
    )
  }
}

public class LazyListState(
  firstVisibleItemIndex: Int = 0,
) {
  public var firstVisibleItemIndex: Int by mutableStateOf(firstVisibleItemIndex)
    internal set

  internal var scrollToItemTriggeredId by mutableStateOf(0)

  public fun scrollToItem(
    index: Int,
  ) {
    firstVisibleItemIndex = index
    scrollToItemTriggeredId++
  }
  public companion object {
    /**
     * The default [Saver] implementation for [LazyListState].
     */
    public val Saver: Saver<LazyListState, *> = Saver(
      save = { it.firstVisibleItemIndex },
      restore = {
        LazyListState(
          firstVisibleItemIndex = it,
        )
      }
    )
  }
}
