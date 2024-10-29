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
package app.cash.redwood.treehouse

import app.cash.redwood.leaks.LeakDetector
import app.cash.zipline.Zipline
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineHttpClient
import com.example.redwood.testapp.treehouse.HostApi
import com.example.redwood.testapp.treehouse.TestAppPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import okio.ByteString
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * Create a production-like instance of the test-app.
 *
 * This uses a real Zipline runtime.
 *
 * It doesn't use real HTTP; the [ZiplineHttpClient] loads files directly from the test-app/ module.
 */
internal class TreehouseTester(
  private val testScope: TestScope,
) {
  val eventLog = EventLog()

  var hostApi: HostApi = FakeHostApi()

  var eventListenerFactory: EventListener.Factory = FakeEventListener.Factory(eventLog)

  private val manifestUrl = MutableStateFlow("http://example.com/manifest.zipline.json")

  private val kotlinZiplineDir = "../test-app/presenter-treehouse/build/zipline/Development".toPath()

  private val returnedTreehouseDispatchers = mutableListOf<FakeDispatchers>()

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
    override fun newCache(
      name: String,
      maxSizeInBytes: Long,
      loaderEventListener: LoaderEventListener,
    ) = error("unexpected call")

    override fun newDispatchers(
      applicationName: String,
    ): FakeDispatchers {
      return FakeDispatchers(testScope)
        .also { returnedTreehouseDispatchers += it }
    }
  }

  private var appLifecycleAwaitingAFrame = MutableStateFlow<AppLifecycle?>(null)

  private val frameClock = object : FrameClock {
    override fun requestFrame(appLifecycle: AppLifecycle) {
      appLifecycleAwaitingAFrame.value = appLifecycle
    }

    override fun close() {
    }
  }

  private val frameClockFactory = object : FrameClock.Factory {
    override fun create(scope: CoroutineScope, dispatchers: TreehouseDispatchers) = frameClock
  }

  val treehouseAppFactory = RealTreehouseApp.Factory(
    platform = platform,
    httpClient = httpClient,
    frameClockFactory = frameClockFactory,
    manifestVerifier = ManifestVerifier.NO_SIGNATURE_CHECKS,
    embeddedFileSystem = null,
    embeddedDir = null,
    cacheName = "cache",
    cacheMaxSizeInBytes = 0L,
    ziplineLoaderDispatcher = testScope.dispatcher(),
    concurrentDownloads = 1,
    loaderEventListener = LoaderEventListener.None,
    stateStore = MemoryStateStore(),
    leakDetector = LeakDetector.none(),
  )

  val openTreehouseDispatchersCount: Int
    get() = returnedTreehouseDispatchers.count { !it.isClosed }

  var spec: TreehouseApp.Spec<TestAppPresenter> = object : TreehouseApp.Spec<TestAppPresenter>() {
    override val name: String
      get() = "test_app"
    override val manifestUrl: Flow<String>
      get() = this@TreehouseTester.manifestUrl
    override val loadCodeFromNetworkOnly: Boolean
      get() = true

    override suspend fun bindServices(
      treehouseApp: TreehouseApp<TestAppPresenter>,
      zipline: Zipline,
    ) {
      zipline.bind("HostApi", hostApi)
    }

    override fun create(zipline: Zipline): TestAppPresenter {
      return zipline.take("TestAppPresenter")
    }
  }

  fun loadApp(): TreehouseApp<TestAppPresenter> {
    return treehouseAppFactory.create(
      appScope = testScope,
      spec = spec,
      eventListenerFactory = eventListenerFactory,
    )
  }

  fun content(treehouseApp: TreehouseApp<TestAppPresenter>): Content {
    return treehouseApp.createContent(
      source = { app -> app.launchForTester() },
    )
  }

  fun view(): FakeTreehouseView {
    return FakeTreehouseView(
      name = "view",
      onBackPressedDispatcher = FakeOnBackPressedDispatcher(eventLog),
    )
  }

  /** Waits for a frame to be requested, then sends it. */
  suspend fun sendFrame() {
    val target = appLifecycleAwaitingAFrame.first { it != null }!!
    appLifecycleAwaitingAFrame.value = null
    target.sendFrame(0L)
  }
}
