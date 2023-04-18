/*
 * Copyright (C) 2023 Square, Inc.
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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.api.Dp
import app.cash.redwood.layout.api.dp
import app.cash.redwood.layout.widget.Spacer

internal class ComposeUiSpacer : Spacer<@Composable () -> Unit> {
  private var width by mutableStateOf(0.dp)
  private var height by mutableStateOf(0.dp)

  override val value = @Composable {
    val minWidth = width.toComposeUiDp()
    val minHeight = height.toComposeUiDp()
    Spacer(Modifier.defaultMinSize(minWidth, minHeight))
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun width(width: Dp) {
    this.width = width
  }

  override fun height(height: Dp) {
    this.height = height
  }
}
