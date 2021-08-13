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
package example.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import example.schema.compose.Item
import example.schema.compose.ScrollableColumn
import example.schema.compose.Toolbar

@Composable
fun TodoPresenter() {
  Toolbar("Todo!")

  val items = remember { mutableStateListOf("Milk", "Bread", "Cookies", "Beer") }
  ScrollableColumn {
    items.forEachIndexed { index, item ->
      Item(item, onComplete = { items.removeAt(index) })
    }
  }
}
