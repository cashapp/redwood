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
package com.example.redwood.emojisearch.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.compose.LocalUiConfiguration
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.layout.compose.Spacer
import app.cash.redwood.lazylayout.compose.ExperimentalRedwoodLazyLayoutApi
import app.cash.redwood.lazylayout.compose.LazyColumn
import app.cash.redwood.lazylayout.compose.items
import app.cash.redwood.lazylayout.compose.rememberLazyListState
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.emojisearch.compose.Image
import com.example.redwood.emojisearch.compose.Text
import com.example.redwood.emojisearch.compose.TextInput
import com.example.redwood.emojisearch.compose.reuse
import example.values.TextFieldState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class EmojiImage(
  val label: String,
  val url: String,
)

// TODO Switch to https://github.com/cashapp/zipline/issues/490 once available.
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
fun interface HttpClient {
  suspend fun call(url: String, headers: Map<String, String>): String
}

interface Navigator {
  /** Open a URL in the app that owns it. For example, a browser. */
  fun openUrl(url: String)
}

@Composable
@OptIn(ExperimentalRedwoodLazyLayoutApi::class)
fun EmojiSearch(
  httpClient: HttpClient,
  navigator: Navigator,
  modifier: Modifier = Modifier,
  viewInsets: Margin = LocalUiConfiguration.current.safeAreaInsets,
) {
  val scope = rememberCoroutineScope()
  val allEmojis = remember { mutableStateListOf<EmojiImage>() }

  // Simple counter that allows us to trigger refreshes by simple incrementing the value
  var refreshSignal by remember { mutableIntStateOf(0) }
  var refreshing by remember { mutableStateOf(false) }

  val searchTermSaver = object : Saver<TextFieldState, String> {
    override fun restore(value: String) = TextFieldState(value)
    override fun SaverScope.save(value: TextFieldState) = value.text
  }

  var searchTerm by rememberSaveable(stateSaver = searchTermSaver) { mutableStateOf(TextFieldState("")) }

  val lazyListState = rememberLazyListState()

  LaunchedEffect(searchTerm) {
    lazyListState.programmaticScroll(0, animated = true)
  }

  LaunchedEffect(refreshSignal) {
    try {
      refreshing = true
      val emojisJson = httpClient.call(
        url = "https://api.github.com/emojis",
        headers = mapOf("Accept" to "application/vnd.github.v3+json"),
      )
      val labelToUrl = Json.decodeFromString<Map<String, String>>(emojisJson)

      allEmojis.clear()
      var index = 0
      allEmojis.addAll(labelToUrl.map { (key, value) -> EmojiImage("${index++}. $key", value) })
    } finally {
      refreshing = false
    }
  }

  val filteredEmojis by remember {
    derivedStateOf {
      val searchTerms = searchTerm.text.split(" ")
      allEmojis.filter { image ->
        searchTerms.all { image.label.contains(it, ignoreCase = true) }
      }
    }
  }

  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    margin = Margin(start = viewInsets.start, end = viewInsets.end, top = viewInsets.top),
    modifier = modifier,
  ) {
    TextInput(
      state = TextFieldState(searchTerm.text),
      hint = "Search",
      onChange = { textFieldState ->
        // Make it easy to trigger a crash to manually test exception handling!
        when (textFieldState.text) {
          "crash" -> throw RuntimeException("boom!")

          "async" -> {
            scope.launch {
              throw RuntimeException("boom!")
            }
          }
        }

        searchTerm = textFieldState
      },
    )
    LazyColumn(
      refreshing = refreshing,
      onRefresh = { refreshSignal++ },
      state = lazyListState,
      width = Constraint.Fill,
      modifier = Modifier.flex(1.0),
      placeholder = {
        Item(
          emojiImage = loadingEmojiImage,
          onClick = {},
        )
      },
    ) {
      items(filteredEmojis) { image ->
        Item(
          modifier = Modifier.reuse(),
          emojiImage = image,
          onClick = {
            navigator.openUrl(image.url)
          },
        )
      }
      item {
        Spacer(height = viewInsets.bottom)
      }
    }
  }
}

@Composable
fun Item(
  emojiImage: EmojiImage,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Row(
    modifier = modifier,
    width = Constraint.Fill,
    height = Constraint.Wrap,
    verticalAlignment = CrossAxisAlignment.Center,
    horizontalAlignment = MainAxisAlignment.Start,
  ) {
    Image(
      url = emojiImage.url,
      modifier = Modifier
        .margin(Margin(8.dp))
        .size(24.dp, 24.dp),
      onClick = onClick,
    )
    Text(text = emojiImage.label)
  }
}

val loadingEmojiImage = EmojiImage(
  label = "loading…",
  url = "https://github.githubassets.com/images/icons/emoji/unicode/231a.png?v8",
)
