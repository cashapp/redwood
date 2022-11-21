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

import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import app.cash.redwood.LayoutModifier
import example.schema.widget.TextInput

class ViewTextInput(
  override val value: EditText,
) : TextInput<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  init {
    value.maxLines = 2
  }

  override fun hint(hint: String) {
    value.hint = hint
  }

  override fun text(text: String) {
    value.setText(text)
    value.setSelection(text.length)
  }

  private var watcher: TextWatcher? = null
  override fun onTextChanged(onTextChanged: ((String) -> Unit)?) {
    watcher = if (onTextChanged == null) {
      watcher?.let(value::removeTextChangedListener)
      null
    } else {
      value.doOnTextChanged { _, _, _, _ ->
        onTextChanged.invoke(value.text.toString())
      }
    }
  }
}
