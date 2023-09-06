/*
 * Copyright (C) 2023 Square, Inc.
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
package com.example.redwood.reposearch.android.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.view.ViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseLayout
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import app.cash.zipline.loader.withDevelopmentServerPush
import com.example.redwood.reposearch.launcher.RepoSearchAppSpec
import com.example.redwood.reposearch.treehouse.RepoSearchPresenter
import com.example.redwood.reposearch.widget.RepoSearchProtocolNodeFactory
import com.example.redwood.reposearch.widget.RepoSearchWidgetFactories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class RepoSearchActivity : ComponentActivity() {
  private val scope: CoroutineScope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val context = this
    val treehouseApp = createTreehouseApp()
    val treehouseContentSource = TreehouseContentSource(RepoSearchPresenter::launch)

    val widgetSystem = object : TreehouseView.WidgetSystem {
      override fun widgetFactory(
        json: Json,
        protocolMismatchHandler: ProtocolMismatchHandler,
      ) = RepoSearchProtocolNodeFactory(
        provider = RepoSearchWidgetFactories(
          RepoSearch = AndroidRepoSearchWidgetFactory(context),
          RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context),
          RedwoodLazyLayout = ViewRedwoodLazyLayoutWidgetFactory(context),
        ),
        json = json,
        mismatchHandler = protocolMismatchHandler,
      )
    }

    setContentView(
      TreehouseLayout(this, widgetSystem).apply {
        treehouseContentSource.bindWhenReady(this, treehouseApp)
      },
    )
  }

  private fun createTreehouseApp(): TreehouseApp<RepoSearchPresenter> {
    val httpClient = OkHttpClient()
    val ziplineHttpClient = httpClient.asZiplineHttpClient()

    val treehouseAppFactory = TreehouseAppFactory(
      context = applicationContext,
      httpClient = httpClient,
      manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
    )

    val manifestUrlFlow = flowOf("http://10.0.2.2:8080/manifest.zipline.json")
      .withDevelopmentServerPush(ziplineHttpClient)

    val treehouseApp = treehouseAppFactory.create(
      appScope = scope,
      spec = RepoSearchAppSpec(
        manifestUrl = manifestUrlFlow,
        hostApi = RealHostApi(httpClient),
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
