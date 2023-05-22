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
package com.example.redwood.emojisearch.android.composeui

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.redwood.Modifier
import com.example.redwood.emojisearch.widget.TextInput
import example.values.TextFieldState
import kotlin.coroutines.EmptyCoroutineContext

internal class ComposeUiTextInput : TextInput<@Composable () -> Unit> {
  private var state by mutableStateOf(TextFieldState())
  private var hint by mutableStateOf("")
  private var onChange: ((TextFieldState) -> Unit)? = null
  private var updating = false

  override var modifier: Modifier = Modifier

  override val value = @Composable {
    // Preserve 'composition' and other state properties that we don't modify.
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    TextField(
      value = textFieldValue.copy(
        text = state.text,
        selection = TextRange(state.selectionStart, state.selectionEnd),
      ),
      label = {
        if (hint.isNotEmpty()) {
          Text(hint)
        }
      },
      maxLines = 2,
      onValueChange = { newValue ->
        textFieldValue = newValue
        stateChanged(newValue)
      },
    )
  }

  /**
   * Handle state changes from Treehouse. These will often be based on out-of-date user edits,
   * in which case we discard the Treehouse update. Eventually the user will stop typing and we'll
   * make the update without interrupting them.
   */
  override fun state(state: TextFieldState) {
    if (state.userEditCount < this.state.userEditCount) return

    check(!updating)
    try {
      updating = true
      this.state = state
    } finally {
      updating = false
    }
  }

  override fun hint(hint: String) {
    this.hint = hint
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    this.onChange = onChange
  }

  /**
   * Handle state changes from the user. When these happen we save the new state, which has a
   * new [TextFieldState.userEditCount]. That way we can ignore updates that are based on stale
   * data.
   */
  private fun stateChanged(value: TextFieldValue) {
    // Ignore this update if it isn't a user edit.
    if (updating) return

    val newState = state.userEdit(
      text = value.text,
      selectionStart = value.selection.start,
      selectionEnd = value.selection.end,
    )
    if (!state.contentEquals(newState)) {
      state = newState
      onChange?.invoke(newState)
    }
  }
}
