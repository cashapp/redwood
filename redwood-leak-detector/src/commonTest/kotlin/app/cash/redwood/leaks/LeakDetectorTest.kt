/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.leaks

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.prop
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class LeakDetectorTest {
  private val scope = CoroutineScope(Dispatchers.Default)
  private val callback = RecordingLeakCallback()
  private val leakDetector = LeakDetector.timeBasedIn(scope, TimeSource.Monotonic, 10.seconds, callback)
  private var ref: Any? = object : Any() {
    override fun toString() = "ref"
  }

  @BeforeTest fun before() {
    assertThat(leakDetector)
      .isInstanceOf<TimeBasedLeakDetector>()
      .prop(TimeBasedLeakDetector::gc)
      .isNotSameInstanceAs(Gc.None)
  }

  @AfterTest fun after() {
    scope.cancel()
  }

  @Test fun detectCollection() = runTest {
    leakDetector.watchReference(ref!!, "note")
    ref = null
    leakDetector.awaitClose()
    assertThat(callback.events).isEmpty()
  }

  @Test fun detectLeak() = runTest {
    leakDetector.watchReference(ref!!, "note")
    leakDetector.awaitClose()
    assertThat(callback.events).containsExactly("leaked ref note")
  }

  @Test fun gcOnlyRunsWhenWatchingReferences() = runTest {
    var gcRuns = 0
    val gc = Gc { gcRuns += 1 }
    val leakDetector = TimeBasedLeakDetector(this, gc, testTimeSource, 10.seconds, callback)

    // No refs? No GC.
    advanceTimeByAndRunCurrent(30.seconds)
    assertThat(gcRuns).isEqualTo(0)

    leakDetector.watchReference(ref!!, "note")

    advanceTimeByAndRunCurrent(5.seconds)
    assertThat(gcRuns).isEqualTo(1)
    advanceTimeByAndRunCurrent(5.seconds)
    assertThat(gcRuns).isEqualTo(2)
    assertThat(callback.events).hasSize(1)

    // No refs? No GC.
    advanceTimeByAndRunCurrent(30.seconds)
    assertThat(gcRuns).isEqualTo(2)

    leakDetector.watchReference(ref!!, "note")

    advanceTimeByAndRunCurrent(5.seconds)
    assertThat(gcRuns).isEqualTo(3)
    advanceTimeByAndRunCurrent(5.seconds)
    assertThat(gcRuns).isEqualTo(4)
    assertThat(callback.events).hasSize(2)

    leakDetector.close()
  }

  private fun TestScope.advanceTimeByAndRunCurrent(duration: Duration) {
    advanceTimeBy(duration)
    runCurrent()
  }
}
