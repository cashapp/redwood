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

import app.cash.redwood.treehouse.StandardFrameClockService
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import com.example.redwood.emojisearch.compose.EmojiSearchProtocolBridge
import com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi
import com.example.redwood.emojisearch.presenter.HttpClient
import kotlinx.serialization.json.Json

class RealEmojiSearchPresenter(
  private val hostApi: HostApi,
  private val json: Json,
) : EmojiSearchPresenter {
  override val frameClockService = StandardFrameClockService

  override fun launch(): ZiplineTreehouseUi {
    val bridge = EmojiSearchProtocolBridge.create(json)
    val treehouseUi = EmojiSearchTreehouseUi(
      httpClient = object : HttpClient {
        override suspend fun call(url: String, headers: Map<String, String>): String {
          return hostApi.httpCall(url, headers)
        }
      },
      bridge = bridge
    )
    return treehouseUi.asZiplineTreehouseUi(
      bridge = bridge,
      widgetVersion = 0U,
    )
  }
}
