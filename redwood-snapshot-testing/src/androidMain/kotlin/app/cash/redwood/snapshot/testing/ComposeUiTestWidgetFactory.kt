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
package app.cash.redwood.snapshot.testing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp as ComposeDp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.snapshot.testing.Color as ColorWidget
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.toPlatformDp

object ComposeUiTestWidgetFactory : TestWidgetFactory<@Composable () -> Unit> {
  override fun color(): ColorWidget<@Composable () -> Unit> = ComposeUiColor()

  override fun text(): Text<@Composable () -> Unit> = ComposeUiText()
}

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

  override val measureCount = 0

  override fun text(text: String) {
    this.text = text
  }

  override fun bgColor(color: Int) {
    bgColor = color
  }
}

class ComposeUiColor : ColorWidget<@Composable () -> Unit> {
  private var width by mutableStateOf(0.dp)
  private var height by mutableStateOf(0.dp)
  private var color by mutableStateOf(Transparent)

  override val value = @Composable {
    Spacer(Modifier.size(width, height).background(Color(color)))
  }

  override fun width(width: Dp) {
    this.width = width.toDp()
  }

  override fun height(height: Dp) {
    this.height = height.toDp()
  }

  override fun color(color: Int) {
    this.color = color
  }

  override var modifier: RedwoodModifier = RedwoodModifier
}

internal fun Dp.toDp(): ComposeDp {
  return ComposeDp(toPlatformDp().toFloat())
}
