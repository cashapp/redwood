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
package app.cash.redwood.lazylayout.widget

import app.cash.redwood.lazylayout.api.ScrollItemIndex

public abstract class LazyListScrollProcessor {
  /** To notify guest code of user scrolls. */
  private var onViewportChanged: ((Int, Int) -> Unit)? = null

  /** We can't scroll to this index until we have enough data for it to display! */
  private var deferredProgrammaticScrollIndex: Int = -1

  /** Once we receive a user scroll, we stop forwarding programmatic scrolls. */
  private var userHasScrolled = false

  /** De-duplicate calls to [onViewportChanged]. */
  private var mostRecentFirstIndex = -1
  private var mostRecentLastIndex = -1

  public fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  public fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
    // Defer until we have data in onEndChanges().
    deferredProgrammaticScrollIndex = scrollItemIndex.index
  }

  public fun onEndChanges() {
    // Do nothing: we don't have deferred scrolls.
    if (deferredProgrammaticScrollIndex == -1) return

    // Do nothing: we don't do programmatic scrolls if the user has already scrolled manually.
    if (userHasScrolled) return

    // Do nothing: we can't scroll to this item because it hasn't loaded yet!
    if (contentSize() <= deferredProgrammaticScrollIndex) return

    // Do a programmatic scroll!
    programmaticScroll(deferredProgrammaticScrollIndex)
    deferredProgrammaticScrollIndex = -1
  }

  /**
   * React to a user-initiated scroll. Callers should not call this function for programmatic
   * scrolls.
   */
  public fun onUserScroll(firstIndex: Int, lastIndex: Int) {
    if (firstIndex > 0) userHasScrolled = true

    if (firstIndex == mostRecentFirstIndex && lastIndex == mostRecentLastIndex) return

    this.mostRecentFirstIndex = firstIndex
    this.mostRecentLastIndex = lastIndex

    onViewportChanged?.invoke(firstIndex, lastIndex)
  }

  /** Returns the number of items we're scrolling over. */
  public abstract fun contentSize(): Int

  /** Perform a programmatic scroll. */
  public abstract fun programmaticScroll(firstIndex: Int)
}
