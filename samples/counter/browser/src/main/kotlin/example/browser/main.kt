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
package example.browser

import app.cash.redwood.compose.WindowAnimationFrameClock
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolDisplay
import example.browser.sunspot.HtmlSunspotBox
import example.browser.sunspot.HtmlSunspotNodeFactory
import example.shared.Counter
import example.sunspot.compose.DiffProducingSunspotWidgetFactory
import example.sunspot.widget.DiffConsumingSunspotWidgetFactory
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

fun main() {
  val composition = ProtocolRedwoodComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    factory = DiffProducingSunspotWidgetFactory(),
    widgetVersion = 1U,
    onDiff = { console.log("RedwoodDiff", it.toString()) },
    onEvent = { console.log("RedwoodEvent", it.toString()) },
  )

  val content = document.getElementById("content")!! as HTMLElement
  val factory = DiffConsumingSunspotWidgetFactory(HtmlSunspotNodeFactory(document))
  val display = ProtocolDisplay(
    root = factory.wrap(HtmlSunspotBox(content)),
    factory = factory,
    eventSink = composition,
  )

  composition.start(display)

  composition.setContent {
    Counter()
  }
}
