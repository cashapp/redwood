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
package app.cash.zipline.samples.emojisearch

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.cash.redwood.compose.AndroidUiDispatcher
import app.cash.redwood.compose.AndroidUiDispatcher.Companion.Main
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolDisplay
import coil.compose.AsyncImage
import example.schema.compose.DiffProducingEmojiSearchWidgetFactory
import example.schema.widget.DiffConsumingEmojiSearchWidgetFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@NoLiveLiterals
class EmojiSearchActivity : ComponentActivity() {
  private val scope = CoroutineScope(Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val composition = ProtocolRedwoodComposition(
      scope = scope,
      factory = DiffProducingEmojiSearchWidgetFactory(),
      widgetVersion = 1U,
      onDiff = { Log.d("RedwoodDiff", it.toString()) },
      onEvent = { Log.d("RedwoodEvent", it.toString()) },
    )

    val root = ComposeUiColumn()
    val composeView = ComposeView(this).apply {
      layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
      setContent(root.value)
    }
    setContentView(composeView)

    val factory = DiffConsumingEmojiSearchWidgetFactory(AndroidEmojiSearchWidgetFactory)
    val display = ProtocolDisplay(
      root = factory.wrap(root),
      factory = factory,
      eventSink = composition,
    )
    composition.start(display)

    val events = MutableSharedFlow<EmojiSearchEvent>(extraBufferCapacity = Int.MAX_VALUE)
    val models = MutableStateFlow(initialViewModel)
    EmojiSearchZipline().produceModelsIn(scope, events, models)

    composition.setContent {
      val modelsState = models.collectAsState()
      EmojiSearchTheme {
        EmojiSearch(modelsState.value) { event ->
          events.tryEmit(event)
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}

@Composable
fun EmojiSearch(
  model: EmojiSearchViewModel,
  events: (EmojiSearchEvent) -> Unit
) {
  Surface(
    color = MaterialTheme.colors.background,
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
    ) {
      SearchField(model.searchTerm, events)
      SearchResults(model.images)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  val events = fun(_: EmojiSearchEvent) = Unit
  EmojiSearchTheme {
    EmojiSearch(sampleViewModel, events)
  }
}
