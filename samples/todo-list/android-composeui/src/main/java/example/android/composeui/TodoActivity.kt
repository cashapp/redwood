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

package example.android.composeui

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import app.cash.treehouse.compose.AndroidUiDispatcher.Companion.Main
import app.cash.treehouse.protocol.widget.ProtocolDisplay
import example.presenters.TodoPresenter
import example.schema.compose.ProtocolComposeWidgetFactory
import example.schema.compose.TodoComposition
import example.schema.widget.ProtocolColumn
import example.schema.widget.ProtocolDisplayWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class TodoActivity : ComponentActivity() {
  private val scope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val composition = TodoComposition(
      scope = scope,
      factory = ProtocolComposeWidgetFactory(),
      onDiff = { Log.d("TreehouseDiff", it.toString()) },
      onEvent = { Log.d("TreehouseEvent", it.toString()) },
    )

    val root = ComposeUiColumn()
    val composeView = ComposeView(this).apply {
      layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
      setContent(root.value)
    }
    setContentView(composeView)

    val display = ProtocolDisplay(
      root = ProtocolColumn(root),
      factory = ProtocolDisplayWidgetFactory(ComposeUiWidgetFactory),
      eventSink = composition,
    )

    composition.start(display)

    composition.setContent {
      TodoPresenter()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}
