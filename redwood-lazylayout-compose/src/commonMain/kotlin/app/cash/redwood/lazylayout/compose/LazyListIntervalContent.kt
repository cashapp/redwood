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
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import app.cash.redwood.lazylayout.compose.layout.LazyLayoutIntervalContent
import app.cash.redwood.lazylayout.compose.layout.MutableIntervalList

internal class LazyListIntervalContent(
  content: LazyListScope.() -> Unit,
) : LazyLayoutIntervalContent<LazyListInterval>(), LazyListScope {
  override val intervals: MutableIntervalList<LazyListInterval> = MutableIntervalList()

  init {
    apply(content)
  }

  override fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    intervals.addInterval(
      count,
      LazyListInterval(
        item = itemContent,
      ),
    )
  }

  override fun item(content: @Composable () -> Unit) {
    intervals.addInterval(
      1,
      LazyListInterval(
        item = { content() },
      ),
    )
  }
}

internal data class LazyListInterval(
  val item: @Composable (index: Int) -> Unit,
) : LazyLayoutIntervalContent.Interval

internal class ItemPagingSource(
  private val scope: LazyListIntervalContent,
) : PagingSource<Int, @Composable () -> Unit>() {

  override suspend fun load(
    params: PagingSourceLoadParams<Int>,
  ): PagingSourceLoadResult<Int, @Composable () -> Unit> {
    val key = params.key ?: 0
    val limit = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
      else -> params.loadSize
    }.coerceAtMost(scope.itemCount)
    val offset = when (params) {
      is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
      else -> key
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

  override fun getRefreshKey(state: PagingState<Int, @Composable () -> Unit>) =
    state.anchorPosition?.let(state::closestPageToPosition)?.prevKey
}
