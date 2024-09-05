/*
 * Copyright (C) 2023 Square, Inc.
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

import kotlinx.cinterop.convert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.posix.CLOCK_MONOTONIC_RAW
import platform.posix.clock_gettime_nsec_np

internal class IosDisplayLinkClock private constructor(
  private val scope: CoroutineScope,
  private val dispatchers: TreehouseDispatchers,
) : FrameClock {
  /** Non-null if we're expecting a call to [AppLifecycle.sendFrame]. */
  private var appLifecycle: AppLifecycle? = null

  private var displayLinkTarget: DisplayLinkTarget? = DisplayLinkTarget {
    unsubscribe()
    scope.launch(dispatchers.zipline) {
      val nanos = clock_gettime_nsec_np(CLOCK_MONOTONIC_RAW.convert()).convert<Long>()
      appLifecycle?.sendFrame(nanos)
      appLifecycle = null
    }
  }

  override fun requestFrame(appLifecycle: AppLifecycle) {
    this.appLifecycle = appLifecycle
    scope.launch(dispatchers.ui) {
      displayLinkTarget?.subscribe()
    }
  }

  override fun close() {
    appLifecycle = null
    displayLinkTarget?.close()
    displayLinkTarget = null // Break a reference cycle.
  }

  companion object : FrameClock.Factory {
    override fun create(
      scope: CoroutineScope,
      dispatchers: TreehouseDispatchers,
    ) = IosDisplayLinkClock(scope, dispatchers)
  }
}
