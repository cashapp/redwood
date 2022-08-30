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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NoLiveLiterals
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseLauncher
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.ViewBinder
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.composeui.TreehouseComposeView
import app.cash.zipline.loader.ManifestVerifier
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

    val treehouseApp = createTreehouseApp()

    val view = TreehouseComposeView<EmojiSearchPresenter>(
      context = this,
      treehouseApp = treehouseApp,
      widgetFactory = AndroidEmojiSearchWidgetFactory,
    )
    view.setContent(object : TreehouseView.Content<EmojiSearchPresenter> {
      override fun get(app: EmojiSearchPresenter): ZiplineTreehouseUi {
        return app.launch()
      }
    })

    setContentView(view)
  }

  private fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val httpClient = OkHttpClient()

    val treehouseLauncher = TreehouseLauncher(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
    )

    return treehouseLauncher.launch(
      scope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrlString = "http://10.0.2.2:8080/manifest.zipline.json",
        hostApi = RealHostApi(httpClient),
        viewBinder = object : ViewBinder {
          override fun widgetFactory(
            view: TreehouseView<*>,
            json: Json,
          ): DiffConsumingWidget.Factory<*> = DiffConsumingEmojiSearchWidgetFactory<@Composable () -> Unit>(
            delegate = (view as TreehouseComposeView<*>).widgetFactory as AndroidEmojiSearchWidgetFactory,
            json = json,
          )
        },
      ),
    )
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
