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

internal interface WindowedItemList<T : Any> : List<T?> {
  var itemsBefore: Int
  var itemsAfter: Int
  val items: MutableList<T>
}

internal class WindowedItemListImpl<T : Any> : WindowedItemList<T>, AbstractList<T?>() {
  override var itemsBefore: Int = 0
  override var itemsAfter: Int = 0
  override val items: MutableList<T> = mutableListOf()

  override val size: Int
    get() = itemsBefore + items.size + itemsAfter

  override fun get(index: Int): T? {
    return when (index) {
      in 0 until itemsBefore -> null
      in itemsBefore until (itemsBefore + items.size) -> {
        items[index - itemsBefore]
      }
      in (itemsBefore + items.size) until size -> null
      else -> throw IndexOutOfBoundsException(
        "Illegal attempt to access index $index in WindowedItemList of size $size",
      )
    }
  }
}
