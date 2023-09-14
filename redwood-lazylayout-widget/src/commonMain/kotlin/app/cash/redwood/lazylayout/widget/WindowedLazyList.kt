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

public abstract class WindowedLazyList<W : Any>(
  private val listUpdateCallback: ListUpdateCallback,
) : LazyList<W> {

  private var firstVisibleItemIndex = 0
  private var lastVisibleItemIndex = 0
  protected var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? = null

  final override val items: WindowedChildren<W> = WindowedChildren(listUpdateCallback)

  final override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  public fun updateViewport(firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) {
    if (firstVisibleItemIndex != this.firstVisibleItemIndex || lastVisibleItemIndex != this.lastVisibleItemIndex) {
      this.firstVisibleItemIndex = firstVisibleItemIndex
      this.lastVisibleItemIndex = lastVisibleItemIndex
      onViewportChanged?.invoke(firstVisibleItemIndex, lastVisibleItemIndex)
    }
  }

  override fun itemsBefore(itemsBefore: Int) {
    val delta = itemsBefore - items.itemsBefore
    items.itemsBefore = itemsBefore

    if (delta > 0) {
      listUpdateCallback.onInserted(itemsBefore - delta, delta)
    } else {
      listUpdateCallback.onRemoved(itemsBefore, -delta)
    }
  }

  final override fun itemsAfter(itemsAfter: Int) {
    val delta = itemsAfter - items.itemsAfter
    items.itemsAfter = itemsAfter

    val position = items.itemsBefore + items.items.size
    if (delta > 0) {
      listUpdateCallback.onInserted(position, delta)
    } else {
      listUpdateCallback.onRemoved(position, -delta)
    }
  }
}
