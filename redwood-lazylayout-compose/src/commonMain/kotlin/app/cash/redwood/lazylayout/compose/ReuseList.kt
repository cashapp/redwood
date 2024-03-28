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

/**
 * Execute [render] for each item in [items], while minimizing the amount of work the applier needs
 * to do when elements are added and removed.
 *
 * This uses Compose's [movableContentOf] to match inserted items to removed items, and will reuse
 * element subtrees where possible. This will also track moves of elements within the list, so long
 * as the key function matches.
 */
@Composable
public fun <T> ReuseList(
  items: List<T>,
  key: (T) -> Any,
  render: @Composable (T) -> Unit,
) {
  ReuseList(
    itemCount = items.size,
    key = { key(items[it]) },
    render = { render(items[it]) },
  )
}

@Composable
public fun ReuseList(
  itemCount: Int,
  key: (Int) -> Any,
  render: @Composable (Int) -> Unit,
) {
  val poolState = remember { mutableStateOf(listOf<PoolEntry>()) }

  val pool = rebuildPool(poolState.value, itemCount, key, render)
  poolState.value = pool

  // Render each item.
  for (i in 0 until itemCount) {
    pool[i].show(i)
  }
}

@Composable
private fun rebuildPool(
  pool: List<PoolEntry>,
  itemCount: Int,
  key: (Int) -> Any,
  render: @Composable (Int) -> Unit,
): List<PoolEntry> {
  // Mark all old entries as free.
  var freeEntryCount = pool.size
  for (entry in pool) {
    entry.free = true
  }

  val result = arrayOfNulls<PoolEntry>(itemCount)

  // Take entries from the pool that match by key.
  for (i in 0 until itemCount) {
    val itemKey = key(i)
    val entry = pool.firstOrNull { it.free && it.key == itemKey }
    if (entry != null) {
      entry.free = false
      result[i] = entry
      freeEntryCount--
    }
  }

  // Take entries from the pool that don't match. Allocate new entries when the pool is empty.
  var hits = 0
  var misses = 0
  for (i in 0 until itemCount) {
    var entry = result[i]

    if (entry == null) {
      if (freeEntryCount > 0) {
        hits++
        entry = pool.first { it.free }
        freeEntryCount--
        entry.free = false
        entry.key = key(i)
      } else {
        misses++
        entry = PoolEntry(
          key = key(i),
          render = render,
        )
      }
      result[i] = entry
    }
  }

  return result.map { it!! }
}

private class PoolEntry(
  var key: Any,
  render: @Composable (Int) -> Unit,
) {
  var free = false
  val show: @Composable (Int) -> Unit = movableContentOf(render)
}
