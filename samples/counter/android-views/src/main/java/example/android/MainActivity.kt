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
package example.android

import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import app.cash.redwood.compose.AndroidUiDispatcher
import app.cash.redwood.protocol.compose.ProtocolBridge as ComposeProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.widget.ViewGroupChildren
import example.android.sunspot.AndroidSunspotWidgetFactory
import example.shared.Counter
import example.sunspot.compose.DiffProducingSunspotWidgetFactory
import example.sunspot.widget.DiffConsumingSunspotWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {
  private val scope = CoroutineScope(AndroidUiDispatcher.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val composeBridge = ComposeProtocolBridge()
    val composition = ProtocolRedwoodComposition(
      scope = scope,
      factory = DiffProducingSunspotWidgetFactory(composeBridge),
      widgetVersion = 1U,
    )

    val root = FrameLayout(this).apply {
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    setContentView(root)

    val factory = DiffConsumingSunspotWidgetFactory(AndroidSunspotWidgetFactory(this))
    val widgetBridge = ProtocolBridge(
      container = ViewGroupChildren(root),
      factory = factory,
      eventSink = composeBridge,
    )

    composition.start(widgetBridge)

    composition.setContent {
      Counter()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
