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
package com.example.redwood.counter.presenter

import app.cash.redwood.testing.TestRedwoodComposition
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.testing.flatten
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.redwood.counter.widget.ButtonValue
import com.example.redwood.counter.widget.SchemaTester
import com.example.redwood.counter.widget.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class CounterTest {
  @Test fun counts() = runTest {
    SchemaTester {
      setCounter()

      awaitSnapshot().apply {
        assertThat(labelText()).isEqualTo("Count: 0")
        clickButton("+1")
      }
      awaitSnapshot().apply {
        assertThat(labelText()).isEqualTo("Count: 1")
        clickButton("+1")
      }
      awaitSnapshot().apply {
        assertThat(labelText()).isEqualTo("Count: 2")
        clickButton("-1")
      }
      awaitSnapshot().apply {
        assertThat(labelText()).isEqualTo("Count: 1")
      }
    }
  }

  @Test fun savesCount() = runTest {
    val state = SchemaTester {
      setCounter()
      awaitSnapshot().clickButton("+1")
      awaitSnapshot().clickButton("+1")
      saveState()
    }
    SchemaTester(savedState = state) {
      setCounter()
      assertThat(awaitSnapshot().labelText()).isEqualTo("Count: 2")
    }
  }

  private fun TestRedwoodComposition<List<WidgetValue>>.setCounter() {
    // Defined in its own function to ensure state restoration works. The content lambda
    // across both compositions must be the same to produce the same implicit keys.
    setContent { Counter() }
  }

  private fun List<WidgetValue>.clickButton(text: String) {
    val button = flatten()
      .filterIsInstance<ButtonValue>()
      .firstOrNull { it.text == text }
      ?: throw IllegalStateException("No button with text: $text")
    button.onClick?.invoke()
  }

  private fun List<WidgetValue>.labelText(): String {
    return flatten()
      .filterIsInstance<TextValue>()
      .firstOrNull()
      ?.text
      ?: throw IllegalStateException("No Text found.")
  }
}
