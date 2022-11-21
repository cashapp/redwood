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

import androidx.compose.runtime.Composable
import app.cash.redwood.layout.composeui.ComposeRedwoodLayoutWidgetFactory
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import example.schema.widget.EmojiSearchWidgetFactory

class AndroidEmojiSearchWidgetFactory<W : Any>(
  private val treehouseApp: TreehouseApp<W>,
  private val widgetSystem: TreehouseView.WidgetSystem<W>,
) : EmojiSearchWidgetFactory<@Composable () -> Unit> {
  override val RedwoodLayout = ComposeRedwoodLayoutWidgetFactory()
  override fun LazyColumn() = ComposeUiLazyColumn(treehouseApp, widgetSystem)
  override fun TextInput() = ComposeUiTextInput(treehouseApp.dispatchers)
  override fun Text() = ComposeUiText()
  override fun Image() = ComposeUiImage()
}
