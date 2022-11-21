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
package app.cash.zipline.samples.emojisearch.views

import android.content.Context
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.TreehouseDispatchers
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import example.schema.widget.TextInput
import example.values.TextFieldState
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher

class ViewTextInput(
  context: Context,
  private val dispatchers: TreehouseDispatchers,
) : TextInput<View> {
  private var state = TextFieldState()
  private var onChange: ((TextFieldState) -> Unit)? = null
  private var updating = false

  override val value = object : TextInputEditText(context) {
    init {
      addTextChangedListener(
        onTextChanged = { _, _, _, _ ->
          stateChanged(this)
        }
      )
      maxLines = 2
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
      super.onSelectionChanged(selStart, selEnd)
      stateChanged(this)
    }
  }
  override var layoutModifiers: LayoutModifier = LayoutModifier

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
      value.setText(state.text)
      value.setSelection(state.selectionStart, state.selectionEnd)
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
      dispatchers.zipline.dispatch(
        EmptyCoroutineContext,
        Runnable { onChange?.invoke(newState) },
      )
    }
  }
}
