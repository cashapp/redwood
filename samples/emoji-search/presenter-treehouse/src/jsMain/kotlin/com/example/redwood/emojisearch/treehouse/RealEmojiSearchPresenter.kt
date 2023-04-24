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

import app.cash.redwood.treehouse.StandardAppLifecycle
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import com.example.redwood.emojisearch.compose.EmojiSearchProtocolBridge
import com.example.redwood.emojisearch.presenter.EmojiSearchTreehouseUi
import kotlinx.serialization.json.Json

class RealEmojiSearchPresenter(
  private val hostApi: HostApi,
  json: Json,
) : EmojiSearchPresenter {
  override val appLifecycle = StandardAppLifecycle(
    protocolBridgeFactory = EmojiSearchProtocolBridge,
    json = json,
    widgetVersion = 0U,
  )

  override fun launch(): ZiplineTreehouseUi {
    val treehouseUi = EmojiSearchTreehouseUi(hostApi::httpCall, appLifecycle)
    return treehouseUi.asZiplineTreehouseUi(
      appLifecycle = appLifecycle,
    )
  }
}
