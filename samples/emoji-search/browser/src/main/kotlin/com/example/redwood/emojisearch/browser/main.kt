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
package com.example.redwood.emojisearch.browser

import androidx.compose.runtime.Composable
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.compose.WindowAnimationFrameClock
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.dom.HTMLElementRedwoodLayoutWidgetFactory
import app.cash.redwood.treehouse.lazylayout.widget.RedwoodTreehouseLazyLayoutWidgetFactory
import app.cash.redwood.widget.HTMLElementChildren
import com.example.redwood.emojisearch.presenter.ColumnProvider
import com.example.redwood.emojisearch.presenter.EmojiSearch
import com.example.redwood.emojisearch.presenter.HttpClient
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactories
import kotlin.js.json
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement
import org.w3c.dom.Window
import org.w3c.fetch.RequestInit

fun main() {
  val content = document.getElementById("content") as HTMLElement

  @OptIn(DelicateCoroutinesApi::class)
  val composition = RedwoodComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    container = HTMLElementChildren(content),
    provider = EmojiSearchWidgetFactories(
      EmojiSearch = HTMLElementEmojiSearchWidgetFactory(document),
      RedwoodLayout = HTMLElementRedwoodLayoutWidgetFactory(document),
      RedwoodTreehouseLazyLayout = object : RedwoodTreehouseLazyLayoutWidgetFactory<HTMLElement> {
        // For now we use a ColumnProvider to replace this with a normal Column.
        override fun LazyColumn() = throw UnsupportedOperationException()
      },
    ),
  )
  val httpClient = FetchHttpClient(window)
  composition.setContent {
    EmojiSearch(httpClient, TruncatingColumnProvider)
  }
}

private class FetchHttpClient(
  private val window: Window,
) : HttpClient {
  override suspend fun call(
    url: String,
    headers: Map<String, String>,
  ): String {
    val jsonHeaders = json()
    for ((key, value) in headers) {
      jsonHeaders[key] = value
    }
    return window.fetch(url, RequestInit(headers = jsonHeaders)).await().text().await()
  }
}

private object TruncatingColumnProvider : ColumnProvider {
  @Composable
  override fun <T> create(
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit,
  ) {
    Column {
      for (item in items.take(25)) {
        itemContent(item)
      }
    }
  }
}
