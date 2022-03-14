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
package com.example.redwood.counter.ios

import app.cash.redwood.compose.DisplayLinkClock
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.widget.RedwoodUIView
import com.example.redwood.counter.presenter.Counter
import com.example.redwood.counter.widget.SchemaWidgetFactories
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import platform.UIKit.UIStackView

@Suppress("unused") // Called from Swift.
class CounterViewControllerDelegate(
  root: UIStackView,
) {
  private val scope = MainScope() + DisplayLinkClock

  init {
    val composition = RedwoodComposition(
      scope = scope,
      view = RedwoodUIView(root),
      provider = SchemaWidgetFactories(
        Schema = IosWidgetFactory,
        RedwoodLayout = UIViewRedwoodLayoutWidgetFactory(),
      ),
    )
    composition.setContent {
      Counter()
    }
  }

  fun dispose() {
    scope.cancel()
  }
}
