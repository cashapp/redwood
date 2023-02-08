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
package app.cash.redwood.protocol.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.MonotonicFrameClock
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.DiffSink
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class DiffAwaitingFrameClock : MonotonicFrameClock, DiffSink {
  private val clock = BroadcastFrameClock()
  private val diffs = Channel<Diff>(Channel.UNLIMITED)

  override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
    return clock.withFrameNanos(onFrame)
  }

  override fun sendDiff(diff: Diff) {
    check(diffs.trySend(diff).isSuccess)
  }

  suspend fun awaitDiff(timeout: Duration = 2.seconds): Diff = coroutineScope {
    val tick = launch {
      while (true) {
        clock.sendFrame(0L)
        delay(16)
      }
    }
    val diff = withTimeout(timeout) {
      diffs.receive()
    }
    tick.cancelAndJoin()
    diff
  }
}
