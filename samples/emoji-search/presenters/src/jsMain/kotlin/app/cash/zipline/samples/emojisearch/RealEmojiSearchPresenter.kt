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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalCoroutinesApi
class RealEmojiSearchPresenter(
  private val hostApi: HostApi,
) : EmojiSearchPresenter {
  private var imageIndex = listOf<EmojiImage>()
  private var latestSearchTerm = ""

  private val loadingImage = EmojiImage(
    "watch",
    "https://github.githubassets.com/images/icons/emoji/unicode/231a.png?v8",
  )

  override fun produceModels(
    events: Flow<EmojiSearchEvent>,
  ): Flow<EmojiSearchViewModel> {
    return channelFlow {
      send(EmojiSearchViewModel("", listOf(loadingImage)))

      loadImageIndex()
      send(produceModel())

      events.collectLatest { event ->
        when (event) {
          is EmojiSearchEvent.SearchTermEvent -> {
            latestSearchTerm = event.searchTerm
            send(produceModel())
          }
        }
      }
    }
  }

  private suspend fun loadImageIndex() {
    val emojisJson = hostApi.httpCall(
      url = "https://api.github.com/emojis",
      headers = mapOf("Accept" to "application/vnd.github.v3+json"),
    )
    val labelToUrl = Json.decodeFromString<Map<String, String>>(emojisJson)
    imageIndex = labelToUrl.map { (key, value) -> EmojiImage(key, value) }
  }

  private fun produceModel(): EmojiSearchViewModel {
    val filteredImages = imageIndex
      .filter { image ->
        latestSearchTerm.split(" ").all { it in image.label }
      }
      .take(25)
    return EmojiSearchViewModel(latestSearchTerm, filteredImages)
  }
}
