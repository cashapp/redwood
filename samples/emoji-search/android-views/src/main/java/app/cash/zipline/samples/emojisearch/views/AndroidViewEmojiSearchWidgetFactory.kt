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
import android.widget.ImageView
import android.widget.TextView
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.lazylayout.view.ViewRedwoodTreehouseLazyLayoutWidgetFactory
import example.schema.widget.EmojiSearchWidgetFactory

class AndroidViewEmojiSearchWidgetFactory<A : AppService>(
  private val context: Context,
  private val treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
) : EmojiSearchWidgetFactory<View> {
  override val RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context)
  override val RedwoodTreehouseLazyLayout = ViewRedwoodTreehouseLazyLayoutWidgetFactory(context, treehouseApp, widgetSystem)
  override fun TextInput() = ViewTextInput(context, treehouseApp.dispatchers)
  override fun Text() = ViewText(TextView(context))
  override fun Image() = ViewImage(ImageView(context))
}
