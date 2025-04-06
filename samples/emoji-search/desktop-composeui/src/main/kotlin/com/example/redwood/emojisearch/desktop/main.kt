/*
 * Copyright (C) 2024 Square, Inc.
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
@file:JvmName("Main")

package com.example.redwood.emojisearch.desktop

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.redwood.composeui.RedwoodContent
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.basic.composeui.ComposeUiRedwoodUiBasicWidgetSystem
import app.cash.redwood.ui.basic.composeui.RedwoodUiBasicTheme
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import com.example.redwood.emojisearch.presenter.EmojiSearch
import okhttp3.OkHttpClient

fun main() {
  val client = OkHttpClient()
  val httpClient = JvmHttpClient(client)
  val imageLoader = ImageLoader.Builder(PlatformContext.INSTANCE)
    .serviceLoaderEnabled(false)
    .components {
      add(OkHttpNetworkFetcherFactory(client))
    }
    .build()
  val widgetSystem = ComposeUiRedwoodUiBasicWidgetSystem(imageLoader)

  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "Emoji Search",
    ) {
      RedwoodUiBasicTheme {
        Scaffold { contentPadding ->
          RedwoodContent(widgetSystem, modifier = Modifier.padding(contentPadding)) {
            EmojiSearch(
              httpClient = httpClient,
              navigator = DesktopNavigator,
              viewInsets = Margin.Zero,
            )
          }
        }
      }
    }
  }
}
