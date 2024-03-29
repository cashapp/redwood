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
package app.cash.redwood.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.testing.TestRedwoodComposition
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.single
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.testing.TestSchemaTester
import com.example.redwood.testing.testing.TextValue
import kotlin.test.Test
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.test.runTest

private val throwingOnBack = { error("Only the innermost enabled back handler should be invoked.") }

class BackHandlerTest {
  @Test
  fun enabledBackHandler() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        var backCounter by remember { mutableStateOf(0) }
        BackHandler {
          backCounter++
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "0"))
      onBackPressedDispatcher.onBackPressedCallbacks.single { it.isEnabled }.handleOnBackPressed()
      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "1"))
    }
  }

  @Test
  fun disabledBackHandler() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        val backCounter by remember { mutableStateOf(0) }
        BackHandler(enabled = false, throwingOnBack)
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(false)
      assertNoSnapshot()
    }
  }

  @Test
  fun disabledToEnabledBackHandler() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      var enabled by mutableStateOf(false)
      setContent {
        var backCounter by remember { mutableStateOf(0) }
        BackHandler(enabled) {
          backCounter++
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(false)
      assertNoSnapshot()
      enabled = true
      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "0"))
      onBackPressedDispatcher.onBackPressedCallbacks.single { it.isEnabled }.handleOnBackPressed()
      assertThat(awaitSnapshot()).single().isEqualTo(TextValue(text = "1"))
    }
  }

  @Test
  fun outermostEnabledAndInnermostEnabledBackHandlers() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        var backCounter by remember { mutableStateOf(0) }
        BackHandler(enabled = false, throwingOnBack)
        Box {
          BackHandler {
            backCounter += 1
          }
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).contains(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(false, true)
      onBackPressedDispatcher.onBackPressedCallbacks.last().handleOnBackPressed()
      assertThat(awaitSnapshot()).contains(TextValue(text = "1"))
    }
  }

  @Test
  fun outermostEnabledAndInnermostDisabledBackHandlers() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        var backCounter by remember { mutableStateOf(0) }
        BackHandler {
          backCounter += 1
        }
        Box {
          BackHandler(enabled = false, throwingOnBack)
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).contains(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(true, false)
      onBackPressedDispatcher.onBackPressedCallbacks.first().handleOnBackPressed()
      assertThat(awaitSnapshot()).contains(TextValue(text = "1"))
    }
  }

  @Test
  fun outermostDisabledAndInnermostEnabledBackHandlers() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        var backCounter by remember { mutableStateOf(0) }
        BackHandler(enabled = false, throwingOnBack)
        Box {
          BackHandler {
            backCounter += 1
          }
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).contains(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(false, true)
      onBackPressedDispatcher.onBackPressedCallbacks.last().handleOnBackPressed()
      assertThat(awaitSnapshot()).contains(TextValue(text = "1"))
    }
  }

  @Test
  fun outermostDisabledAndInnermostDisabledBackHandlers() = runTest {
    val onBackPressedDispatcher = FakeOnBackPressedDispatcher()
    TestSchemaTester(onBackPressedDispatcher) {
      setContent {
        val backCounter by remember { mutableStateOf(0) }
        BackHandler(enabled = false, throwingOnBack)
        Box {
          BackHandler(enabled = false, throwingOnBack)
        }
        Text(backCounter.toString())
      }

      assertThat(awaitSnapshot()).contains(TextValue(text = "0"))
      assertThat(onBackPressedDispatcher.onBackPressedCallbacks).extracting { it.isEnabled }.containsExactly(false, false)
      assertNoSnapshot()
    }
  }
}

private suspend fun TestRedwoodComposition<List<WidgetValue>>.assertNoSnapshot() {
  assertFailure { awaitSnapshot() }.isInstanceOf<TimeoutCancellationException>()
}

private class FakeOnBackPressedDispatcher : OnBackPressedDispatcher {
  private val _onBackPressedCallbacks = ArrayDeque<OnBackPressedCallback>()
  val onBackPressedCallbacks: List<OnBackPressedCallback> = _onBackPressedCallbacks

  override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
    _onBackPressedCallbacks += onBackPressedCallback
    return object : Cancellable {
      override fun cancel() {
        _onBackPressedCallbacks -= onBackPressedCallback
      }
    }
  }
}
