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
package app.cash.zipline.samples.emojisearch.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseLauncher
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseWidgetView
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.samples.emojisearch.EmojiSearchAppSpec
import app.cash.zipline.samples.emojisearch.EmojiSearchPresenter
import example.schema.widget.DiffConsumingEmojiSearchWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class EmojiSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val treehouseApp = createTreehouseApp()
    val treehouseContent = TreehouseView.Content(EmojiSearchPresenter::launch)

    val widgetSystem = object : TreehouseView.WidgetSystem<EmojiSearchPresenter> {
      override fun widgetFactory(
        app: TreehouseApp<EmojiSearchPresenter>,
        json: Json,
        protocolMismatchHandler: ProtocolMismatchHandler,
      ): DiffConsumingWidget.Factory<*> {
        return DiffConsumingEmojiSearchWidgetFactory(
          delegate = AndroidViewEmojiSearchWidgetFactory(this@EmojiSearchActivity, treehouseApp),
          json = json,
          mismatchHandler = protocolMismatchHandler,
        )
      }
    }

    setContentView(
      TreehouseWidgetView(this, treehouseApp, widgetSystem).apply {
        setContent(treehouseContent)
      },
    )
  }

  private fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val httpClient = OkHttpClient()

    val treehouseLauncher = TreehouseLauncher(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
    )

    return treehouseLauncher.launch(
      scope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrlString = "http://10.0.2.2:8080/manifest.zipline.json",
        hostApi = RealHostApi(httpClient),
      ),
    )
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
