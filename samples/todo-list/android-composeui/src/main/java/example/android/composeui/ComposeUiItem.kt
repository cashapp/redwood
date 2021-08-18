/*
 * Copyright (C) 2021 Square, Inc.
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

package example.android.composeui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import example.schema.widget.Item

class ComposeUiItem : Item<@Composable () -> Unit> {
  private var content by mutableStateOf("")
  private var onComplete: (() -> Unit)? = null

  override val value = @Composable {
    Row(modifier = Modifier.padding(8.dp)) {
      Checkbox(modifier = Modifier.padding(end = 8.dp), checked = false, onCheckedChange = {
        onComplete?.invoke()
      })
      Text(content, style = MaterialTheme.typography.body1)
    }
  }

  override fun content(content: String) {
    this.content = content
  }

  override fun onComplete(onComplete: (() -> Unit)?) {
    this.onComplete = onComplete
  }
}
