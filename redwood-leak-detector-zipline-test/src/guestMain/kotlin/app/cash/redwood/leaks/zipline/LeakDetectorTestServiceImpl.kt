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
package app.cash.redwood.leaks.zipline

import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.leaks.LeakListener
import app.cash.redwood.leaks.zipline.LeakDetectorTestService.Companion.SERVICE_NAME
import app.cash.zipline.Zipline
import assertk.assertThat
import assertk.assertions.isSameInstanceAs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class LeakDetectorTestServiceImpl : LeakDetectorTestService {
  override fun leakDetectorDisabled() {
    val leakDetector = LeakDetector.timeBased(
      listener = object : LeakListener {
        override fun onReferenceCollected(name: String) {}
        override fun onReferenceLeaked(name: String, alive: Duration) {}
      },
      timeSource = TimeSource.Monotonic,
      leakThreshold = 2.seconds,
    )
    // QuickJS does not support WeakRef which is required for the leak detection to work correctly.
    // Once WeakRef is supported and this test starts failing, enable bridging of the real tests.
    assertThat(leakDetector).isSameInstanceAs(LeakDetector.None)
  }
}

fun main() {
  val zipline = Zipline.get()
  zipline.bind<LeakDetectorTestService>(SERVICE_NAME, LeakDetectorTestServiceImpl())
}
