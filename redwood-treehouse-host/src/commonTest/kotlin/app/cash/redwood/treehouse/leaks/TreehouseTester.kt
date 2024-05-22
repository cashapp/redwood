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
package app.cash.redwood.treehouse.leaks

import app.cash.redwood.treehouse.AppLifecycle
import app.cash.redwood.treehouse.EventLog
import app.cash.redwood.treehouse.FrameClock
import app.cash.redwood.treehouse.MemoryStateStore
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseDispatchers
import app.cash.redwood.treehouse.TreehousePlatform
import app.cash.zipline.Zipline
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineHttpClient
import com.example.redwood.testapp.treehouse.HostApi
import com.example.redwood.testapp.treehouse.TestAppPresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import okio.ByteString
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.SYSTEM

/**
 * Create a production-like instance of the test-app.
 *
 * This uses a real Zipline runtime.
 *
 * It doesn't use real HTTP; the [ZiplineHttpClient] loads files directly from the test-app/ module.
 */
class TreehouseTester(
  private val testScope: TestScope,
  private val eventLog: EventLog,
) {
  @OptIn(ExperimentalStdlibApi::class)
  private val testDispatcher = testScope.coroutineContext[CoroutineDispatcher.Key] as TestDispatcher

  private val manifestUrl = MutableStateFlow("http://example.com/manifest.zipline.json")

  private val kotlinZiplineDir = "../test-app/presenter-treehouse/build/compileSync/js/main/developmentExecutable/kotlinZipline".toPath()

  private val httpClient = object : ZiplineHttpClient() {
    override suspend fun download(
      url: String,
      requestHeaders: List<Pair<String, String>>,
    ): ByteString {
      val regex = Regex("http://example[.]com(.*)")
      val match = regex.matchEntire(url) ?: throw IOException("unexpected URL: $url")
      val file = match.groupValues[1].toPath(normalize = true)
      FileSystem.SYSTEM.read(kotlinZiplineDir / file.relativeTo("/".toPath())) {
        return readByteString()
      }
    }
  }

  private val platform = object : TreehousePlatform {
    override fun newCache(name: String, maxSizeInBytes: Long) = error("unexpected call")
  }

  private val dispatchers = object : TreehouseDispatchers {
    override val ui = testDispatcher
    override val zipline = testDispatcher

    override fun checkUi() {
    }

    override fun checkZipline() {
    }

    override fun close() {
    }
  }

  private var appLifecycleAwaitingAFrame = MutableStateFlow<AppLifecycle?>(null)

  private val frameClock = object : FrameClock {
    override fun requestFrame(appLifecycle: AppLifecycle) {
      appLifecycleAwaitingAFrame.value = appLifecycle
    }
  }

  private val frameClockFactory = object : FrameClock.Factory {
    override fun create(scope: CoroutineScope, dispatchers: TreehouseDispatchers) = frameClock
  }

  private val hostApi = object : HostApi {
    override suspend fun httpCall(url: String, headers: Map<String, String>): String {
      error("unexpected call")
    }
  }

  private val treehouseAppFactory = TreehouseApp.Factory(
    platform = platform,
    dispatchers = dispatchers,
    httpClient = httpClient,
    frameClockFactory = frameClockFactory,
    manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
    embeddedFileSystem = null,
    embeddedDir = null,
    cacheName = "cache",
    cacheMaxSizeInBytes = 0L,
    concurrentDownloads = 1,
    stateStore = MemoryStateStore(),
  )

  private val appSpec = object : TreehouseApp.Spec<TestAppPresenter>() {
    override val name: String
      get() = "test-app"
    override val manifestUrl: Flow<String>
      get() = this@TreehouseTester.manifestUrl
    override val loadCodeFromNetworkOnly: Boolean
      get() = true

    override fun bindServices(zipline: Zipline) {
      zipline.bind<HostApi>("HostApi", hostApi)
    }

    override fun create(zipline: Zipline): TestAppPresenter {
      return zipline.take("TestAppPresenter")
    }
  }

  fun loadApp(): TreehouseApp<TestAppPresenter> {
    return treehouseAppFactory.create(
      appScope = testScope,
      spec = appSpec,
      eventListenerFactory = FakeEventListener.Factory(eventLog),
    )
  }

  /** Waits for a frame to be requested, then sends it. */
  suspend fun sendFrame() {
    val target = appLifecycleAwaitingAFrame.first { it != null }!!
    appLifecycleAwaitingAFrame.value = null
    target.sendFrame(0L)
  }
}
