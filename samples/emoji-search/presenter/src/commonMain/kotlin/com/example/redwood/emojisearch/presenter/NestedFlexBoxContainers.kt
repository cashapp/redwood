/*
 * Copyright (C) 2023 Square, Inc.
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
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.emojisearch.compose.Text
import com.example.redwood.emojisearch.compose.TextInput
import example.values.TextFieldState
import kotlinx.serialization.json.Json

@Composable
fun NestedFlexBoxContainers(httpClient: HttpClient) {
  val allEmojis = remember { mutableStateListOf<EmojiImage>() }

  val searchTermSaver = object : Saver<TextFieldState, String> {
    override fun restore(value: String) = TextFieldState(value)
    override fun SaverScope.save(value: TextFieldState) = value.text
  }

  var searchTerm by rememberSaveable(stateSaver = searchTermSaver) { mutableStateOf(TextFieldState("")) }

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
