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
package com.example.redwood.testing.ios

import app.cash.redwood.treehouse.EventListener
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseAppFactory
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.ManifestVerifier.Companion.NO_SIGNATURE_CHECKS
import app.cash.zipline.loader.asZiplineHttpClient
import app.cash.zipline.loader.withDevelopmentServerPush
import com.example.redwood.testing.launcher.TestAppSpec
import com.example.redwood.testing.treehouse.HostApi
import com.example.redwood.testing.treehouse.TestAppPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOf
import platform.Foundation.NSLog
import platform.Foundation.NSURLSession

class TestAppLauncher(
  private val nsurlSession: NSURLSession,
  private val hostApi: HostApi,
) {
  private val coroutineScope: CoroutineScope = MainScope()
  private val manifestUrl = "http://localhost:8080/manifest.zipline.json"

  @Suppress("unused") // Invoked in Swift.
  fun createTreehouseApp(): TreehouseApp<TestAppPresenter> {
    val ziplineHttpClient = nsurlSession.asZiplineHttpClient()

    val eventListener = object : EventListener() {
      override fun codeLoadFailed(exception: Exception, startValue: Any?) {
        NSLog("Treehouse: codeLoadFailed: $exception")
      }

      override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
        NSLog("Treehouse: codeLoadSuccess")
      }
    }

    val treehouseAppFactory = TreehouseAppFactory(
      httpClient = ziplineHttpClient,
      manifestVerifier = NO_SIGNATURE_CHECKS,
      eventListenerFactory = { app, manifestUrl -> eventListener },
    )

    val manifestUrlFlow = flowOf(manifestUrl)
      .withDevelopmentServerPush(ziplineHttpClient)

    val treehouseApp = treehouseAppFactory.create(
      appScope = coroutineScope,
      spec = TestAppSpec(
        manifestUrl = manifestUrlFlow,
        hostApi = hostApi,
      ),
    )

    treehouseApp.start()

    return treehouseApp
  }
}
