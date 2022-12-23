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
package com.example.redwood.emojisearch.android.composeui

import androidx.compose.runtime.Composable
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactory
import com.example.redwood.emojisearch.widget.Image
import com.example.redwood.emojisearch.widget.Text
import com.example.redwood.emojisearch.widget.TextInput

class AndroidEmojiSearchWidgetFactory<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
) : EmojiSearchWidgetFactory<@Composable () -> Unit> {
  override fun TextInput(): TextInput<@Composable () -> Unit> = ComposeUiTextInput(treehouseApp.dispatchers)
  override fun Text(): Text<@Composable () -> Unit> = ComposeUiText()
  override fun Image(): Image<@Composable () -> Unit> = ComposeUiImage()
}
