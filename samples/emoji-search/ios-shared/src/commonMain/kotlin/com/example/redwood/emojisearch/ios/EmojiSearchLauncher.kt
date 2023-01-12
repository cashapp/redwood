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
package com.example.redwood.emojisearch.ios

import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import com.example.redwood.emojisearch.launcher.EmojiSearchAppSpec
import com.example.redwood.emojisearch.treehouse.EmojiSearchPresenter
import com.example.redwood.emojisearch.treehouse.HostApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import platform.Foundation.NSURLSession

class EmojiSearchLauncher(
  private val nsurlSession: NSURLSession,
  private val hostApi: HostApi,
) {
  private val coroutineScope: CoroutineScope = MainScope()
  private val manifestUrl = "http://localhost:8080/manifest.zipline.json"

  @Suppress("unused") // Invoked in Swift.
  fun createTreehouseApp(): TreehouseApp<EmojiSearchPresenter> {
    val treehouseAppFactory = TreehouseAppFactory(
      httpClient = nsurlSession.asZiplineHttpClient(),
      manifestVerifier = ManifestVerifier.Companion.NO_SIGNATURE_CHECKS,
    )

    val treehouseApp = treehouseAppFactory.create(
      appScope = coroutineScope,
      spec = EmojiSearchAppSpec(
        manifestUrlString = manifestUrl,
        hostApi = hostApi,
      ),
    )

    treehouseApp.start()

    return treehouseApp
  }
}
