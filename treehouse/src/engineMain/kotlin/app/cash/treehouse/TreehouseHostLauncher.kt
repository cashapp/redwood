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

import app.cash.treehouse.FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY
import app.cash.zipline.loader.LoadedZipline
import app.cash.zipline.loader.ZiplineLoader
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Creates the Zipline and binds its core interfaces.
 *
 * This works with [TreehouseHost] to create new [Zipline] instances when code is initially loaded
 * or reloaded.
 */
public class TreehouseHostLauncher<T : Any>(
  private val dispatchers: TreehouseDispatchers,
  private val viewBinderAdapter: ViewBinder.Adapter,
  private val launcher: TreehouseAppLauncher<T>,
  private val manifestUrlFlow: Flow<String>,
  private val ziplineLoader: ZiplineLoader,
  private val freshCodePolicy: FreshCodePolicy,
) {
  private val scope: CoroutineScope = MainScope()

  public fun launch(): TreehouseHost<T> {
    val treehouseHost = TreehouseHost<T>(
      scope = scope,
      dispatchers = dispatchers,
      viewBinder = RealViewBinder(dispatchers, viewBinderAdapter),
    )

    scope.launch(dispatchers.zipline) {
      val ziplineFileFlow = ziplineFlow()
      ziplineFileFlow.collect {
        val app = launcher.create(it.zipline)
        treehouseHost.onCodeChanged(it.zipline, app)
      }
    }

    return treehouseHost
  }

  /**
   * Continuously polls for updated code, and emits a new [LoadedZipline] instance when new code is
   * found.
   */
  private suspend fun ziplineFlow(): Flow<LoadedZipline> {
    val manifestUrlFlowForLoad = when (freshCodePolicy) {
      ALWAYS_REFRESH_IMMEDIATELY -> manifestUrlFlow.rebounce(500.milliseconds)
      else -> manifestUrlFlow
    }

    return ziplineLoader.load(
      launcher.applicationName,
      manifestUrlFlowForLoad,
    ) { zipline ->
      launcher.bindServices(zipline)
    }
  }
}
