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
package app.cash.zipline.samples.emojisearch.composeui

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.redwood.LayoutModifier
import example.schema.widget.TextInput
import example.values.TextFieldState

class ComposeUiTextInput : TextInput<@Composable () -> Unit> {
  private var hint by mutableStateOf("")
  private var composeState by mutableStateOf(TextFieldValue())
  private var redwoodState by mutableStateOf(TextFieldState())
  private var onTextChanged: ((TextFieldState) -> Unit)? = null

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value = @Composable {
    // not sure which of these are correct
    // the first has one source of truth, but latency in the UI and a lot of TextField state bouncing around internally
    // the second results in less latency in the UI but two sources of truth
    val composeStateWithLastTextValue = composeState.copy(text = redwoodState.text)
    // val composeStateWithLastTextValue = composeState

    println("!!!Android - Recomposing with $composeStateWithLastTextValue")
    TextField(
      value = composeStateWithLastTextValue,
      onValueChange = { newComposeState ->
        println("!!!Android - onValueChange $newComposeState")
        composeState = newComposeState

        val stringChangedSinceLastInvocation = redwoodState.text != newComposeState.text

        onTextChanged?.invoke(
          redwoodState.copy(
            text = newComposeState.text,
            selectionStart = newComposeState.selection.start,
            selectionEnd = newComposeState.selection.end,
            userEditCount = when {
              stringChangedSinceLastInvocation -> redwoodState.userEditCount + 1
              else -> redwoodState.userEditCount
            }
          )
        )
      },
      label = { Text(hint) },
      maxLines = 2,
    )
  }

  override fun hint(hint: String) {
    this.hint = hint
  }

  override fun text(text: TextFieldState) {
    println("!!!Android Setting text to $text")
    this.redwoodState = text
    this.composeState = this.composeState.copy(
      text = text.text,
      selection = TextRange(text.selectionStart, text.selectionEnd)
    )
  }

  override fun onTextChanged(onTextChanged: ((TextFieldState) -> Unit)?) {
    this.onTextChanged = onTextChanged
  }
}
