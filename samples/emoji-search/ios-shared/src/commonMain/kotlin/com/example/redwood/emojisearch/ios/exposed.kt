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
import app.cash.redwood.basic.protocol.host.RedwoodBasicProtocolFactory
import app.cash.redwood.basic.uiview.UIViewRedwoodBasicWidgetSystem
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.Content
import app.cash.redwood.treehouse.TreehouseUIView
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.bindWhenReady
import okio.Closeable

// Used to export types to Objective-C / Swift.
fun exposedTypes(
  emojiSearchLauncher: EmojiSearchLauncher,
  protocolFactory: RedwoodBasicProtocolFactory<*>,
  treehouseUIView: TreehouseUIView,
  treehouseWidgetSystem: WidgetSystem<*>,
) {
  throw AssertionError()
}

fun basicWidgetSystem() = UIViewRedwoodBasicWidgetSystem()

fun modifier(): Modifier = Modifier

fun <A : AppService> bindWhenReady(
  content: Content,
  view: TreehouseView<*>,
): Closeable = content.bindWhenReady(view)
