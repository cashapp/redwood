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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.LayoutModifier
import coil.compose.AsyncImage
import example.schema.widget.Image

class ComposeUiImage : Image<@Composable () -> Unit> {
  private var url by mutableStateOf("")

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value = @Composable {
    AsyncImage(
      model = url,
      contentDescription = null,
      modifier = Modifier.size(48.dp)
    )
  }

  override fun url(url: String) {
    this.url = url
  }
}
