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
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okio.Closeable
import okio.FileSystem
import okio.Path

/**
 * This class binds downloaded code to on-screen views.
 */
@ObjCName("TreehouseApp", exact = true)
public class TreehouseApp<A : AppService> private constructor(
  private val factory: Factory,
  private val appScope: CoroutineScope,
  public val spec: Spec<A>,
) {
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

  /**
   * Returns the current zipline attached to this host, or null if Zipline hasn't loaded yet. The
   * returned value will be invalid when new code is loaded.
   *
   * It is unwise to use this instance for anything beyond measurement and monitoring, because the
   * instance may be replaced if new code is loaded.
   */
  public val zipline: Zipline?
    get() = (codeHost.codeSession as? ZiplineCodeSession)?.zipline

  /**
   * Create content for [source].
   *
   * Calls to this function will [start] this app if it isn't already started.
   */
  public fun createContent(
    source: TreehouseContentSource<A>,
    codeListener: CodeListener = CodeListener(),
  ): Content {
    start()

    return TreehouseAppContent(
      codeHost = codeHost,
      dispatchers = dispatchers,
      appScope = appScope,
      codeListener = codeListener,
      source = source,
    )
  }

  /**
   * Initiate the initial code download and load, and start driving the views that are rendered by
   * this app.
   *
   * This function returns immediately if this app is already started.
   *
   * This function may only be invoked on [TreehouseDispatchers.ui].
   */
  public fun start() {
    codeHost.start()
  }

  /**
   * Stop any currently-running code and stop receiving new code.
   *
   * This function may only be invoked on [TreehouseDispatchers.ui].
   */
  public fun stop() {
    codeHost.stop()
  }

  /**
   * Stop the currently-running application (if any) and start it again.
   *
   * This function may only be invoked on [TreehouseDispatchers.ui].
   */
  public fun restart() {
    codeHost.restart()
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadResult] instance when new code is
   * found.
   */
  private fun ziplineFlow(): Flow<LoadResult> {
    // Loads applications from the network only. The cache is neither read nor written.
    var loader = ZiplineLoader(
      dispatcher = dispatchers.zipline,
      manifestVerifier = factory.manifestVerifier,
      httpClient = factory.httpClient,
    )

    loader.concurrentDownloads = factory.concurrentDownloads

    // Adapt [EventListener.Factory] to a [ZiplineEventListener.Factory]
    val ziplineEventListenerFactory = ZiplineEventListener.Factory { _, manifestUrl ->
      val eventListener = factory.eventListenerFactory.create(this@TreehouseApp, manifestUrl)
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
      appScope = appScope,
      eventPublisher = eventPublisher,
      appService = appService,
      zipline = zipline,
    )
  }

  /**
   * This manages a cache that should be shared by all launched applications.
   *
   * This class holds a stateful disk cache. At most one instance with each [cacheName] should be
   * open at any time. Most callers should use a single [Factory] for best caching.
   */
  @ObjCName("TreehouseAppFactory", exact = true)
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
  ) : Closeable {
    /** This is lazy to avoid initializing the cache on the thread that creates this launcher. */
    internal val cache: ZiplineCache by lazy {
      platform.newCache(name = cacheName, maxSizeInBytes = cacheMaxSizeInBytes)
    }

    public fun <A : AppService> create(
      appScope: CoroutineScope,
      spec: Spec<A>,
    ): TreehouseApp<A> = TreehouseApp(this, appScope, spec)

    override fun close() {
      cache.close()
    }
  }

  /**
   * Configuration and code to launch a Treehouse application.
   */
  public abstract class Spec<A : AppService> {
    public abstract val name: String

    /**
     * The URL of the Zipline manifest file to load this app's code from.
     *
     * This flow should emit each time that a code load should be attempted. No code will be loaded
     * until this flow's first emit.
     *
     * The flow may make subsequent emits to trigger a hot reload attempt. Hot reloads will be
     * attempted even if the URL is unchanged. This is typically most useful during development.
     * Consider using [app.cash.zipline.loader.withDevelopmentServerPush] to turn the Zipline
     * development server URL flow into one that emits each time that server's code is updated.
     */
    public abstract val manifestUrl: Flow<String>

    public open val serializersModule: SerializersModule
      get() = EmptySerializersModule()

    /**
     * Returns true to only load code from the network. Otherwise, this will recover from
     * unreachable network code by loading code from the cache or the embedded file system.
     *
     * This is false by default. Override it to return true in development, where loading code from
     * a source other than the network may be surprising.
     */
    public open val loadCodeFromNetworkOnly: Boolean
      get() = false

    public abstract fun bindServices(zipline: Zipline)
    public abstract fun create(zipline: Zipline): A
  }
}
