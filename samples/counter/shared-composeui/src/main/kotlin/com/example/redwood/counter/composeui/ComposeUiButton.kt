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
package com.example.redwood.counter.composeui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.cash.redwood.Modifier as RedwoodModifier
import com.example.redwood.counter.widget.Button

internal class ComposeUiButton : Button<@Composable () -> Unit> {
  private var text by mutableStateOf("")
  private var isEnabled by mutableStateOf(false)
  private var onClick by mutableStateOf({})

  override var modifiers: RedwoodModifier = RedwoodModifier

  override val value = @Composable {
    Button(
      onClick = onClick,
      enabled = isEnabled,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text)
    }
  }

  override fun text(text: String?) {
    this.text = text ?: ""
  }

  override fun enabled(enabled: Boolean) {
    this.isEnabled = enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    this.onClick = onClick ?: {}
  }
}
