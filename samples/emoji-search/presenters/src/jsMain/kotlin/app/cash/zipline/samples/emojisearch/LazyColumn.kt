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
package app.cash.zipline.samples.emojisearch

import androidx.compose.runtime.Composable
import app.cash.redwood.protocol.compose.DiffProducingWidget
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent

interface DiffProducingWidgetFactoryProvider {
  val factory: DiffProducingWidget.Factory
}

@Composable
fun DiffProducingWidgetFactoryProvider.LazyColumn(content: LazyListScope.() -> Unit) {
  val lazyListScope = LazyListScope(factory)
  content(lazyListScope)
  app.cash.redwood.treehouse.lazylayout.compose.LazyColumn(lazyListScope.intervals)
}

class LazyListScope(private val factory: DiffProducingWidget.Factory) {
  private val _intervals = mutableListOf<LazyListIntervalContent>()
  val intervals: List<LazyListIntervalContent> = _intervals

  private class Item(
    private val factory: DiffProducingWidget.Factory,
    private val content: @Composable (index: Int) -> Unit,
  ) : LazyListIntervalContent.Item {

    override fun get(index: Int): ZiplineTreehouseUi {
      val treehouseUi = object : TreehouseUi {
        @Composable
        override fun Show() {
          content(index)
        }
      }
      return treehouseUi.asZiplineTreehouseUi(factory, widgetVersion = 1u)
    }
  }

  fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    _intervals += LazyListIntervalContent(
      count,
      itemProvider = Item(factory, itemContent),
    )
  }
}

inline fun <T> LazyListScope.items(
  items: List<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
) = items(
  count = items.size,
) {
  itemContent(items[it])
}
