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
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlin.native.ObjCName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import okio.FileSystem
import okio.Path

@ObjCName("RealTreehouseApp", exact = true)
public class RealTreehouseApp<A : AppService> private constructor(
  private val factory: Factory,
  private val appScope: CoroutineScope,
  private val spec: TreehouseApp.Spec<A>,
) : TreehouseApp<A> {
  public val dispatchers: TreehouseDispatchers = factory.dispatchers

  private val codeHost = object : CodeHost<A>(
    dispatchers = dispatchers,
    appScope = appScope,
    frameClockFactory = factory.frameClockFactory,
    stateStore = factory.stateStore,
  ) {
    override fun codeUpdatesFlow(): Flow<CodeSession<A>> {
      return ziplineFlow().mapNotNull { loadResult ->
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

  public override val zipline: Zipline?
    get() = (codeHost.codeSession as? ZiplineCodeSession)?.zipline

  public override fun createContent(
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

  public override fun start() {
    codeHost.start()
  }

  public override fun stop() {
    codeHost.stop()
  }

  public override fun restart() {
    codeHost.restart()
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadResult] instance when new code is
   * found.
   */
  private fun ziplineFlow(): Flow<LoadResult> {
    var loader = ZiplineLoader(
      dispatcher = dispatchers.zipline,
      manifestVerifier = factory.manifestVerifier,
      httpClient = factory.httpClient,
    )

    loader.concurrentDownloads = factory.concurrentDownloads

    // Adapt [EventListener.Factory] to a [ZiplineEventListener.Factory]
    val ziplineEventListenerFactory = ZiplineEventListener.Factory { _, manifestUrl ->
      val eventListener = factory.eventListenerFactory.create(this@RealTreehouseApp, manifestUrl)
      RealEventPublisher(eventListener).ziplineEventListener
    }
    loader = loader.withEventListenerFactory(ziplineEventListenerFactory)

    if (!spec.loadCodeFromNetworkOnly) {
      loader = loader.withCache(
        cache = factory.cache,
      )

      if (factory.embeddedDir != null && factory.embeddedFileSystem != null) {
        loader = loader.withEmbedded(
          embeddedDir = factory.embeddedDir,
          embeddedFileSystem = factory.embeddedFileSystem,
        )
      }
    }

    return loader.load(
      applicationName = spec.name,
      manifestUrlFlow = spec.manifestUrl,
      serializersModule = spec.serializersModule,
    ) { zipline ->
      spec.bindServices(zipline)
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
  @ObjCName("RealTreehouseAppFactory", exact = true)
  public class Factory internal constructor(
    private val platform: TreehousePlatform,
    public val dispatchers: TreehouseDispatchers,
    internal val eventListenerFactory: EventListener.Factory,
    internal val httpClient: ZiplineHttpClient,
    internal val frameClockFactory: FrameClock.Factory,
    internal val manifestVerifier: ManifestVerifier,
    internal val embeddedDir: Path?,
    internal val embeddedFileSystem: FileSystem?,
    private val cacheName: String,
    private val cacheMaxSizeInBytes: Long,
    internal val concurrentDownloads: Int,
    internal val stateStore: StateStore,
  ) : TreehouseApp.Factory {
    /** This is lazy to avoid initializing the cache on the thread that creates this launcher. */
    internal val cache: ZiplineCache by lazy {
      platform.newCache(name = cacheName, maxSizeInBytes = cacheMaxSizeInBytes)
    }

    public override fun <A : AppService> create(
      appScope: CoroutineScope,
      spec: TreehouseApp.Spec<A>,
    ): TreehouseApp<A> = RealTreehouseApp(this, appScope, spec)

    override fun close() {
      cache.close()
    }
  }
}
