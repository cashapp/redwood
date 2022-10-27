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
package example.ios

import androidx.compose.runtime.BroadcastFrameClock
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolDisplay
import app.cash.redwood.widget.UIViewChildren
import example.ios.sunspot.IosSunspotNodeFactory
import example.shared.Counter
import example.sunspot.compose.DiffProducingSunspotWidgetFactory
import example.sunspot.widget.DiffConsumingSunspotWidgetFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import platform.Foundation.NSLog
import platform.UIKit.UIStackView

class CounterViewControllerDelegate(
  root: UIStackView,
) {
  private val clock = BroadcastFrameClock()
  private val scope = MainScope() + clock

  init {
    val composition = ProtocolRedwoodComposition(
      scope = scope,
      factory = DiffProducingSunspotWidgetFactory(),
      widgetVersion = 1U,
      onDiff = { NSLog("RedwoodDiff: $it") },
      onEvent = { NSLog("RedwoodEvent: $it") },
    )

    val children = UIViewChildren(
      parent = root,
      insert = { view, index -> root.insertArrangedSubview(view, index.toULong()) },
    )
    val factory = DiffConsumingSunspotWidgetFactory(IosSunspotNodeFactory)
    val display = ProtocolDisplay(
      container = children,
      factory = factory,
      eventSink = composition,
    )

    composition.start(display)

    composition.setContent {
      Counter()
    }
  }

  fun tickClock() {
    clock.sendFrame(0L) // Compose does not use frame time.
  }

  fun dispose() {
    scope.cancel()
  }
}
