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
package app.cash.zipline.samples.emojisearch

import example.schema.widget.EmojiSearchWidgetFactory
import okio.ByteString
import okio.toByteString
import platform.Foundation.NSData

@Suppress("unused", "UNUSED_PARAMETER") // Used to export types to Objective-C / Swift.
fun exposedTypes(
  emojiSearchZipline: EmojiSearchZipline,
  emojiSearchEvent: EmojiSearchEvent,
  emojiSearchWidgetFactory: EmojiSearchWidgetFactory<*>,
  treehouseUIKitView: app.cash.redwood.treehouse.TreehouseUIKitView<*>,
) {
  throw AssertionError()
}

fun byteStringOf(data: NSData): ByteString = data.toByteString()
