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

public interface LoadingStrategy {
  /**
   * Returns the index of the first item that is visible on screen. The item may be partially
   * visible.
   *
   * This is used to save the scroll position when the view is unloaded.
   *
   * This may temporarily be larger than the total number of items in the model. This will occur if
   * the number of items in the model shrinks.
   */
  public val firstVisibleIndex: Int

  /**
   * Returns the index of the last item that is visible on screen. The item may be partially
   * visible.
   *
   * This may temporarily be larger than the total number of items in the model. This will occur if
   * the number of items in the model shrinks.
   */
  public val lastVisibleIndex: Int

  /** Perform a programmatic scroll to [firstVisibleIndex]. */
  public fun scrollTo(firstVisibleIndex: Int)

  /** React to a user-initiated scroll to the target range. */
  public fun onUserScroll(firstVisibleIndex: Int, lastVisibleIndex: Int)

  /**
   * Returns the range of items to render into the view tree. This should be a slice of
   * `0..(itemCount - 1)`. It should cover the most-recently scrolled to `firstIndex..lastIndex`
   * range, plus any adjacent indexes to preload.
   */
  public fun loadRange(itemCount: Int): IntRange
}
