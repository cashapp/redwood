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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.ui.basic.widget.Image
import coil3.ImageLoader
import coil3.compose.AsyncImage

internal class ComposeUiImage(
  private val imageLoader: ImageLoader,
) : Image<@Composable () -> Unit> {
  private var url by mutableStateOf("")
  private var onClick by mutableStateOf({})

  override var modifier: RedwoodModifier = RedwoodModifier

  override val value = @Composable {
    AsyncImage(
      model = url,
      imageLoader = imageLoader,
      contentDescription = null,
      modifier = Modifier
        .size(48.dp)
        .clickable(onClick = onClick),
    )
  }

  override fun url(url: String) {
    this.url = url
  }

  override fun onClick(onClick: (() -> Unit)?) {
    this.onClick = onClick ?: {}
  }
}
