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
package app.cash.redwood.treehouse.lazylayout.paging

import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems
import app.cash.redwood.treehouse.lazylayout.compose.LazyListScope

/**
 * Adds the [LazyPagingItems] and their content to the scope. The range from 0 (inclusive) to
 * [LazyPagingItems.itemCount] (exclusive) always represents the full range of presentable items,
 * because every event from [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
public fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  itemContent: @Composable (value: T?) -> Unit,
) {
  items(items.itemCount) { index ->
    itemContent(items[index])
  }
}

/**
 * Adds the [LazyPagingItems] and their content to the scope where the content of an item is
 * aware of its local index. The range from 0 (inclusive) to [LazyPagingItems.itemCount] (exclusive)
 * always represents the full range of presentable items, because every event from
 * [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
public fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  itemContent: @Composable (index: Int, value: T?) -> Unit,
) {
  items(items.itemCount) { index ->
    itemContent(index, items[index])
  }
}
