/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.widget

import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff

public interface Widget<T : Any> {
  public val value: T

  public fun apply(diff: PropertyDiff, events: (Event) -> Unit)

  public fun children(tag: Int): Children<T> {
    throw IllegalArgumentException("Widget does not support children")
  }

  public interface Children<T : Any> {
    public fun insert(index: Int, widget: T)
    public fun move(fromIndex: Int, toIndex: Int, count: Int)
    public fun remove(index: Int, count: Int)
    public fun clear()

    public companion object {
      public fun validateInsert(childCount: Int, index: Int) {
        if (index < 0 || index > childCount) {
          throw IndexOutOfBoundsException("index must be in range [0, $childCount]: $index")
        }
      }

      public fun validateMove(childCount: Int, fromIndex: Int, toIndex: Int, count: Int) {
        if (fromIndex < 0 || fromIndex >= childCount) {
          throw IndexOutOfBoundsException(
            "fromIndex must be in range [0, $childCount): $fromIndex"
          )
        }
        if (toIndex < 0 || toIndex > childCount) {
          throw IndexOutOfBoundsException(
            "toIndex must be in range [0, $childCount]: $toIndex"
          )
        }
        if (count < 0) {
          throw IndexOutOfBoundsException("count must be > 0: $count")
        }
        if (fromIndex + count > childCount) {
          throw IndexOutOfBoundsException(
            "count exceeds children: fromIndex=$fromIndex, count=$count, children=$childCount"
          )
        }
      }

      public fun validateRemove(childCount: Int, index: Int, count: Int) {
        if (index < 0 || index >= childCount) {
          throw IndexOutOfBoundsException("Index must be in range [0, $childCount): $index")
        }
        val toIndex = index + count
        if (toIndex < index || toIndex > childCount) {
          throw IndexOutOfBoundsException(
            "Count must be in range [0, ${childCount - index}): $count"
          )
        }
      }
    }
  }

  public interface Factory<T : Any> {
    public fun create(kind: Int, id: Long): Widget<T>
  }
}
