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
package com.example.redwood.emojisearch.android.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.view.ViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.treehouse.EventListener
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseLayout
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import app.cash.zipline.loader.withDevelopmentServerPush
import com.example.redwood.emojisearch.launcher.EmojiSearchAppSpec
import com.example.redwood.emojisearch.treehouse.EmojiSearchPresenter
import com.example.redwood.emojisearch.widget.EmojiSearchProtocolNodeFactory
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path.Companion.toOkioPath

class EmojiSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)

  @SuppressLint("ResourceType")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val context = this
    val treehouseApp = createTreehouseApp()
    val treehouseContentSource = TreehouseContentSource(EmojiSearchPresenter::launch)

    val widgetSystem = TreehouseView.WidgetSystem { json, protocolMismatchHandler ->
      EmojiSearchProtocolNodeFactory(
        provider = EmojiSearchWidgetFactories(
          EmojiSearch = AndroidEmojiSearchWidgetFactory(context),
          RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context),
          RedwoodLazyLayout = ViewRedwoodLazyLayoutWidgetFactory(context),
        ),
        json = json,
        mismatchHandler = protocolMismatchHandler,
      )
    }

    setContentView(
      TreehouseLayout(this, widgetSystem, onBackPressedDispatcher).apply {
        treehouseContentSource.bindWhenReady(this, treehouseApp)
      },
    )
  }

  private fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val httpClient = OkHttpClient()
    val ziplineHttpClient = httpClient.asZiplineHttpClient()

    val treehouseAppFactory = TreehouseAppFactory(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
      eventListener = appEventListener,
      stateStore = FileStateStore(
        json = Json,
        fileSystem = FileSystem.SYSTEM,
        directory = applicationContext.getDir("TreehouseState", MODE_PRIVATE).toOkioPath(),
      ),
    )

    val manifestUrlFlow = flowOf("http://10.0.2.2:8080/manifest.zipline.json")
      .withDevelopmentServerPush(ziplineHttpClient)

    val treehouseApp = treehouseAppFactory.create(
      appScope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrl = manifestUrlFlow,
        hostApi = RealHostApi(this@EmojiSearchActivity, httpClient),
      ),
    )

    treehouseApp.start()

    return treehouseApp
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}

val appEventListener: EventListener = object : EventListener() {
  override fun codeLoadFailed(app: TreehouseApp<*>, manifestUrl: String?, exception: Exception, startValue: Any?) {
    Log.w("Treehouse", "codeLoadFailed", exception)
  }

  override fun codeLoadSuccess(app: TreehouseApp<*>, manifestUrl: String?, manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
    Log.i("Treehouse", "codeLoadSuccess")
  }
}
