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
package com.example.redwood.emojisearch.launcher

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.zipline.Zipline
import com.example.redwood.emojisearch.treehouse.EmojiSearchPresenter
import com.example.redwood.emojisearch.treehouse.HostApi
import com.example.redwood.emojisearch.treehouse.treehouseSerializersModule
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.modules.SerializersModule

class EmojiSearchAppSpec(
  manifestUrlString: String,
  private val hostApi: HostApi,
) : TreehouseApp.Spec<EmojiSearchPresenter>() {
  override val name = "emoji-search"
  override val manifestUrl = flowOf(manifestUrlString)

  override val serializersModule: SerializersModule
    get() = treehouseSerializersModule

  override fun bindServices(zipline: Zipline) {
    zipline.bind<HostApi>("HostApi", hostApi)
  }

  override fun create(zipline: Zipline): EmojiSearchPresenter {
    return zipline.take<EmojiSearchPresenter>("EmojiSearchPresenter")
  }
}
