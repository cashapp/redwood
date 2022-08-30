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

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.ViewBinder
import app.cash.zipline.Zipline
import kotlinx.coroutines.flow.flowOf

class EmojiSearchAppSpec(
  manifestUrlString: String,
  private val hostApi: HostApi,
  override val viewBinder: ViewBinder,
) : TreehouseApp.Spec<EmojiSearchPresenter>() {
  override val name = "emoji-search"
  override val manifestUrl = flowOf(manifestUrlString)

  override fun bindServices(zipline: Zipline) {
    zipline.bind<HostApi>("HostApi", hostApi)
  }

  override fun create(zipline: Zipline): EmojiSearchPresenter {
    return zipline.take<EmojiSearchPresenter>("EmojiSearchPresenter")
  }
}
