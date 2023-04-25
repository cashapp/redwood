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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.compose.testing.flatten
import app.cash.redwood.compose.testing.viewTree
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.widget.ColumnValue
import com.example.redwood.emojisearch.compose.Text
import com.example.redwood.emojisearch.compose.TextInput
import com.example.redwood.emojisearch.widget.EmojiSearchTester
import com.example.redwood.emojisearch.widget.TextInputValue
import com.example.redwood.emojisearch.widget.TextValue
import example.values.TextFieldState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * This test demonstrates typical use of [RedwoodTester].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EmojiSearchTest {
  @Test
  fun recomposed() = runTest {
    EmojiSearchTester(this).test {
      setContent {
        BasicEmojiSearch()
      }

      val snapshot0 = awaitSnapshot()
      assertEquals(
        listOf(
          ColumnValue(
            children = listOf(
              TextInputValue(
                state = TextFieldState(text = ""),
                hint = "Search",
              ),
            ),
          ),
        ),
        snapshot0,
      )

      snapshot0.flatten().filterIsInstance<TextInputValue>().first()
        .onChange!!.invoke(TextFieldState(text = "tree"))

      val snapshot1 = awaitSnapshot()
      assertEquals(
        listOf(
          ColumnValue(
            children = listOf(
              TextInputValue(
                state = TextFieldState(text = "tree"),
                hint = "Search",
              ),
              TextValue(
                text = "🌲",
              ),
              TextValue(
                text = "🌳",
              ),
            ),
          ),
        ),
        snapshot1,
      )
    }
  }

  /** A simplified sample to demonstrate the test harness. */
  @Composable
  private fun BasicEmojiSearch() {
    var search by remember { mutableStateOf(TextFieldState()) }
    Column {
      TextInput(
        state = search,
        hint = "Search",
        onChange = {
          search = it
        },
      )

      if ("tree" in search.text) {
        Text(text = "🌲")
        Text(text = "🌳")
      }
    }
  }
}
