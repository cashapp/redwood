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
@file:Suppress("unused", "UNUSED_PARAMETER")

package com.example.redwood.emojisearch.ios

import app.cash.redwood.Modifier
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.uiview.UIViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.Content
import app.cash.redwood.treehouse.TreehouseUIKitView
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.bindWhenReady
import com.example.redwood.emojisearch.widget.EmojiSearchProtocolNodeFactory
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactories
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactory
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.Closeable
import platform.Foundation.NSData

// Used to export types to Objective-C / Swift.
fun exposedTypes(
  emojiSearchLauncher: EmojiSearchLauncher,
  emojiSearchWidgetFactory: EmojiSearchWidgetFactory<*>,
  protocolNodeFactory: EmojiSearchProtocolNodeFactory<*>,
  treehouseUIKitView: TreehouseUIKitView,
  uiViewRedwoodLayoutWidgetFactory: UIViewRedwoodLayoutWidgetFactory,
  uiViewRedwoodLazyLayoutWidgetFactory: UIViewRedwoodLazyLayoutWidgetFactory,
  widgetSystem: WidgetSystem,
  widgetFactories: EmojiSearchWidgetFactories<*>,
) {
  throw AssertionError()
}

fun byteStringOf(data: NSData): ByteString = data.toByteString()

fun modifier(): Modifier = Modifier

fun <A : AppService> bindWhenReady(
  content: Content,
  view: TreehouseView,
): Closeable = content.bindWhenReady(view)
