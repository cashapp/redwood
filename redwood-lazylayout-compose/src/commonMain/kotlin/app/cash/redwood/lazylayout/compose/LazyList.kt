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
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.redwood.LayoutModifier
import kotlin.jvm.JvmName

@Composable
private fun lazyPagingItems(
  content: LazyListScope.() -> Unit,
): LazyPagingItems<@Composable () -> Unit> {
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
  return lazyPagingItems
}

@Composable
internal fun LazyList(
  isVertical: Boolean,
  layoutModifier: LayoutModifier = LayoutModifier,
  content: LazyListScope.() -> Unit,
) {
  val lazyPagingItems = lazyPagingItems(content)
  LazyList(
    isVertical,
    onPositionDisplayed = { position ->
      /** Triggers load at position, loading all items within [PagingConfig.prefetchDistance] of the [position]. */
      if (position < lazyPagingItems.itemCount) {
        lazyPagingItems[position]
      }
    },
    layoutModifier = layoutModifier,
    items = {
      repeat(lazyPagingItems.itemCount) { index ->
        // Only invokes Composable lambdas that are loaded.
        lazyPagingItems.peek(index)!!()
      }
    },
  )
}

@Composable
internal fun RefreshableLazyList(
  isVertical: Boolean,
  refreshing: Boolean = false,
  onRefresh: (() -> Unit)? = null,
  layoutModifier: LayoutModifier = LayoutModifier,
  content: LazyListScope.() -> Unit,
) {
  val lazyPagingItems = lazyPagingItems(content)
  RefreshableLazyList(
    isVertical,
    onPositionDisplayed = { position ->
      /** Triggers load at position, loading all items within [PagingConfig.prefetchDistance] of the [position]. */
      if (position < lazyPagingItems.itemCount) {
        lazyPagingItems[position]
      }
    },
    refreshing = refreshing,
    onRefresh = onRefresh,
    layoutModifier = layoutModifier,
    items = {
      repeat(lazyPagingItems.itemCount) { index ->
        // Only invokes Composable lambdas that are loaded.
        lazyPagingItems.peek(index)!!()
      }
    },
  )
}

private class ItemPagingSource(
  private val scope: LazyListIntervalContent,
) : PagingSource<Int, @Composable () -> Unit>() {

  override suspend fun load(
    params: PagingSourceLoadParams<Int>,
  ): PagingSourceLoadResult<Int, @Composable () -> Unit> {
    val key = params.key ?: 0
    val limit = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
      is PagingSourceLoadParamsRefresh<*> -> key + params.loadSize
      else -> params.loadSize
    }.coerceAtMost(scope.itemCount)
    val offset = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
      is PagingSourceLoadParamsAppend<*> -> key
      is PagingSourceLoadParamsRefresh<*> -> 0
      else -> error("Shouldn't happen")
    }
    val nextPosToLoad = offset + limit
    val loadResult: PagingSourceLoadResultPage<Int, @Composable () -> Unit> = PagingSourceLoadResultPage(
      data = List(if (nextPosToLoad <= scope.itemCount) limit else scope.itemCount - offset) { index ->
        scope.withInterval(index + offset) { localIntervalIndex, content ->
          { content.item.invoke(localIntervalIndex) }
        }
      },
      prevKey = offset.takeIf { it > 0 && limit > 0 },
      nextKey = nextPosToLoad.takeIf { limit > 0 && it < scope.itemCount },
      itemsBefore = offset,
      itemsAfter = maxOf(0, scope.itemCount - nextPosToLoad),
    )

    return (if (invalid) PagingSourceLoadResultInvalid<Int, @Composable () -> Unit>() else loadResult) as PagingSourceLoadResult<Int, @Composable () -> Unit>
  }

  override fun getRefreshKey(state: PagingState<Int, @Composable () -> Unit>): Int = state.pages.sumOf { it.data.size }
}
