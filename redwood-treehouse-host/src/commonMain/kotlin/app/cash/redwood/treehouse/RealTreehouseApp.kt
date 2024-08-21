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
package app.cash.redwood.treehouse

import app.cash.redwood.leaks.LeakDetector
import app.cash.zipline.EventListener as ZiplineEventListener
import app.cash.zipline.Zipline
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import okio.FileSystem
import okio.Path

internal class RealTreehouseApp<A : AppService> private constructor(
  private val factory: Factory,
  private val appScope: CoroutineScope,
  spec: Spec<A>,
  override val dispatchers: TreehouseDispatchers,
  eventListenerFactory: EventListener.Factory,
  private val leakDetector: LeakDetector,
) : TreehouseApp<A>() {
  /** This property is confined to [TreehouseDispatchers.ui]. */
  private var closed = false

  /** Non-null until this app is closed. This property is confined to [TreehouseDispatchers.ui]. */
  private var eventListenerFactory: EventListener.Factory? = eventListenerFactory

  private val codeHost = object : CodeHost<A>(
    dispatchers = dispatchers,
    appScope = appScope,
    frameClockFactory = factory.frameClockFactory,
    stateStore = factory.stateStore,
  ) {
    override fun codeUpdatesFlow(
      eventListenerFactory: EventListener.Factory,
    ): Flow<CodeSession<A>> {
      return ziplineFlow(eventListenerFactory).mapNotNull { loadResult ->
        when (loadResult) {
          is LoadResult.Failure -> {
            null // EventListener already notified.
          }

          is LoadResult.Success -> {
            createCodeSession(loadResult.zipline)
          }
        }
      }
    }
  }

  private var spec: Spec<A>? = spec

  override val name: String = spec.name

  override val zipline: StateFlow<Zipline?>
    get() = codeHost.zipline

  override fun createContent(
    source: TreehouseContentSource<A>,
    codeListener: CodeListener,
  ): Content {
    start()

    return TreehouseAppContent(
      codeHost = codeHost,
      dispatchers = dispatchers,
      codeEventPublisher = RealCodeEventPublisher(codeListener, this),
      source = source,
      leakDetector = leakDetector,
    )
  }

  override fun start() {
    val eventListenerFactory = eventListenerFactory ?: error("closed")
    codeHost.start(eventListenerFactory)
  }

  override fun stop() {
    codeHost.stop()
  }

  override fun restart() {
    val eventListenerFactory = eventListenerFactory ?: error("closed")
    codeHost.restart(eventListenerFactory)
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadResult] instance when new code is
   * found.
   */
  @OptIn(ExperimentalCoroutinesApi::class) // limitedParallelism is experimental.
  private fun ziplineFlow(
    eventListenerFactory: EventListener.Factory,
  ): Flow<LoadResult> {
    val spec = spec ?: error("closed")
    var loader = ZiplineLoader(
      dispatcher = dispatchers.zipline,
      manifestVerifier = factory.manifestVerifier,
      httpClient = factory.httpClient,
    )

    loader.concurrentDownloads = factory.concurrentDownloads

    // Adapt [EventListener.Factory] to a [ZiplineEventListener.Factory]
    val ziplineEventListenerFactory = ZiplineEventListener.Factory { _, manifestUrl ->
      val eventListener = eventListenerFactory.create(this@RealTreehouseApp, manifestUrl)
      RealEventPublisher(eventListener).ziplineEventListener
    }
    loader = loader.withEventListenerFactory(ziplineEventListenerFactory)

    if (!spec.loadCodeFromNetworkOnly) {
      loader = loader.withCache(
        cache = factory.cache.value,
        cacheDispatcher = factory.ziplineLoaderDispatcher.limitedParallelism(1),
      )

      if (factory.embeddedFileSystem != null && factory.embeddedDir != null) {
        loader = loader.withEmbedded(
          embeddedFileSystem = factory.embeddedFileSystem,
          embeddedDir = factory.embeddedDir,
        )
      }
    }

    return loader.load(
      applicationName = spec.name,
      manifestUrlFlow = spec.manifestUrl,
      serializersModule = spec.serializersModule,
      freshnessChecker = spec.freshnessChecker,
    ) { zipline ->
      spec.bindServices(this, zipline)
    }
  }

  private fun createCodeSession(zipline: Zipline): ZiplineCodeSession<A> {
    val spec = spec ?: error("closed")
    val appService = spec.create(zipline)

    // Extract the RealEventPublisher() created in ziplineFlow().
    val eventListener = zipline.eventListener as RealEventPublisher.ZiplineEventListener
    val eventPublisher = eventListener.eventPublisher

    return ZiplineCodeSession(
      dispatchers = dispatchers,
      eventPublisher = eventPublisher,
      frameClockFactory = factory.frameClockFactory,
      appService = appService,
      zipline = zipline,
      appScope = appScope,
    )
  }

  override fun close() {
    dispatchers.checkUi()

    closed = true
    spec = null
    eventListenerFactory = null
    stop()
    dispatchers.close()
  }

  class Factory internal constructor(
    private val platform: TreehousePlatform,
    internal val httpClient: ZiplineHttpClient,
    internal val frameClockFactory: FrameClock.Factory,
    internal val manifestVerifier: ManifestVerifier,
    internal val embeddedFileSystem: FileSystem?,
    internal val embeddedDir: Path?,
    private val cacheName: String,
    private val cacheMaxSizeInBytes: Long,
    internal val ziplineLoaderDispatcher: CoroutineDispatcher,
    private val loaderEventListener: LoaderEventListener,
    internal val concurrentDownloads: Int,
    internal val stateStore: StateStore,
    private val leakDetector: LeakDetector,
  ) : TreehouseApp.Factory {
    /** This is lazy to avoid initializing the cache on the thread that creates this launcher. */
    internal val cache = lazy {
      platform.newCache(
        name = cacheName,
        maxSizeInBytes = cacheMaxSizeInBytes,
        loaderEventListener = loaderEventListener,
      )
    }

    override fun <A : AppService> create(
      appScope: CoroutineScope,
      spec: Spec<A>,
      eventListenerFactory: EventListener.Factory,
    ): TreehouseApp<A> = RealTreehouseApp(
      factory = this,
      appScope = appScope,
      spec = spec,
      dispatchers = platform.newDispatchers(spec.name),
      eventListenerFactory = eventListenerFactory,
      leakDetector = leakDetector,
    )

    override fun close() {
      if (cache.isInitialized()) {
        cache.value.close()
      }
    }
  }
}
