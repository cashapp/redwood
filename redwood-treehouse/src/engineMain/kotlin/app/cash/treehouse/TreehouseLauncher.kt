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
package app.cash.treehouse

import app.cash.zipline.loader.LoadedZipline
import app.cash.zipline.loader.ZiplineLoader
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * This manages a cache that should be shared by all launched applications. For best performance
 * and correctness this must be a singleton.
 */
public class TreehouseLauncher internal constructor(
  private val platform: TreehousePlatform,
) {
  /** Dispatchers to execute code with the right capabilities. */
  public val dispatchers: TreehouseDispatchers = platform.dispatchers

  /** Lazily create the ZiplineLoader so its initialized off the main dispatcher. */
  private val ziplineLoader: ZiplineLoader by lazy {
    platform.newZiplineLoader()
  }

  public fun <T : Any> launch(
    scope: CoroutineScope,
    spec: TreehouseApp.Spec<T>,
  ): TreehouseApp<T> {
    val dispatchers = platform.dispatchers
    val treehouseApp = TreehouseApp<T>(
      scope = scope,
      dispatchers = dispatchers,
      viewBinder = RealViewBinder(dispatchers, spec.viewBinderAdapter),
    )

    scope.launch(dispatchers.zipline) {
      val ziplineFileFlow = ziplineFlow(spec)
      ziplineFileFlow.collect {
        val app = spec.create(it.zipline)
        treehouseApp.onCodeChanged(it.zipline, app)
      }
    }

    return treehouseApp
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadedZipline] instance when new code is
   * found.
   */
  private fun ziplineFlow(spec: TreehouseApp.Spec<*>): Flow<LoadedZipline> {
    val manifestUrlFlowForLoad = when (spec.freshCodePolicy) {
      FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY -> spec.manifestUrl.rebounce(500.milliseconds)
      else -> spec.manifestUrl
    }

    return ziplineLoader.load(
      spec.name,
      manifestUrlFlowForLoad,
    ) { zipline ->
      spec.bindServices(zipline)
    }
  }
}
