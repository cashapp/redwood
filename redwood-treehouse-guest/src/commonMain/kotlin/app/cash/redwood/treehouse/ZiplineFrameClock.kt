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

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.MonotonicFrameClock

/** A guest frame clock that asks the host for frames. */
internal class ZiplineFrameClock(
  private val host: AppLifecycle.Host,
) : MonotonicFrameClock {
  /** Get [BroadcastFrameClock] to keep track of suspending callers. */
  private val delegate = BroadcastFrameClock()

  /** True if we've requested a frame from [host]. */
  private var awaitingFrame = false

  fun sendFrame(timeNanos: Long) {
    awaitingFrame = false
    return delegate.sendFrame(timeNanos)
  }

  override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
    if (!awaitingFrame) {
      awaitingFrame = true
      host.requestFrame()
    }
    return delegate.withFrameNanos(onFrame)
  }
}
