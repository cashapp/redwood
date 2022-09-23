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

import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseLauncher
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.ViewBinder
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import example.schema.widget.DiffConsumingEmojiSearchWidgetFactory
import example.schema.widget.EmojiSearchWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.serialization.json.Json
import platform.Foundation.NSURLSession
import platform.UIKit.UIView

class EmojiSearchLauncher(
  private val nsurlSession: NSURLSession,
  private val hostApi: HostApi,
  private val widgetFactory: EmojiSearchWidgetFactory<UIView>,
) {
  private val coroutineScope: CoroutineScope = MainScope()
  private val manifestUrl = "http://localhost:8080/manifest.zipline.json"

  public fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val treehouseLauncher = TreehouseLauncher(
      httpClient = nsurlSession.asZiplineHttpClient(),
      manifestVerifier = ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
    )

    return treehouseLauncher.launch(
      scope = coroutineScope,
      spec = EmojiSearchAppSpec(
        manifestUrlString = manifestUrl,
        hostApi = hostApi,
        viewBinder = object : ViewBinder {
          override fun widgetFactory(
            view: TreehouseView<*>,
            json: Json,
          ): DiffConsumingWidget.Factory<*> = DiffConsumingEmojiSearchWidgetFactory<UIView>(
            delegate = widgetFactory,
            json = json,
          )
        },
      ),
    )
  }
}
