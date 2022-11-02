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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.zipline.samples.emojisearch.EmojiSearchEvent.SearchTermEvent
import example.schema.compose.Image
import example.schema.compose.Text
import example.schema.compose.TextInput
import kotlinx.coroutines.flow.Flow

class EmojiSearchTreehouseUi(
  private val initialViewModel: EmojiSearchViewModel,
  private val viewModels: Flow<EmojiSearchViewModel>,
  private val onEvent: (EmojiSearchEvent) -> Unit,
) : TreehouseUi {

  @Composable
  override fun Show() {
    val viewModel by viewModels.collectAsState(initialViewModel)

    Column(
      horizontalAlignment = CrossAxisAlignment.Stretch
    ) {
      TextInput(
        text = viewModel.searchTerm,
        hint = "Search",
        onTextChanged = { onEvent(SearchTermEvent(it)) },
      )
      Column(
        overflow = Overflow.Scroll,
        padding = Padding(horizontal = 40),
        horizontalAlignment = CrossAxisAlignment.Stretch,
      ) {
        for (image in viewModel.images) {
          Row(
            verticalAlignment = CrossAxisAlignment.Center,
          ) {
            Image(url = image.url)
            Text(text = image.label)
          }
        }
      }
    }
  }
}
