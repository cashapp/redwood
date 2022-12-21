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

package app.cash.zipline.samples.emojisearch

import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.treehouse.TreehouseUIKitView
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.lazylayout.uiview.UIViewRedwoodTreehouseLazyLayoutWidgetFactory
import example.schema.widget.EmojiSearchDiffConsumingNodeFactory
import example.schema.widget.EmojiSearchWidgetFactories
import example.schema.widget.EmojiSearchWidgetFactory
import okio.ByteString
import okio.toByteString
import platform.Foundation.NSData

// Used to export types to Objective-C / Swift.
fun exposedTypes(
  emojiSearchLauncher: EmojiSearchLauncher,
  emojiSearchWidgetFactory: EmojiSearchWidgetFactory<*>,
  treehouseUIKitView: TreehouseUIKitView<*>,
  uiViewRedwoodLayoutWidgetFactory: UIViewRedwoodLayoutWidgetFactory,
  uiViewRedwoodTreehouseLazyLayoutWidgetFactory: UIViewRedwoodTreehouseLazyLayoutWidgetFactory<*>,
  widgetSystem: TreehouseView.WidgetSystem<*>,
  widgetFactories: EmojiSearchWidgetFactories<*>,
  diffConsumingNodeFactory: EmojiSearchDiffConsumingNodeFactory<*>,
) {
  throw AssertionError()
}

fun byteStringOf(data: NSData): ByteString = data.toByteString()

fun layoutModifier(): LayoutModifier = LayoutModifier
