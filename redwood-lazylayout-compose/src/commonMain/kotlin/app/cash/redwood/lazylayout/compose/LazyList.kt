/*
 * Copyright (C) 2022 Square, Inc.
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
@file:JvmName("LazyList") // Conflicts with generated LazyList compose widget

package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlin.jvm.JvmName

@Composable
internal fun LazyList(
  isVertical: Boolean,
  content: LazyListScope.() -> Unit,
) {
  var itemPagingSource: ItemPagingSource? by remember { mutableStateOf(null) }
  val scope = LazyListIntervalContent(content)
  val pagerFlow = remember {
    // TODO Don't hardcode pageSizes
    // TODO Enable placeholder support
    // TODO Set a maxSize so we don't keep _too_ many views in memory
    val pager = Pager(PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false)) {
      itemPagingSource!!
    }
    pager.flow
  }
  val lazyPagingItems = pagerFlow.collectAsLazyPagingItems()
  DisposableEffect(scope) {
    itemPagingSource = ItemPagingSource(scope)
    onDispose {
      itemPagingSource?.invalidate()
    }
  }
  LazyList(
    isVertical,
    onPositionDisplayed = { position ->
      /** Triggers load at position, loading all items within [PagingConfig.prefetchDistance] of the [position]. */
      if (position < lazyPagingItems.itemCount) {
        lazyPagingItems[position]
      }
    },
    items = {
      repeat(lazyPagingItems.itemCount) { index ->
        // Only invokes Composable lambdas that are loaded.
        lazyPagingItems.peek(index)!!()
      }
    },
  )
}
