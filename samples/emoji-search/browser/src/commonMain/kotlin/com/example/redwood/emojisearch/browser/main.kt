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

import androidx.compose.runtime.CompositionLocalProvider
import app.cash.redwood.compose.LocalUiConfiguration
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.compose.WindowAnimationFrameClock
import app.cash.redwood.layout.dom.HTMLElementRedwoodLayoutWidgetFactory
import app.cash.redwood.layout.dom.HTMLElementRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.HTMLElementChildren
import com.example.redwood.emojisearch.presenter.EmojiSearch
import com.example.redwood.emojisearch.presenter.HttpClient
import com.example.redwood.emojisearch.presenter.Navigator
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
  val navigator = object : Navigator {
    override fun openUrl(url: String) {
      window.open(url)
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  val composition = RedwoodComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    container = HTMLElementChildren(content),
    provider = EmojiSearchWidgetFactories(
      EmojiSearch = HTMLElementEmojiSearchWidgetFactory(document),
      RedwoodLayout = HTMLElementRedwoodLayoutWidgetFactory(document),
      RedwoodLazyLayout = HTMLElementRedwoodLazyLayoutWidgetFactory(document),
    ),
  )
  val httpClient = FetchHttpClient(window)
  composition.setContent {
    CompositionLocalProvider(LocalUiConfiguration provides UiConfiguration()) {
      EmojiSearch(httpClient, navigator)
    }
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
