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
package app.cash.redwood.treehouse.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.redwood.LayoutScopeMarker
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent

@Composable
public fun ProtocolBridge.LazyColumn(
  placeholder: @Composable () -> Unit,
  content: LazyListScope.() -> Unit,
) {
  val widgetVersion = LocalWidgetVersion.current
  val scope = TreehouseLazyListScope(this, widgetVersion)
  content(scope)
  LazyColumn(
    intervals = scope.intervals,
    placeholder = placeholder,
  )
}

@LayoutScopeMarker
public interface LazyListScope {
  public fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  )
}

public inline fun <T> LazyListScope.items(
  items: List<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
) {
  items(items.size) {
    itemContent(items[it])
  }
}

private class TreehouseLazyListScope(
  private val provider: ProtocolBridge,
  private val widgetVersion: UInt,
) : LazyListScope {
  val intervals = mutableListOf<LazyListIntervalContent>()

  private class Item(
    private val provider: ProtocolBridge,
    private val widgetVersion: UInt,
    private val content: @Composable (index: Int) -> Unit,
  ) : LazyListIntervalContent.Item {

    override fun get(index: Int): ZiplineTreehouseUi {
      val treehouseUi = IndexedTreehouseUi(content, index)
      return treehouseUi.asZiplineTreehouseUi(provider, widgetVersion)
    }
  }

  override fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  ) {
    intervals += LazyListIntervalContent(
      count,
      itemProvider = Item(provider, widgetVersion, itemContent),
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
