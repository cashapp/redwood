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
package app.cash.redwood.treehouse.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.redwood.LayoutScopeMarker
import app.cash.redwood.protocol.compose.ProtocolBridge

@LayoutScopeMarker
public interface LazyListScope {
  public fun item(
    content: @Composable () -> Unit,
  )

  public fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  )
}

public inline fun <T> LazyListScope.items(
  items: List<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
): Unit = items(
  count = items.size,
) {
  itemContent(items[it])
}

public inline fun <T> LazyListScope.itemsIndexed(
  items: List<T>,
  crossinline itemContent: @Composable (index: Int, item: T) -> Unit,
): Unit = items(
  count = items.size,
) {
  itemContent(it, items[it])
}

public inline fun <T> LazyListScope.items(
  items: Array<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
): Unit = items(
  count = items.size,
) {
  itemContent(items[it])
}

public inline fun <T> LazyListScope.itemsIndexed(
  items: Array<T>,
  crossinline itemContent: @Composable (index: Int, item: T) -> Unit,
): Unit = items(
  count = items.size,
) {
  itemContent(it, items[it])
}

@Composable
public fun LazyRow(
  bridge: ProtocolBridge,
  content: LazyListScope.() -> Unit,
) {
  LazyList(
    bridge = bridge,
    isVertical = false,
    content = content,
  )
}

@Composable
public fun LazyColumn(
  bridge: ProtocolBridge,
  content: LazyListScope.() -> Unit,
) {
  LazyList(
    bridge = bridge,
    isVertical = true,
    content = content,
  )
}
