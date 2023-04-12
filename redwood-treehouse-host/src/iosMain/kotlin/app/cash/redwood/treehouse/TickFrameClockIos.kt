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
 * This just sends frames at 60hz.
 *
 * TODO: get a frame pulse from the host platform.
 */
internal actual fun CoroutineScope.tickFrameClock(
  dispatchers: TreehouseDispatchers,
  clockService: FrameClockService,
) {
  val ticksPerSecond = 60
  var now = 0L
  val delayNanos = 1_000_000_000L / ticksPerSecond
  launch(dispatchers.zipline) {
    while (true) {
      clockService.sendFrame(now)
      delay(delayNanos / 1_000_000)
      now += delayNanos
    }
  }
}
