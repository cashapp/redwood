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

import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import app.cash.zipline.samples.emojisearch.EmojiSearchEvent.SearchTermEvent
import example.schema.compose.DiffProducingEmojiSearchWidgetFactory
import example.values.TextFieldState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RealEmojiSearchPresenter(
  private val hostApi: HostApi,
  private val json: Json,
) : EmojiSearchPresenter {
  private var emojis = listOf<EmojiImage>()
  private var latestSearchTerm = TextFieldState()

  private val loadingImage = EmojiImage(
    label = "watch",
    url = "https://github.githubassets.com/images/icons/emoji/unicode/231a.png?v8",
  )
  private val initialViewModel = EmojiSearchViewModel(latestSearchTerm, listOf(loadingImage))

  override fun launch(): ZiplineTreehouseUi {
    val events = MutableSharedFlow<EmojiSearchEvent>(extraBufferCapacity = Int.MAX_VALUE)
    val factory = DiffProducingEmojiSearchWidgetFactory(json)
    val treehouseUi = EmojiSearchTreehouseUi(
      initialViewModel = initialViewModel,
      viewModels = produceModels(events),
      onEvent = events::tryEmit,
      factory = factory,
    )
    return treehouseUi.asZiplineTreehouseUi(
      factory = factory,
      widgetVersion = 0U,
    )
  }

  private fun produceModels(
    events: Flow<EmojiSearchEvent>,
  ): Flow<EmojiSearchViewModel> {
    return channelFlow {
      loadEmojis()
      send(produceModel())

      events.collectLatest { event ->
        when (event) {
          is SearchTermEvent -> {
            if(event.searchTerm.userEditCount > latestSearchTerm.userEditCount) {
              println("!!!JS Setting latestSearchTerm to ${event.searchTerm}")
              latestSearchTerm = event.searchTerm
              send(produceModel())
            }
          }
        }
      }
    }
  }

  private suspend fun loadEmojis() {
    val emojisJson = hostApi.httpCall(
      url = "https://api.github.com/emojis",
      headers = mapOf("Accept" to "application/vnd.github.v3+json"),
    )
    val labelToUrl = Json.decodeFromString<Map<String, String>>(emojisJson)
    emojis = labelToUrl.map { (key, value) -> EmojiImage(key, value) }
  }

  private fun produceModel(): EmojiSearchViewModel {
    val searchTerms = latestSearchTerm.text.split(" ")
    val filteredImages = emojis
      .filter { image ->
        searchTerms.all { image.label.contains(it, ignoreCase = true) }
      }
    return EmojiSearchViewModel(latestSearchTerm, filteredImages)
  }
}
