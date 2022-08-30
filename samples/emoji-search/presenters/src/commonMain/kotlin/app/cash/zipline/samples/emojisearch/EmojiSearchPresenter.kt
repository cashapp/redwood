/*
 * Copyright (C) 2021 Square, Inc.
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
import app.cash.zipline.ZiplineService
import app.cash.zipline.samples.emojisearch.EmojiSearchEvent.SearchTermEvent
import example.schema.compose.Image
import example.schema.compose.ScrollableColumn
import example.schema.compose.TextInput
import kotlinx.coroutines.flow.Flow

interface EmojiSearchPresenter : ZiplineService {
  fun produceModels(
    events: Flow<EmojiSearchEvent>,
  ): Flow<EmojiSearchViewModel>
}

@Composable
fun EmojiSearch(
  viewModel: EmojiSearchViewModel,
  onEvent: (EmojiSearchEvent) -> Unit,
) {
  TextInput(
    text = viewModel.searchTerm,
    hint = "Search",
    onTextChanged = { onEvent(SearchTermEvent(it)) },
  )
  ScrollableColumn {
    for (image in viewModel.images) {
      Image(
        url = image.url,
        label = image.label,
      )
    }
  }
}
