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
import app.cash.redwood.treehouse.StandardAppLifecycle
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.redwood.treehouse.lazylayout.api.LazyListInterval

internal class LazyListIntervalContent(
  private val appLifecycle: StandardAppLifecycle,
) : LazyListScope {
  val intervals = mutableListOf<LazyListInterval>()

  private class Item(
    private val appLifecycle: StandardAppLifecycle,
    private val content: @Composable (index: Int) -> Unit,
  ) : LazyListInterval.Item {

    override fun get(index: Int): ZiplineTreehouseUi {
      val treehouseUi = IndexedTreehouseUi(content, index)
      return treehouseUi.asZiplineTreehouseUi(appLifecycle)
    }
  }

  override fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    intervals += LazyListInterval(
      count,
      itemProvider = Item(appLifecycle, itemContent),
    )
  }

  override fun item(content: @Composable () -> Unit) {
    intervals += LazyListInterval(
      1,
      Item(appLifecycle) { content() },
    )
  }
}

private class IndexedTreehouseUi(
  private val content: @Composable (index: Int) -> Unit,
  private val index: Int,
) : TreehouseUi {
  @Composable
  override fun Show() {
    content(index)
  }
}
