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
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.prop
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class LeakDetectorTest {
  private val timeSource = TestTimeSource()
  private val listener = RecordingLeakListener()
  private val leakDetector = LeakDetector.timeBased(listener, timeSource, 10.seconds)
  private var ref: Any? = Any()

  @BeforeTest fun before() {
    assertThat(leakDetector)
      .isInstanceOf<TimeBasedLeakDetector>()
      .prop(TimeBasedLeakDetector::gc)
      .isNotSameInstanceAs(Gc.None)
  }

  private suspend fun wait(duration: Duration) {
    // Advance the virtual time against which leak thresholds are compared.
    timeSource += duration
    // Use a delay indirection that might use wall clock time where required.
    delayForGc(duration)
  }

  @Test fun detectImmediateCollection() = runTest {
    leakDetector.watchReference(ref!!, "ref")

    ref = null
    wait(10.seconds)

    leakDetector.checkLeaks()
    assertThat(listener.events).containsExactly("collected: ref")
  }

  @Test fun detectDelayedCollection() = runTest {
    leakDetector.watchReference(ref!!, "ref")

    wait(5.seconds)

    leakDetector.checkLeaks()
    assertThat(listener.events).isEmpty()

    ref = null

    wait(5.seconds)

    leakDetector.checkLeaks()
    assertThat(listener.events).containsExactly("collected: ref")
  }

  @Test fun detectLeak() = runTest {
    leakDetector.watchReference(ref!!, "ref")

    // Only advance virtual time for checking the event message.
    timeSource += 15.seconds

    leakDetector.checkLeaks()
    assertThat(listener.events).containsExactly("leaked @ 15s: ref")
  }

  @Test fun concurrencyStressTest() = runTest {
    coroutineScope {
      repeat(10_000) { i ->
        launch(Dispatchers.Default) {
          leakDetector.watchReference(Any(), "$i")
        }
      }
      repeat(100) {
        launch(Dispatchers.Default) {
          leakDetector.checkLeaks()
        }
      }
    }
  }
}
