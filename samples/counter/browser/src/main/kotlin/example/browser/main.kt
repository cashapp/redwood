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

import app.cash.treehouse.compose.WindowAnimationFrameClock
import app.cash.treehouse.protocol.widget.ProtocolDisplay
import example.browser.sunspot.HtmlSunspotBox
import example.browser.sunspot.HtmlSunspotNodeFactory
import example.shared.Counter
import example.sunspot.compose.ProtocolComposeWidgetFactory
import example.sunspot.compose.SunspotComposition
import example.sunspot.widget.ProtocolDisplayWidgetFactory
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

fun main() {
  val composition = SunspotComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    factory = ProtocolComposeWidgetFactory(),
    onDiff = { console.log("TreehouseDiff", it.toString()) },
    onEvent = { console.log("TreehouseEvent", it.toString()) },
  )

  val content = document.getElementById("content")!! as HTMLElement
  val factory = ProtocolDisplayWidgetFactory(HtmlSunspotNodeFactory(document))
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
