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
package example.shared

import androidx.compose.runtime.BroadcastFrameClock
import app.cash.treehouse.protocol.widget.ProtocolDisplay
import example.sunspot.SunspotBox
import example.sunspot.SunspotButton
import example.sunspot.SunspotText
import example.sunspot.compose.ProtocolComposeWidgetFactory
import example.sunspot.compose.SunspotComposition
import example.sunspot.test.SchemaSunspotBox
import example.sunspot.test.SchemaSunspotWidgetFactory
import example.sunspot.widget.ProtocolDisplayWidgetFactory
import example.sunspot.widget.ProtocolSunspotBox
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.test.Test

class CounterTest {
  @Test fun basic() = runTest {
    val root = SchemaSunspotBox()

    val clock = BroadcastFrameClock()
    val composition = SunspotComposition(
      scope = this + clock,
      factory = ProtocolComposeWidgetFactory(),
      onDiff = { println(it) },
      onEvent = { println(it) },
    )

    val display = ProtocolDisplay(
      root = ProtocolSunspotBox(root),
      factory = ProtocolDisplayWidgetFactory(SchemaSunspotWidgetFactory),
      eventSink = composition,
    )

    composition.start(display)

    composition.setContent {
      Counter()
    }

    clock.advanceFrame()
    root.assertSchema(
      SunspotBox(
        listOf(
          SunspotButton("-1"),
          SunspotText("0"),
          SunspotButton("+1")
        )
      )
    )

    composition.cancel()
  }
}

private suspend fun BroadcastFrameClock.advanceFrame() {
  coroutineScope {
    launch(start = UNDISPATCHED) { withFrameNanos { } }
    sendFrame(0)
  }
}
