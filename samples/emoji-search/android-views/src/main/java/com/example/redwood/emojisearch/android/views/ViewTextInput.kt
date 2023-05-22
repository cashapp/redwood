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
package com.example.redwood.emojisearch.android.views

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import app.cash.redwood.Modifier
import com.example.redwood.emojisearch.widget.TextInput
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import example.values.TextFieldState
import kotlin.coroutines.EmptyCoroutineContext

internal class ViewTextInput(
  context: Context,
) : TextInput<View> {
  private var state = TextFieldState()
  private var onChange: ((TextFieldState) -> Unit)? = null
  private var updating = false

  private val textInputLayout = TextInputLayout(context)
  private val textInputEditText = object : TextInputEditText(textInputLayout.context) {
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
      super.onSelectionChanged(selStart, selEnd)
      stateChanged(this)
    }
  }

  override val value get() = textInputLayout
  override var modifiers: Modifier = Modifier

  init {
    textInputLayout.addView(textInputEditText)
    textInputLayout.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    textInputEditText.addTextChangedListener(
      onTextChanged = { _, _, _, _ ->
        stateChanged(textInputEditText)
      }
    )
    textInputEditText.maxLines = 2
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
      textInputEditText.setText(state.text)
      textInputEditText.setSelection(state.selectionStart, state.selectionEnd)
    } finally {
      updating = false
    }
  }

  override fun hint(hint: String) {
    value.hint = hint
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    this.onChange = onChange
  }

  /**
   * Handle state changes from the user. When these happen we save the new state, which has a
   * new [TextFieldState.userEditCount]. That way we can ignore updates that are based on stale
   * data.
   */
  private fun stateChanged(editText: EditText) {
    // Ignore this update if it isn't a user edit.
    if (updating) return

    val newState = state.userEdit(
      text = editText.text?.toString().orEmpty(),
      selectionStart = editText.selectionStart,
      selectionEnd = editText.selectionEnd,
    )
    if (!state.contentEquals(newState)) {
      state = newState
      onChange?.invoke(newState)
    }
  }
}
