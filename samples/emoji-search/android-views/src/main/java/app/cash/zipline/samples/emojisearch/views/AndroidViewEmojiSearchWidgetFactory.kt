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
package app.cash.zipline.samples.emojisearch.views

import android.content.Context
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.treehouse.TreehouseApp
import example.schema.widget.EmojiSearchWidgetFactory

class AndroidViewEmojiSearchWidgetFactory<T : Any>(
  private val context: Context,
  private val treehouseApp: TreehouseApp<T>
) : EmojiSearchWidgetFactory<View> {
  override val RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context)
  override fun LazyColumn() = ViewLazyColumn(treehouseApp, RecyclerView(context))
  override fun TextInput() = ViewTextInput(EditText(context))
  override fun Text() = ViewText(TextView(context))
  override fun Image() = ViewImage(ImageView(context))
}
