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
package app.cash.redwood.ui.basic.composeui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.layout.composeui.ComposeUiRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.composeui.ComposeUiRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.ui.basic.api.TextFieldState
import app.cash.redwood.ui.basic.widget.RedwoodUiBasicWidgetSystem
import coil3.ImageLoader
import coil3.compose.AsyncImage

@Suppress("FunctionName") // Acting like a type.
public fun ComposeUiRedwoodUiBasicWidgetSystem(
  imageLoader: ImageLoader,
): RedwoodUiBasicWidgetSystem<@Composable (Modifier) -> Unit> {
  return RedwoodUiBasicWidgetSystem(
    RedwoodUiBasic = ComposeUiRedwoodUiBasicWidgetFactory(imageLoader),
    RedwoodLayout = ComposeUiRedwoodLayoutWidgetFactory(),
    RedwoodLazyLayout = ComposeUiRedwoodLazyLayoutWidgetFactory(),
  )
}

private class ComposeUiRedwoodUiBasicWidgetFactory(
  private val imageLoader: ImageLoader,
) : AbstractComposeUiRedwoodUiBasicWidgetFactory() {
  @Composable
  override fun TextInputBinding(
    state: TextFieldState,
    hint: String,
    onChange: ((TextFieldState) -> Unit)?,
    modifier: Modifier,
  ) {
    TextInput(state, hint, onChange, modifier)
  }

  @Composable
  override fun TextBinding(text: String, modifier: Modifier) {
    Text(
      modifier = modifier,
      text = text,
      color = MaterialTheme.colors.onBackground,
    )
  }

  @Composable
  override fun ImageBinding(
    url: String,
    onClick: (() -> Unit)?,
    modifier: Modifier,
  ) {
    AsyncImage(
      model = url,
      imageLoader = imageLoader,
      contentDescription = null,
      modifier = modifier
        .size(48.dp)
        .run {
          if (onClick != null) {
            clickable(onClick = onClick)
          } else {
            this
          }
        },
    )
  }

  @Composable
  override fun ButtonBinding(
    text: String?,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier,
  ) {
    Button(
      onClick = onClick ?: {},
      enabled = enabled,
      modifier = modifier.fillMaxWidth(),
    ) {
      Text(text ?: "")
    }
  }
}
