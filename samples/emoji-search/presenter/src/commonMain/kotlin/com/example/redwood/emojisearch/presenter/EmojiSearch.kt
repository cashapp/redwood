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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.compose.LocalUiConfiguration
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.lazylayout.compose.ExperimentalRedwoodLazyLayoutApi
import app.cash.redwood.lazylayout.compose.LazyColumn
import app.cash.redwood.lazylayout.compose.LazyListState
import app.cash.redwood.lazylayout.compose.items
import app.cash.redwood.lazylayout.compose.rememberLazyListState
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.emojisearch.compose.Image
import com.example.redwood.emojisearch.compose.Text
import com.example.redwood.emojisearch.compose.TextInput
import example.values.TextFieldState
import kotlinx.serialization.json.Json

private data class EmojiImage(
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

enum class Variant {
  LAZY_COLUMN, SCROLLABLE_FLEXBOX, BUGGY_COLUMNS
}

@Composable
fun EmojiSearch(
  httpClient: HttpClient,
  navigator: Navigator,
  variant: Variant = Variant.LAZY_COLUMN,
) {
  when (variant) {
    Variant.LAZY_COLUMN -> LazyColumn(httpClient, navigator)
    Variant.SCROLLABLE_FLEXBOX -> NestedFlexBoxContainers(httpClient, navigator)
    Variant.BUGGY_COLUMNS -> BuggyNestedColumns(httpClient, navigator)
  }
}

@OptIn(ExperimentalRedwoodLazyLayoutApi::class)
@Composable
private fun LazyColumn(
  httpClient: HttpClient,
  navigator: Navigator,
) {
  val allEmojis = remember { mutableStateListOf<EmojiImage>() }

  // Simple counter that allows us to trigger refreshes by simple incrementing the value
  var refreshSignal by remember { mutableStateOf(0) }
  var refreshing by remember { mutableStateOf(false) }

  val searchTermSaver = object : Saver<TextFieldState, String> {
    override fun restore(value: String) = TextFieldState(value)
    override fun SaverScope.save(value: TextFieldState) = value.text
  }

  var searchTerm by rememberSaveable(stateSaver = searchTermSaver) {
    mutableStateOf(TextFieldState(""))
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
      allEmojis.addAll(labelToUrl.map { (key, value) -> EmojiImage(key, value) })
    } finally {
      refreshing = false
    }
  }

  val filteredEmojis by derivedStateOf {
    val searchTerms = searchTerm.text.split(" ")
    allEmojis.filter { image ->
      searchTerms.all { image.label.contains(it, ignoreCase = true) }
    }
  }

  val lazyListState = rememberLazyListState()

  LaunchedEffect(searchTerm) {
    lazyListState.scrollToItem(0)
  }

  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    margin = LocalUiConfiguration.current.safeAreaInsets,
  ) {
    TextInput(
      state = TextFieldState(searchTerm.text),
      hint = "Search",
      onChange = { searchTerm = it },
      modifier = Modifier.shrink(0.0),
    )
    LazyColumn(
      refreshing = refreshing,
      onRefresh = { refreshSignal++ },
      state = lazyListState,
      width = Constraint.Fill,
      modifier = Modifier.grow(1.0),
      placeholder = {
        Item(
          emojiImage = loadingEmojiImage,
          onClick = {},
        )
      },
    ) {
      items(filteredEmojis) { image ->
        Item(
          emojiImage = image,
          onClick = {
            navigator.openUrl(image.url)
          },
        )
      }
    }
  }
}

