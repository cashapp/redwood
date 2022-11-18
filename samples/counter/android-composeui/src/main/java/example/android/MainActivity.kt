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
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import app.cash.redwood.compose.AndroidUiDispatcher
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.widget.compose.ComposeWidgetChildren
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

    val widgets = initComposition {
      Counter()
    }

    setContent {
      CounterTheme {
        widgets.render()
      }
    }
  }

  private fun initComposition(content: @Composable () -> Unit): ComposeWidgetChildren {
    val composeChildren = ComposeWidgetChildren()

    val composition = ProtocolRedwoodComposition(
      scope = scope,
      factory = DiffProducingSunspotWidgetFactory(),
      widgetVersion = 1U,
      onDiff = { Log.d("RedwoodDiff", it.toString()) },
      onEvent = { Log.d("RedwoodEvent", it.toString()) },
    )

    val factory = DiffConsumingSunspotWidgetFactory(AndroidSunspotWidgetFactory())
    val bridge = ProtocolBridge(
      container = composeChildren,
      factory = factory,
      eventSink = composition,
    )

    composition.start(bridge)
    composition.setContent(content)

    return composeChildren
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
