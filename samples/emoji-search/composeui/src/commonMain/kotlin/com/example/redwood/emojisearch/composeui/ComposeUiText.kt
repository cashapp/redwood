/*
 * Copyright (C) 2024 Square, Inc.
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
package com.example.redwood.emojisearch.composeui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import com.example.redwood.emojisearch.widget.Text

internal class ComposeUiText : Text<@Composable () -> Unit> {
  private var text by mutableStateOf("")

  override var modifier: Modifier = Modifier

  override val value = @Composable {
    Text(
      text = text,
      color = MaterialTheme.colors.onBackground,
    )
  }

  override fun text(text: String) {
    this.text = text
  }
}
