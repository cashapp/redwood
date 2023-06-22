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

import app.cash.redwood.treehouse.AppLifecycle.Host
import assertk.all
import assertk.assertThat
import assertk.assertions.isLessThan
import assertk.assertions.isPositive
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AbstractFrameClockTest {
  internal abstract val frameClock: FrameClock

  @Test fun ticksWithTime() = runTest {
    val dispatchers = object : TreehouseDispatchers {
      override val ui = UnconfinedTestDispatcher(testScheduler)
      override val zipline get() = ui
      override fun checkUi() {}
      override fun checkZipline() {}
      override fun close() {}
    }
    frameClock.start(this, dispatchers)

    val frameTimes = Channel<Long>(Channel.UNLIMITED)
    val appLifecycle = object : AppLifecycle {
      override fun start(host: Host) {
      }
      override fun sendFrame(timeNanos: Long) {
        check(frameTimes.trySend(timeNanos).isSuccess)
      }
    }

    frameClock.requestFrame(appLifecycle)
    val frameTimeA = frameTimes.receive()

    frameClock.requestFrame(appLifecycle)
    val frameTimeB = frameTimes.receive()

    assertThat(frameTimeA).all {
      isPositive()
      isLessThan(frameTimeB)
    }
  }
}
