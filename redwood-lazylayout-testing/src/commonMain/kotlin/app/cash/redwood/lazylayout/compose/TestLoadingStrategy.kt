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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A loading strategy that's appropriate for tests because it's simple and predictable.
 *
 * This is not suitable for production use, because it must show a placeholder before an item is
 * loaded.
 */
public class TestLoadingStrategy : LoadingStrategy {
  private var loadRange: IntRange by mutableStateOf(0..0)

  public override val firstVisibleIndex: Int
    get() = loadRange.first

  public override val lastVisibleIndex: Int
    get() = loadRange.last

  override fun scrollTo(firstVisibleIndex: Int) {
    require(firstVisibleIndex >= 0)
    loadRange = firstVisibleIndex..firstVisibleIndex + (loadRange.last - loadRange.first)
  }

  override fun onUserScroll(firstVisibleIndex: Int, lastVisibleIndex: Int) {
    loadRange = firstVisibleIndex..lastVisibleIndex
  }

  override fun loadRange(itemCount: Int): IntRange =
    loadRange.first.coerceIn(0, itemCount - 1)..loadRange.last.coerceIn(0, itemCount - 1)
}
