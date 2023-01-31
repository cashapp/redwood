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

package app.cash.redwood.compose.testing

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout

/**
 * Performs Redwood composition strictly for testing.
 *
 * Create an instance with a generated `AppNameTester()` function.
 */
@OptIn(RedwoodCodegenApi::class)
public class RedwoodTester @RedwoodCodegenApi constructor(
  scope: CoroutineScope,
  private val changeTracker: ChangeTracker,
  provider: Widget.Provider<MutableWidget>,
) {
  /** Emit frames manually in [sendFrames]. */
  private val clock = BroadcastFrameClock()
  private var timeNanos = 0L
  private val frameDelay = 1.seconds / 60

  /** Top-level children of the composition. */
  private val mutableChildren = mutableListOf<Widget<MutableWidget>>()

  private val composition = RedwoodComposition(
    scope = scope + clock,
    container = MutableListChildren(mutableChildren),
    provider = provider,
  )

  /** Execute [testBody] and then cancel this tester. */
  public suspend fun test(testBody: suspend RedwoodTester.() -> Unit) {
    try {
      testBody()
    } finally {
      cancel()
    }
  }

  public fun setContent(content: @Composable () -> Unit) {
    composition.setContent(content)
  }

  /**
   * Returns a snapshot, waiting if necessary for changes to occur since the previous snapshot.
   *
   * @throws TimeoutCancellationException if no new snapshot is produced before [timeoutMillis].
   */
  public suspend fun awaitSnapshot(timeoutMillis: Long = 1_000): List<WidgetValue> {
    // Await at least one change, sending frames while we wait.
    withTimeout(timeoutMillis) {
      val sendFramesJob = sendFrames()

      changeTracker.changes.acquire()
      sendFramesJob.cancel()
    }

    val snapshot = mutableChildren.map { it.value.snapshot() }

    // Consume any extra changes on the returned snapshot.
    changeTracker.changes.acquireAll()

    return snapshot
  }

  /** Launches a job that sends a frame immediately and again every 16 ms until it's canceled. */
  private fun CoroutineScope.sendFrames(): Job {
    return launch {
      while (true) {
        clock.sendFrame(timeNanos)
        timeNanos += frameDelay.inWholeNanoseconds
        delay(frameDelay)
      }
    }
  }

  public fun cancel() {
    composition.cancel()
  }
}
