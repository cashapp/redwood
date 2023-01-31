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
package com.example.redwood.emojisearch.treehouse

import androidx.compose.runtime.Composable
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.redwood.treehouse.lazylayout.compose.LazyColumn
import app.cash.redwood.treehouse.lazylayout.compose.items
import com.example.redwood.emojisearch.compose.EmojiSearchProtocolBridge
import com.example.redwood.emojisearch.presenter.ColumnProvider
import com.example.redwood.emojisearch.presenter.EmojiSearch
import com.example.redwood.emojisearch.presenter.HttpClient
import kotlinx.serialization.json.Json

class RealEmojiSearchPresenter(
  private val hostApi: HostApi,
  private val json: Json,
) : EmojiSearchPresenter {
  override fun launch(): ZiplineTreehouseUi {
    val bridge = EmojiSearchProtocolBridge.create(json)
    val httpClient = HostHttpClient(hostApi)
    val lazyColumnProvider = LazyColumnProvider(bridge)
    val treehouseUi = object : TreehouseUi {
      @Composable
      override fun Show() {
        EmojiSearch(httpClient, lazyColumnProvider)
      }
    }
    return treehouseUi.asZiplineTreehouseUi(
      bridge = bridge,
      widgetVersion = 0U,
    )
  }
}

private class HostHttpClient(
  private val hostApi: HostApi,
) : HttpClient {
  override suspend fun call(url: String, headers: Map<String, String>): String {
    return hostApi.httpCall(url, headers)
  }
}

private class LazyColumnProvider(
  private val bridge: ProtocolBridge,
) : ColumnProvider {
  @Composable
  override fun <T> create(
    items: List<T>,
    placeholder: @Composable () -> Unit,
    itemContent: @Composable (item: T) -> Unit,
  ) {
    bridge.LazyColumn(placeholder) {
      items(items) { item ->
        itemContent(item)
      }
    }
  }
}
