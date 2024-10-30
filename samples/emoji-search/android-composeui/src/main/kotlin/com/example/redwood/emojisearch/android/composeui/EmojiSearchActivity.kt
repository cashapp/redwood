/*
 * Copyright (C) 2024 Square, Inc.
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
package com.example.redwood.emojisearch.android.composeui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration.Indefinite
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.layout.composeui.ComposeUiRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.composeui.ComposeUiRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.treehouse.EventListener
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.composeui.TreehouseContent
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import app.cash.zipline.loader.withDevelopmentServerPush
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import com.example.redwood.emojisearch.composeui.ComposeUiEmojiSearchWidgetFactory
import com.example.redwood.emojisearch.composeui.EmojiSearchTheme
import com.example.redwood.emojisearch.launcher.EmojiSearchAppSpec
import com.example.redwood.emojisearch.protocol.host.EmojiSearchProtocolFactory
import com.example.redwood.emojisearch.treehouse.EmojiSearchPresenter
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetSystem
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import okio.assetfilesystem.asFileSystem

@NoLiveLiterals
class EmojiSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)
  private val snackbarHostState = SnackbarHostState()

  private val leakDetector = LeakDetector.timeBasedIn(
    scope = scope,
    timeSource = TimeSource.Monotonic,
    leakThreshold = 10.seconds,
    callback = { reference, note ->
      Log.e("LEAK", "Leak detected! $reference $note")
    },
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val client = OkHttpClient()
    val treehouseApp = createTreehouseApp(client)
    val treehouseContentSource = TreehouseContentSource(EmojiSearchPresenter::launch)

    val imageLoader = ImageLoader.Builder(this)
      .serviceLoaderEnabled(false)
      .components {
        add(OkHttpNetworkFetcherFactory(client))
      }
      .build()

    val widgetSystem = WidgetSystem { json, protocolMismatchHandler ->
      EmojiSearchProtocolFactory<@Composable () -> Unit>(
        widgetSystem = EmojiSearchWidgetSystem(
          EmojiSearch = ComposeUiEmojiSearchWidgetFactory(imageLoader),
          RedwoodLayout = ComposeUiRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout = ComposeUiRedwoodLazyLayoutWidgetFactory(),
        ),
        json = json,
        mismatchHandler = protocolMismatchHandler,
      )
    }

    setContent {
      EmojiSearchTheme {
        Scaffold(
          snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
          TreehouseContent(
            treehouseApp = treehouseApp,
            widgetSystem = widgetSystem,
            contentSource = treehouseContentSource,
            modifier = Modifier.padding(contentPadding),
            dynamicContentWidgetFactory = EmojiSearchDynamicContentWidgetFactory(),
          )
        }
      }
    }
  }

  val appEventListener: EventListener = object : EventListener() {
    private var success = true
    private var snackbarJob: Job? = null

    override fun codeLoadFailed(exception: Exception, startValue: Any?) {
      Log.w("Treehouse", "codeLoadFailed", exception)
      if (success) {
        // Only show the Snackbar on the first transition from success.
        success = false
        snackbarJob = scope.launch {
          snackbarHostState.showSnackbar(
            message = "Unable to load guest code from server",
            actionLabel = "Dismiss",
            duration = Indefinite,
          )
        }
      }
    }

    override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
      Log.i("Treehouse", "codeLoadSuccess")
      success = true
      snackbarJob?.cancel()
    }
  }

  private fun createTreehouseApp(httpClient: OkHttpClient): TreehouseApp<EmojiSearchPresenter> {
    val treehouseAppFactory = TreehouseAppFactory(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
      embeddedFileSystem = applicationContext.assets.asFileSystem(),
      embeddedDir = "/".toPath(),
      leakDetector = leakDetector,
    )

    val manifestUrlFlow = flowOf("http://10.0.2.2:8080/manifest.zipline.json")
      .withDevelopmentServerPush(httpClient.asZiplineHttpClient())

    val treehouseApp = treehouseAppFactory.create(
      appScope = scope,
      spec = EmojiSearchAppSpec(
        manifestUrl = manifestUrlFlow,
        hostApi = RealHostApi(
          client = httpClient,
          openUrl = { url ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(url))
            startActivity(intent)
          },
        ),
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
