/*
 * Copyright (C) 2023 Square, Inc.
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
package com.example.redwood.emojisearch.ios

import androidx.compose.runtime.BroadcastFrameClock
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.widget.UIViewChildren
import com.example.redwood.emojisearch.presenter.EmojiSearch
import com.example.redwood.emojisearch.treehouse.HostApi
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactories
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactory
import kotlinx.cinterop.convert
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import platform.UIKit.UIStackView
import platform.UIKit.UIView

@Suppress("unused") // Called from Swift.
class EmojiSearchViewControllerDelegate(
  root: UIStackView,
  widgetFactory: EmojiSearchWidgetFactory<UIView>,
  hostApi: HostApi,
) {
  private val clock = BroadcastFrameClock()
  private val scope = MainScope() + clock

  init {
    val children = UIViewChildren(
      parent = root,
      insert = { view, index -> root.insertArrangedSubview(view, index.convert()) },
    )
    val composition = RedwoodComposition(
      scope = scope,
      container = children,
      provider = EmojiSearchWidgetFactories(
        EmojiSearch = widgetFactory,
        RedwoodLayout = UIViewRedwoodLayoutWidgetFactory(),
      ),
    )
    composition.setContent {
      EmojiSearch(hostApi::httpCall)
    }
  }

  fun tickClock() {
    clock.sendFrame(0L) // Compose does not use frame time.
  }

  fun dispose() {
    scope.cancel()
  }
}

