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

import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okio.Closeable
import okio.FileSystem
import okio.Path

/**
 * This manages a cache that should be shared by all launched applications.
 *
 * This class holds a stateful disk cache. At most one instance with each [cacheName] should be
 * open at any time. Most callers should use a single [TreehouseLauncher] for best caching.
 */
public class TreehouseLauncher internal constructor(
  private val platform: TreehousePlatform,
  public val dispatchers: TreehouseDispatchers,
  httpClient: ZiplineHttpClient,
  manifestVerifier: ManifestVerifier,
  private val embeddedDir: Path,
  private val embeddedFileSystem: FileSystem,
  private val cacheName: String,
  private val cacheMaxSizeInBytes: Long,
) : Closeable {
  /** Loads applications from the network only. The cache is neither read nor written. */
  private val ziplineLoaderNetworkOnly = ZiplineLoader(
    dispatcher = dispatchers.zipline,
    manifestVerifier = manifestVerifier,
    httpClient = httpClient,
    eventListener = TreehouseEventListener(platform),
  )

  /** This is lazy to avoid initializing the cache on the thread that creates this launcher. */
  private val cache: ZiplineCache by lazy {
    platform.newCache(name = cacheName, maxSizeInBytes = cacheMaxSizeInBytes)
  }

  /** Loads applications from the network, the cache, or the embedded resources. */
  private val ziplineLoaderNetworkCacheEmbedded: ZiplineLoader by lazy {
    ziplineLoaderNetworkOnly.withCache(
      cache = cache,
    ).withEmbedded(
      embeddedDir = embeddedDir,
      embeddedFileSystem = embeddedFileSystem,
    )
  }

  public fun <T : Any> launch(
    scope: CoroutineScope,
    spec: TreehouseApp.Spec<T>,
  ): TreehouseApp<T> {
    val treehouseApp = TreehouseApp<T>(
      scope = scope,
      dispatchers = dispatchers,
      viewBinder = spec.viewBinder,
    )

    scope.launch(dispatchers.zipline) {
      val ziplineFileFlow = ziplineFlow(spec)
      ziplineFileFlow.collect {
        when (it) {
          is LoadResult.Success -> {
            val app = spec.create(it.zipline)
            treehouseApp.onCodeChanged(it.zipline, app)
          }
          is LoadResult.Failure -> {
            // TODO
          }
        }
      }
    }

    return treehouseApp
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadResult] instance when new code is
   * found.
   */
  private fun ziplineFlow(spec: TreehouseApp.Spec<*>): Flow<LoadResult> {
    val ziplineLoaderForLoad = when (spec.freshCodePolicy) {
      FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY -> ziplineLoaderNetworkOnly
      else -> ziplineLoaderNetworkCacheEmbedded
    }

    val manifestUrlFlowForLoad = when (spec.freshCodePolicy) {
      FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY -> spec.manifestUrl.rebounce(500.milliseconds)
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

  override fun close() {
    cache.close()
  }
}
