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

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.NoLiveLiterals
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.redwood.treehouse.*
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.samples.emojisearch.EmojiSearchAppSpec
import app.cash.zipline.samples.emojisearch.EmojiSearchPresenter
import example.schema.widget.DiffConsumingEmojiSearchWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

@NoLiveLiterals
class EmojiSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val treehouseApp = createTreehouseApp(this)
    val treehouseContent = TreehouseView.Content(EmojiSearchPresenter::launch)

    setContentView(
      TreehouseWidgetView(this, treehouseApp).apply {
        setContent(treehouseContent)
      }
    )
  }

  private fun createTreehouseApp(activity: Activity): TreehouseApp<EmojiSearchPresenter> {
    val httpClient = OkHttpClient()

    val treehouseLauncher = TreehouseLauncher(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
    )

    var widgetFactory: AndroidViewEmojiSearchWidgetFactory<*>? = null
    val treehouseApp = treehouseLauncher.launch(
      scope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrlString = "http://10.0.2.2:8080/manifest.zipline.json",
        hostApi = RealHostApi(httpClient),
        viewBinder = object : ViewBinder {
          override fun widgetFactory(
            view: TreehouseView<*>,
            json: Json,
            mismatchHandler: ProtocolMismatchHandler,
          ) = DiffConsumingEmojiSearchWidgetFactory(
            delegate = widgetFactory!!,
            json = json,
            mismatchHandler = mismatchHandler,
          )
        },
      ),
    )
    widgetFactory = AndroidViewEmojiSearchWidgetFactory(activity, treehouseApp)
    return treehouseApp
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
