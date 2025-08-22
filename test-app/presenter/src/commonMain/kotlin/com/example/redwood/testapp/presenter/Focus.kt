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
package com.example.redwood.testapp.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.compose.rememberFocusRequester
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Stretch
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.basic.api.TextFieldState
import app.cash.redwood.ui.basic.compose.TextInput
import app.cash.redwood.ui.core.compose.focusRequester
import com.example.redwood.testapp.compose.Button

@Composable
fun Focus(modifier: Modifier = Modifier) {
  var nextFocusRequesterIndex by remember { mutableIntStateOf(0) }

  val inputs = listOf(
    remember { mutableStateOf(TextFieldState("A")) } to rememberFocusRequester(),
    remember { mutableStateOf(TextFieldState("B")) } to rememberFocusRequester(),
    remember { mutableStateOf(TextFieldState("C")) } to rememberFocusRequester(),
  )

  Column(width = Fill, horizontalAlignment = Stretch, modifier = modifier) {
    for ((state, focusRequester) in inputs) {
      TextInput(
        modifier = Modifier.focusRequester(focusRequester),
        state = state.value,
        onChange = { update ->
          state.value = update
        },
      )
    }

    Button(
      text = "Move Focus",
      modifier = Modifier,
      onClick = {
        val (state, focusRequester) = inputs[nextFocusRequesterIndex++ % inputs.size]
        state.selectAll()
        focusRequester.requestFocus()
      },
    )
  }
}

private fun MutableState<TextFieldState>.selectAll() {
  value = value.copy(
    selectionStart = 0,
    selectionEnd = value.text.length,
  )
}
