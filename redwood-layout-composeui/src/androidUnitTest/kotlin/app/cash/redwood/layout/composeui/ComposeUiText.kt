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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.Transparent

class ComposeUiText : Text<@Composable () -> Unit> {
  private var text by mutableStateOf("")
  private var bgColor by mutableStateOf(Transparent)

  override val value = @Composable {
    BasicText(
      text = this.text,
      style = TextStyle(fontSize = 18.sp, color = Color.Black),
      modifier = Modifier.background(Color(bgColor)),
    )
  }

  override var modifier: RedwoodModifier = RedwoodModifier

  override fun text(text: String) {
    this.text = text
  }

  override fun bgColor(color: Int) {
    bgColor = color
  }
}
