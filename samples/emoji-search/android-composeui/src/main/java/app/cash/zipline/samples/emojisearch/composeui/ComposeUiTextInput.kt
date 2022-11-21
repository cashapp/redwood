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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.LayoutModifier
import example.schema.widget.TextInput

class ComposeUiTextInput : TextInput<@Composable () -> Unit> {
  private var hint by mutableStateOf("")
  private var text by mutableStateOf("")
  private var onTextChanged: ((String) -> Unit)? = null

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value = @Composable {
    TextField(
      value = text,
      onValueChange = { onTextChanged?.invoke(it) },
      label = { Text(hint) },
      maxLines = 2,
    )
  }

  override fun hint(hint: String) {
    this.hint = hint
  }

  override fun text(text: String) {
    this.text = text
  }

  override fun onTextChanged(onTextChanged: ((String) -> Unit)?) {
    this.onTextChanged = onTextChanged
  }
}
