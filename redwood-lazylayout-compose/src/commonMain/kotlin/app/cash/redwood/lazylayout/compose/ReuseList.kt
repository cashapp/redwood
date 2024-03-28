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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
public fun <T> ReuseList(
  items: List<T>,
  key: (T) -> Any,
  render: @Composable (T) -> Unit,
) {
  val poolState = remember { mutableStateOf(listOf<PoolEntry<T>>()) }
  val pool = poolState.value
  var freeEntryCount = pool.size

  val nextPool = arrayOfNulls<PoolEntry<T>>(items.size)

  // Find matches for items in the pool.
  for ((index, item) in items.withIndex()) {
    val itemKey = key(item)
    val entry = pool.firstOrNull { it.free && it.key == itemKey }
    if (entry != null) {
      nextPool[index] = entry
      freeEntryCount--
    }
  }

  // Render each item and prepare the next pool.
  for ((index, item) in items.withIndex()) {
    var entry = nextPool[index]

    if (entry == null) {
      if (freeEntryCount > 0) {
        entry = pool.first { it.free }
        freeEntryCount--
        entry.free = false
        entry.key = key(item)
      } else {
        entry = PoolEntry(
          key = key(item),
          render = render,
        )
      }
      nextPool[index] = entry
    }

    entry.Show(item)
  }

  // Get the next pool ready for use.
  for (reusableItem in nextPool) {
    reusableItem!!.free = true
  }
  poolState.value = nextPool.map { it!! }
}

private class PoolEntry<T>(
  var key: Any,
  render: @Composable (T) -> Unit,
) {
  var free = false
  val Show: @Composable (T) -> Unit = movableContentOf(render)
}
