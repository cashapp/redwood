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

import app.cash.zipline.EventListener as ZiplineEventListener
import app.cash.zipline.Zipline
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.LoaderEventListener
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import okio.FileSystem
import okio.Path

internal class RealTreehouseApp<A : AppService> private constructor(
  private val factory: Factory,
  private val appScope: CoroutineScope,
  override val spec: Spec<A>,
  eventListenerFactory: EventListener.Factory,
) : TreehouseApp<A>() {
  /** This property is confined to [TreehouseDispatchers.ui]. */
  private var closed = false

  /** Non-null until this app is closed. This property is confined to [TreehouseDispatchers.ui]. */
  private var eventListenerFactory: EventListener.Factory? = eventListenerFactory

  override val dispatchers = factory.dispatchers

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
  private fun ziplineFlow(
    eventListenerFactory: EventListener.Factory,
  ): Flow<LoadResult> {
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
    eventListenerFactory = null
    stop()
  }

  class Factory internal constructor(
    private val platform: TreehousePlatform,
    override val dispatchers: TreehouseDispatchers,
    internal val httpClient: ZiplineHttpClient,
    internal val frameClockFactory: FrameClock.Factory,
    internal val manifestVerifier: ManifestVerifier,
    internal val embeddedFileSystem: FileSystem?,
    internal val embeddedDir: Path?,
    private val cacheName: String,
    private val cacheMaxSizeInBytes: Long,
    private val loaderEventListener: LoaderEventListener,
    internal val concurrentDownloads: Int,
    internal val stateStore: StateStore,
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
    ): TreehouseApp<A> = RealTreehouseApp(this, appScope, spec, eventListenerFactory)

    override fun close() {
      if (cache.isInitialized()) {
        cache.value.close()
      }

      dispatchers.close()
    }
  }
}
