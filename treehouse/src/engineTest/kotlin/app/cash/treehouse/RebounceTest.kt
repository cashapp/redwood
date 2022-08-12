/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.treehouse

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RebounceTest {
  @Test
  fun repeatsLatestValueOnSchedule() = runTest {
    val flow = MutableStateFlow("a")

    val log = ArrayDeque<String>()
    val job = launch {
      val rebounced = flow.rebounce(100.milliseconds)
      rebounced.collect {
        log += "${testScheduler.currentTime}: $it"
      }
    }
    runCurrent()

    flow.emit("b")
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    job.cancel()

    assertEquals(
      listOf(
        "0: a",
        "0: b",
        "100: b",
        "200: b",
        "300: b",
      ),
      log.toList(),
    )
  }

  @Test
  fun neverEmitsWithNoFirstValue() = runTest {
    val flow = emptyFlow<String>()

    val log = ArrayDeque<String>()
    val job = launch {
      val rebounced = flow.rebounce(100.milliseconds)
      rebounced.collect {
        log += "${testScheduler.currentTime}: $it"
      }
    }
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    job.cancel()

    assertEquals(
      listOf(),
      log.toList(),
    )
  }

  @Test
  fun newValueEmittedImmediately() = runTest {
    val flow = MutableStateFlow("a")

    val log = ArrayDeque<String>()
    val job = launch {
      val rebounced = flow.rebounce(100.milliseconds)
      rebounced.collect {
        log += "${testScheduler.currentTime}: $it"
      }
    }
    runCurrent()

    flow.emit("b")
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    advanceTimeBy(10)
    flow.emit("c")
    runCurrent()

    advanceTimeBy(100)
    runCurrent()

    job.cancel()

    assertEquals(
      listOf(
        "0: a",
        "0: b",
        "100: b",
        "110: c",
        "210: c",
      ),
      log.toList(),
    )
  }
}
