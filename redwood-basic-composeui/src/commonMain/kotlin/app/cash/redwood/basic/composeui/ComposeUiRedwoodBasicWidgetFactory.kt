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
package app.cash.redwood.basic.composeui

import androidx.compose.runtime.Composable
import app.cash.redwood.basic.modifier.Reuse
import app.cash.redwood.basic.widget.Button
import app.cash.redwood.basic.widget.Image
import app.cash.redwood.basic.widget.RedwoodBasicWidgetFactory
import app.cash.redwood.basic.widget.Text
import app.cash.redwood.basic.widget.TextInput
import coil3.ImageLoader

public class ComposeUiRedwoodBasicWidgetFactory(
  private val imageLoader: ImageLoader,
) : RedwoodBasicWidgetFactory<@Composable () -> Unit> {
  override fun TextInput(): TextInput<@Composable () -> Unit> = ComposeUiTextInput()
  override fun Text(): Text<@Composable () -> Unit> = ComposeUiText()
  override fun Image(): Image<@Composable () -> Unit> = ComposeUiImage(imageLoader)
  override fun Button(): Button<@Composable (() -> Unit)> = ComposeUiButton()
  override fun Reuse(value: @Composable () -> Unit, modifier: Reuse) {
  }
}
