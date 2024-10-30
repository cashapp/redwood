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
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.view.ViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.treehouse.Crashed
import app.cash.redwood.treehouse.DynamicContentWidgetFactory
import app.cash.redwood.treehouse.EventListener
import app.cash.redwood.treehouse.Loading
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseLayout
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import app.cash.zipline.loader.withDevelopmentServerPush
import com.example.redwood.emojisearch.launcher.EmojiSearchAppSpec
import com.example.redwood.emojisearch.protocol.host.EmojiSearchProtocolFactory
import com.example.redwood.emojisearch.treehouse.EmojiSearchPresenter
import com.example.redwood.emojisearch.treehouse.emojiSearchSerializersModule
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetSystem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.assetfilesystem.asFileSystem

class EmojiSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)
  private lateinit var treehouseLayout: TreehouseLayout

  private val leakDetector = LeakDetector.timeBasedIn(
    scope = scope,
    timeSource = TimeSource.Monotonic,
    leakThreshold = 10.seconds,
    callback = { reference, note ->
      Log.e("LEAK", "Leak detected! $reference $note")
    },
  )

  @SuppressLint("ResourceType")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val context = this
    val treehouseApp = createTreehouseApp()
    val treehouseContentSource = TreehouseContentSource(EmojiSearchPresenter::launch)

    val widgetSystem = WidgetSystem { json, protocolMismatchHandler ->
      EmojiSearchProtocolFactory(
        widgetSystem = EmojiSearchWidgetSystem(
          EmojiSearch = AndroidEmojiSearchWidgetFactory(context),
          RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context),
          RedwoodLazyLayout = ViewRedwoodLazyLayoutWidgetFactory(context),
        ),
        json = json,
        mismatchHandler = protocolMismatchHandler,
      )
    }

    treehouseLayout = TreehouseLayout(
      context = this,
      widgetSystem = widgetSystem,
      androidOnBackPressedDispatcher = onBackPressedDispatcher,
      dynamicContentWidgetFactory = EmojiSearchDynamicContentWidgetFactory(context),
    ).apply {
      treehouseContentSource.bindWhenReady(this, treehouseApp)
    }
    setContentView(treehouseLayout)
  }

  class EmojiSearchDynamicContentWidgetFactory(
    private val context: Context,
  ) : DynamicContentWidgetFactory<View> {
    override fun Loading(): Loading<View> = LoadingView(context)
    override fun Crashed(): Crashed<View> = ExceptionView(context)
  }

  private val appEventListener: EventListener = object : EventListener() {
    private var success = true
    private var snackbar: Snackbar? = null

    override fun uncaughtException(exception: Throwable) {
      Log.e("Treehouse", "uncaughtException", exception)
    }

    override fun codeLoadFailed(exception: Exception, startValue: Any?) {
      Log.w("Treehouse", "codeLoadFailed", exception)
      if (success) {
        // Only show the Snackbar on the first transition from success.
        success = false
        snackbar =
          Snackbar.make(treehouseLayout, "Unable to load guest code from server", LENGTH_INDEFINITE)
            .setAction("Dismiss") { maybeDismissSnackbar() }
            .also(Snackbar::show)
      }
    }

    override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
      Log.i("Treehouse", "codeLoadSuccess")
      success = true
      maybeDismissSnackbar()
    }

    private fun maybeDismissSnackbar() {
      snackbar?.let {
        it.dismiss()
        snackbar = null
      }
    }
  }

  private fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val httpClient = OkHttpClient()
    val ziplineHttpClient = httpClient.asZiplineHttpClient()

    val treehouseAppFactory = TreehouseAppFactory(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
      embeddedFileSystem = applicationContext.assets.asFileSystem(),
      embeddedDir = "/".toPath(),
      stateStore = FileStateStore(
        json = Json {
          useArrayPolymorphism = true
          serializersModule = emojiSearchSerializersModule
        },
        fileSystem = FileSystem.SYSTEM,
        directory = applicationContext.getDir("TreehouseState", MODE_PRIVATE).toOkioPath(),
      ),
      leakDetector = leakDetector,
    )

    val manifestUrlFlow = flowOf("http://10.0.2.2:8080/manifest.zipline.json")
      .withDevelopmentServerPush(ziplineHttpClient)

    val treehouseApp = treehouseAppFactory.create(
      appScope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrl = manifestUrlFlow,
        hostApi = RealHostApi(this@EmojiSearchActivity, httpClient),
      ),
      eventListenerFactory = object : EventListener.Factory {
        override fun create(app: TreehouseApp<*>, manifestUrl: String?) = appEventListener
        override fun close() {
        }
      },
    )

    treehouseApp.start()

    return treehouseApp
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
