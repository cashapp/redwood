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

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SequentialStateFlowTest {

  @Test
  fun happyPath() = runTest {
    val ab = MutableStateFlow("a")
    val sequentialStateFlow = SequentialStateFlow(ab)
    assertEquals("a", sequentialStateFlow.value)

    sequentialStateFlow.test {
      assertEquals("a", awaitItem())
      assertEquals("a", sequentialStateFlow.value)

      ab.value = "b"
      assertEquals("b", sequentialStateFlow.value)
      assertEquals("b", awaitItem())

      val cd = MutableStateFlow("c")
      sequentialStateFlow.stateFlowValue.value = cd
      assertEquals("c", sequentialStateFlow.value)
      assertEquals("c", awaitItem())

      cd.value = "d"
      assertEquals("d", sequentialStateFlow.value)
      assertEquals("d", awaitItem())
    }
  }

  @Test
  fun valuesNotEmittedAfterStateFlowChanges() = runTest {
    val ab = MutableStateFlow("a")
    val sequentialStateFlow = SequentialStateFlow(ab)
    sequentialStateFlow.test {
      assertEquals("a", awaitItem())

      val cd = MutableStateFlow("c")
      sequentialStateFlow.stateFlowValue.value = cd
      assertEquals("c", awaitItem())

      ab.value = "b" // The flow should not emit this value!
      cd.value = "d"
      assertEquals("d", awaitItem())
    }
  }

  @Test
  fun stateFlowChanges() = runTest {
    val ab = MutableStateFlow("a")
    val sequentialStateFlow = SequentialStateFlow(ab)
    sequentialStateFlow.test {
      assertEquals("a", awaitItem())

      val cd = MutableStateFlow("c")
      sequentialStateFlow.stateFlowValue.value = cd

      val ef = MutableStateFlow("e")
      sequentialStateFlow.stateFlowValue.value = ef

      // Because Turbine receives without suspending, emit("c") completes before that collect is canceled.
      assertEquals("c", awaitItem())
      assertEquals("e", awaitItem())
    }
  }
}
