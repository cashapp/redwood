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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A [FrameClock] that suspends with a fixed delay. This does not match the host app's frame pulse.
 *
 * Prefer a platform-specific frame clock whenever possible.
 */
internal class FixedDelayFrameClock : FrameClock {
  private var appLifecycle: AppLifecycle? = null

  override fun start(
    scope: CoroutineScope,
    dispatchers: TreehouseDispatchers,
  ) {
    scope.launch(dispatchers.zipline) {
      run()
    }
  }

  private suspend fun run() {
    val ticksPerSecond = 60
    var now = 0L
    val delayNanos = 1_000_000_000L / ticksPerSecond
    while (true) {
      appLifecycle?.sendFrame(now)
      appLifecycle = null
      delay(delayNanos / 1_000_000)
      now += delayNanos
    }
  }

  override fun requestFrame(appLifecycle: AppLifecycle) {
    require(this.appLifecycle == null)
    this.appLifecycle = appLifecycle
  }
}
