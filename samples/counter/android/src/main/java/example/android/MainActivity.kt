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
package example.android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.compose.AndroidUiDispatcher
import app.cash.treehouse.protocol.compose.ProtocolTreehouseComposition
import app.cash.treehouse.protocol.widget.ProtocolDisplay
import example.android.sunspot.AndroidSunspotBox
import example.android.sunspot.AndroidSunspotWidgetFactory
import example.shared.Counter
import example.sunspot.compose.DiffProducingSunspotWidgetFactory
import example.sunspot.widget.DiffConsumingSunspotWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class MainActivity : Activity() {
  private val scope = CoroutineScope(AndroidUiDispatcher.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val composition = ProtocolTreehouseComposition(
      scope = scope,
      factory = DiffProducingSunspotWidgetFactory(),
      onDiff = { Log.d("TreehouseDiff", it.toString()) },
      onEvent = { Log.d("TreehouseEvent", it.toString()) },
    )

    val root = LinearLayout(this).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    setContentView(root)

    val factory = DiffConsumingSunspotWidgetFactory(AndroidSunspotWidgetFactory(this))
    val display = ProtocolDisplay(
      root = factory.wrap(AndroidSunspotBox(root)),
      factory = factory,
      eventSink = composition,
    )

    composition.start(display)

    composition.setContent {
      Counter()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
