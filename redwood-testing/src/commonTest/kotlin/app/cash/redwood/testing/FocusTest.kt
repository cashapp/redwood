/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.testing

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.compose.rememberFocusRequester
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.basic.api.TextFieldState
import app.cash.redwood.ui.basic.compose.TextInput
import app.cash.redwood.ui.basic.testing.TextInputValue
import app.cash.redwood.ui.core.compose.focusRequester
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.redwood.testapp.testing.TestSchemaTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class FocusTest {
  @Test
  fun happyPath() = runTest {
    TestSchemaTester {
      var step by mutableIntStateOf(0)

      setContent {
        val nameFocusRequester = rememberFocusRequester()
        val colorFocusRequester = rememberFocusRequester()

        LaunchedEffect(step) {
          when (step) {
            1 -> nameFocusRequester.requestFocus()
            2 -> colorFocusRequester.requestFocus()
          }
        }

        var nameState by remember { mutableStateOf(TextFieldState()) }
        var colorState by remember { mutableStateOf(TextFieldState()) }

        Column {
          TextInput(
            modifier = Modifier.focusRequester(nameFocusRequester),
            state = nameState,
            hint = "name",
          )
          TextInput(
            modifier = Modifier.focusRequester(colorFocusRequester),
            state = colorState,
            hint = "color",
          )
        }
      }

      with(awaitSnapshot()) {
        assertThat(focusDirector.getFocused(this)).isNull()
      }

      // Activate step 1 to request focus on the name TextInput.
      step = 1
      with(awaitSnapshot()) {
        val nameInput = flatten().first { (it as? TextInputValue)?.hint == "name" }
        assertThat(focusDirector.getFocused(this)).isEqualTo(nameInput)
      }

      // Activate step 2 to request focus on the color TextInput.
      step = 2
      with(awaitSnapshot()) {
        val colorInput = flatten().first { (it as? TextInputValue)?.hint == "color" }
        assertThat(focusDirector.getFocused(this)).isEqualTo(colorInput)
      }
    }
  }

  @Test
  fun focusOnDetachedWidgetDoesNothing() = runTest {
    TestSchemaTester {
      var step by mutableIntStateOf(0)

      setContent {
        val nameFocusRequester = rememberFocusRequester()

        LaunchedEffect(step) {
          when (step) {
            1 -> nameFocusRequester.requestFocus()
            2 -> nameFocusRequester.requestFocus()
          }
        }

        var nameState by remember { mutableStateOf(TextFieldState()) }

        Column {
          if (step != 1) {
            TextInput(
              modifier = Modifier.focusRequester(nameFocusRequester),
              state = nameState,
              hint = "name",
            )
          }
        }
      }

      with(awaitSnapshot()) {
        assertThat(focusDirector.getFocused(this)).isNull()
      }

      // Activate step 1 to request focus on the name TextInput. This will fail as that component
      // is no longer in the composition.
      step = 1
      with(awaitSnapshot()) {
        assertThat(focusDirector.getFocused(this)).isNull()
      }

      // Activate step 2 to request focus on the name TextInput.
      step = 2
      with(awaitSnapshot()) {
        val nameInput = flatten().first { (it as? TextInputValue)?.hint == "name" }
        assertThat(focusDirector.getFocused(this)).isEqualTo(nameInput)
      }
    }
  }
}
