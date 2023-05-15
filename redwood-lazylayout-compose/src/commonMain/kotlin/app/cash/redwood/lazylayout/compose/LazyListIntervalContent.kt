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
package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState

internal class LazyListIntervalContent(
  content: LazyListScope.() -> Unit,
) : LazyListScope {
  val intervals = mutableListOf<LazyListInterval>()

  init {
    apply(content)
  }

  override fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    intervals += LazyListInterval(
      count,
      itemContent = itemContent,
    )
  }

  override fun item(content: @Composable () -> Unit) {
    intervals += LazyListInterval(
      1,
      itemContent = { content() },
    )
  }
}

internal data class LazyListInterval(
  val count: Int,
  val itemContent: @Composable (index: Int) -> Unit,
)

internal class ItemPagingSource(
  private val scope: LazyListIntervalContent,
) : PagingSource<Int, @Composable () -> Unit>() {

  override suspend fun load(
    params: PagingSourceLoadParams<Int>,
  ): PagingSourceLoadResult<Int, @Composable () -> Unit> {
    val key = params.key ?: 0
    val count = scope.intervals.sumOf { it.count }
    val limit = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
      is PagingSourceLoadParamsRefresh<*> -> key + params.loadSize
      else -> params.loadSize
    }.coerceAtMost(count)
    val offset = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
      is PagingSourceLoadParamsAppend<*> -> key
      is PagingSourceLoadParamsRefresh<*> -> 0
      else -> error("Shouldn't happen")
    }
    val nextPosToLoad = offset + limit
    val loadResult: PagingSourceLoadResultPage<Int, @Composable () -> Unit> = PagingSourceLoadResultPage(
      data = List(if (nextPosToLoad <= count) limit else count - offset) { index ->
        var interval = IndexedValue(index + offset, scope.intervals.first())
        for (nextInterval in scope.intervals.drop(1)) {
          if (interval.index < interval.value.count) break
          interval = IndexedValue(interval.index - interval.value.count, nextInterval)
        }
        { interval.value.itemContent.invoke(interval.index) }
      },
      prevKey = offset.takeIf { it > 0 && limit > 0 },
      nextKey = nextPosToLoad.takeIf { limit > 0 && it < count },
      itemsBefore = offset,
      itemsAfter = maxOf(0, count - nextPosToLoad),
    )

    return (if (invalid) PagingSourceLoadResultInvalid<Int, @Composable () -> Unit>() else loadResult) as PagingSourceLoadResult<Int, @Composable () -> Unit>
  }

  override fun getRefreshKey(state: PagingState<Int, @Composable () -> Unit>): Int = state.pages.sumOf { it.data.size }
}
