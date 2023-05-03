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
package com.example.redwood.emojisearch.presenter

import androidx.compose.runtime.Composable
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.lazylayout.compose.LazyColumn
import app.cash.redwood.treehouse.lazylayout.compose.items

class EmojiSearchTreehouseUi(
  private val httpClient: HttpClient,
) : TreehouseUi {
  private val lazyColumnProvider = LazyColumnProvider()

  @Composable
  override fun Show() {
    EmojiSearch(httpClient, lazyColumnProvider)
  }
}

private class LazyColumnProvider : ColumnProvider {
  @Composable
  override fun <T> create(
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit,
  ) {
    LazyColumn {
      items(items, itemContent)
    }
  }
}
