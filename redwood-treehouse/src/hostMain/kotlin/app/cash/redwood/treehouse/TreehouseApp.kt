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

import app.cash.zipline.Zipline
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import okio.Closeable
import okio.FileSystem
import okio.Path

/**
 * This class binds downloaded code to on-screen views.
 *
 * It updates the binding when the views change via [TreehouseView.OnStateChangeListener], and when
 * new code is available in [onCodeChanged].
 */
public class TreehouseApp<A : AppService> private constructor(
  private val factory: Factory,
  private val appScope: CoroutineScope,
  public val spec: Spec<A>,
) {
  public val dispatchers: TreehouseDispatchers = factory.dispatchers
  private val eventPublisher: EventPublisher = factory.eventPublisher

  private var started = false

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private var closed = false

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var ziplineSession: ZiplineSession<A>? = null

  /**
   * Keys are views currently attached on-screen with non-null contents.
   * Only accessed on [TreehouseDispatchers.ui].
   */
  private val bindings = mutableMapOf<TreehouseView<A>, Binding>()

  /**
   * Returns the current zipline attached to this host, or null if Zipline hasn't loaded yet. The
   * returned value will be invalid when new code is loaded.
   *
   * It is unwise to use this instance for anything beyond measurement and monitoring, because the
   * instance may be replaced if new code is loaded.
   */
  public val zipline: Zipline?
    get() = ziplineSession?.zipline

  private val stateChangeListener = TreehouseView.OnStateChangeListener { view ->
    bind(view, ziplineSession, codeChanged = false)
  }

  public fun renderTo(view: TreehouseView<A>) {
    view.stateChangeListener = stateChangeListener
    stateChangeListener.onStateChanged(view)
  }

  public fun start() {
    if (started) return
    started = true

    eventPublisher.appStart(this)

    appScope.launch(dispatchers.zipline) {
      val ziplineFileFlow = ziplineFlow()
      ziplineFileFlow.collect {
        when (it) {
          is LoadResult.Success -> {
            val app = spec.create(it.zipline)
            onCodeChanged(it.zipline, app)
          }
          is LoadResult.Failure -> {
            // EventListener already notified.
          }
        }
      }
    }
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadResult] instance when new code is
   * found.
   */
  private fun ziplineFlow(): Flow<LoadResult> {
    // Loads applications from the network only. The cache is neither read nor written.
    val ziplineLoaderNetworkOnly = ZiplineLoader(
      dispatcher = dispatchers.zipline,
      manifestVerifier = factory.manifestVerifier,
      httpClient = factory.httpClient,
      eventListener = eventPublisher.ziplineEventListener(this),
    )

    ziplineLoaderNetworkOnly.concurrentDownloads = factory.concurrentDownloads

    val ziplineLoaderForLoad = when (spec.freshCodePolicy) {
      FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY -> {
        ziplineLoaderNetworkOnly
      }
      else -> {
        // Loads applications from the network, the cache, or the embedded resources.
        ziplineLoaderNetworkOnly.withCache(
          cache = factory.cache,
        ).withEmbedded(
          embeddedDir = factory.embeddedDir,
          embeddedFileSystem = factory.embeddedFileSystem,
        )
      }
    }

    val manifestUrlFlowForLoad = when (spec.freshCodePolicy) {
      FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY -> hotReloadFlow(spec.manifestUrl)
      else -> spec.manifestUrl
    }

    return ziplineLoaderForLoad.load(
      applicationName = spec.name,
      manifestUrlFlow = manifestUrlFlowForLoad,
      serializersModule = spec.serializersModule,
    ) { zipline ->
      spec.bindServices(zipline)
    }
  }

  /**
   * Refresh the code. Even if no views are currently showing we refresh the code, so we're ready
   * when a view is added.
   *
   * This function may only be invoked on [TreehouseDispatchers.zipline].
   */
  private fun onCodeChanged(zipline: Zipline, appService: A) {
    dispatchers.checkZipline()
    check(!closed)

    val sessionScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))
    sessionScope.launch(dispatchers.ui) {
      val previous = ziplineSession

      val next = ZiplineSession(
        app = this@TreehouseApp,
        appScope = appScope,
        sessionScope = sessionScope,
        zipline = zipline,
        appService = appService,
        isInitialLaunch = previous == null,
      )

      next.startFrameClock()

      val viewsToRebind = bindings.keys.toTypedArray() // Defensive copy 'cause bind() mutates.
      for (treehouseView in viewsToRebind) {
        bind(treehouseView, next, codeChanged = true)
      }

      if (previous != null) {
        sessionScope.launch(dispatchers.zipline) {
          previous.cancel()
        }
      }

      ziplineSession = next
    }
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  private fun bind(
    view: TreehouseView<A>,
    ziplineSession: ZiplineSession<A>?,
    codeChanged: Boolean,
  ) {
    dispatchers.checkUi()

    // Make sure we're tracking this view, so we can update it when the code changes.
    val content = view.boundContent
    val previous = bindings[view]
    if (!codeChanged && previous is RealBinding<*> && content == previous.content) {
      return // Nothing has changed.
    }

    val next = when {
      // We have content and code. Launch the treehouse UI.
      content != null && ziplineSession != null -> {
        RealBinding(
          app = this@TreehouseApp,
          appScope = appScope,
          eventPublisher = eventPublisher,
          content = content,
          session = ziplineSession,
          view = view,
        ).apply {
          start(ziplineSession, view)
        }
      }

      // We have content but no code. Keep track of it for later.
      content != null -> {
        LoadingBinding.also {
          if (previous == null) {
            view.codeListener.onInitialCodeLoading()
          }
        }
      }

      // No content.
      else -> null
    }

    // Replace the previous binding, if any.
    when {
      next != null -> bindings[view] = next
      else -> bindings.remove(view)
    }

    previous?.cancel()
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  public fun cancel() {
    dispatchers.checkZipline()
    closed = true
    appScope.launch(dispatchers.ui) {
      val session = ziplineSession ?: return@launch
      session.cancel()
      ziplineSession = null
    }
    eventPublisher.appCanceled(this)
  }

  /**
   * This manages a cache that should be shared by all launched applications.
   *
   * This class holds a stateful disk cache. At most one instance with each [cacheName] should be
   * open at any time. Most callers should use a single [Factory] for best caching.
   */
  public class Factory internal constructor(
    private val platform: TreehousePlatform,
    public val dispatchers: TreehouseDispatchers,
    eventListener: EventListener,
    internal val httpClient: ZiplineHttpClient,
    internal val manifestVerifier: ManifestVerifier,
    internal val embeddedDir: Path,
    internal val embeddedFileSystem: FileSystem,
    private val cacheName: String,
    private val cacheMaxSizeInBytes: Long,
    internal val concurrentDownloads: Int,
  ) : Closeable {
    internal val eventPublisher = EventPublisher(eventListener)

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
    public abstract val manifestUrl: Flow<String>

    public open val serializersModule: SerializersModule
      get() = EmptySerializersModule()

    public open val freshCodePolicy: FreshCodePolicy
      get() = FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY

    public abstract fun bindServices(zipline: Zipline)
    public abstract fun create(zipline: Zipline): A
  }
}