@Composable
private fun NestedFlexBoxContainers(httpClient: HttpClient, navigator: Navigator) {
  val allEmojis = remember { mutableStateListOf<EmojiImage>() }

  val searchTermSaver = object : Saver<TextFieldState, String> {
    override fun restore(value: String) = TextFieldState(value)
    override fun SaverScope.save(value: TextFieldState) = value.text
  }

  var searchTerm by rememberSaveable(stateSaver = searchTermSaver) {
    error("boom")
    mutableStateOf(TextFieldState(""))
  }
  var searchTerm1 by rememberSaveable(stateSaver = searchTermSaver) {
    error("boom")
    mutableStateOf(TextFieldState("hhh"))
  }

  LaunchedEffect(Unit) {
    try {
      val emojisJson = httpClient.call(
        url = "https://api.github.com/emojis",
        headers = mapOf("Accept" to "application/vnd.github.v3+json"),
      )
      val labelToUrl = Json.decodeFromString<Map<String, String>>(emojisJson)

      allEmojis.clear()
      allEmojis.addAll(labelToUrl.map { (key, value) -> EmojiImage(key, value) })
    } catch (e: Exception) {
      println("Failed to load https://api.github.com/emojis $e")
    }
  }

  val filteredEmojis by derivedStateOf {
    val searchTerms = searchTerm.text.split(" ")
    allEmojis.filter { image ->
      searchTerms.all { image.label.contains(it, ignoreCase = true) }
    }
  }

  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    overflow = Overflow.Clip,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    margin = LocalUiConfiguration.current.safeAreaInsets,
    verticalAlignment = MainAxisAlignment.Start,
  ) {
    TextInput(
      state = TextFieldState(searchTerm.text),
      hint = "Search",
      onChange = { searchTerm = it },
      modifier = Modifier.shrink(0.0),
    )

    if (filteredEmojis.count() > 0) {
      Text(
        text = "Scroll Column - Nested Scroll Row + B Emojis",
        modifier = Modifier.margin(Margin(12.dp)),
      )
      Column(
        width = Constraint.Fill,
        height = Constraint.Fill,
        overflow = Overflow.Scroll,
        horizontalAlignment = CrossAxisAlignment.Stretch,
        modifier = Modifier.shrink(1.0),
      ) {
        Text(
          text = "Scroll Row - A Emojis",
          modifier = Modifier.margin(Margin(12.dp)),
        )
        Row(
          width = Constraint.Wrap,
          height = Constraint.Wrap,
          overflow = Overflow.Scroll,
          verticalAlignment = CrossAxisAlignment.Center,
        ) {
          val filtered = filteredEmojis.filter { it.label.startsWith("a") }.take(30)
          filtered.forEach { image ->
            Item(image)
          }
        }
        filteredEmojis.filter { it.label.startsWith("b") }.take(30).forEach { image ->
          Item(image)
        }
      }
      Text(
        text = "Scroll FlexRow - People Emojis",
        modifier = Modifier.margin(Margin(12.dp)),
      )
      Row(
        width = Constraint.Wrap,
        height = Constraint.Wrap,
        overflow = Overflow.Scroll,
        verticalAlignment = CrossAxisAlignment.Center,
      ) {
        val filtered =
          filteredEmojis.filter { it.label.contains(Regex("man|woman|person")) }.take(30)
        filtered.forEach { image ->
          Item(image)
        }
      }
    } else {
      Text(
        text = "Empty",
        modifier = Modifier.margin(Margin(12.dp)),
      )
    }
  }
}

@Composable
private fun BuggyNestedColumns(httpClient: HttpClient, navigator: Navigator) {
  // Full-screen parent column
  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Center,
  ) {
    // Header
    Column(
      width = Constraint.Fill,
      horizontalAlignment = CrossAxisAlignment.Center,
    ) {
      Item(
        emojiImage = loadingEmojiImage,
        onClick = {},
      )
      Item(
        emojiImage = loadingEmojiImage,
        onClick = {},
      )
      Item(
        emojiImage = loadingEmojiImage,
        onClick = {},
      )
      Item(
        emojiImage = loadingEmojiImage,
        onClick = {},
      )
    }
    // Body (sibling of Header)
    // Given a height with a Fill constraint, and a Center alignment,
    // the Item should be vertically cenetered in the remaining space, but isn't.
    // Instead, the content is effectively pushed down by the height of the Header.
    Column(
      horizontalAlignment = CrossAxisAlignment.Center,
      width = Constraint.Fill,
      height = Constraint.Fill,
      verticalAlignment = MainAxisAlignment.Center,
    ) {
      Item(
        emojiImage = loadingEmojiImage,
        onClick = {},
      )
    }
  }
}

@Composable
private fun Item(
  emojiImage: EmojiImage,
  onClick: () -> Unit = {},
) {
  Row(
    width = Constraint.Fill,
    height = Constraint.Wrap,
    verticalAlignment = CrossAxisAlignment.Center,
    horizontalAlignment = MainAxisAlignment.Start,
  ) {
    Image(
      url = emojiImage.url,
      modifier = Modifier
        .margin(Margin(8.dp)),
      onClick = onClick,
    )
    Text(text = emojiImage.label)
  }
}

private val loadingEmojiImage = EmojiImage(
  label = "loading…",
  url = "https://github.githubassets.com/images/icons/emoji/unicode/231a.png?v8",
)
