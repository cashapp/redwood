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
import com.example.redwood.emojisearch.compose.Text
import com.example.redwood.emojisearch.compose.TextInput
import example.values.TextFieldState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This test demonstrates typical use of [RedwoodTester].
 */
class EmojiSearchTest {
  @Test
  fun recomposed() {
    val tester = EmojiSearchTester()
    tester.setContent {
      BasicEmojiSearch()
    }

    val snapshot0 = tester.snapshot()
    assertEquals(
      listOf(
        TextInputValue(
          state = TextFieldState(text = ""),
          hint = "Search",
        ),
      ),
      snapshot0,
    )

    (snapshot0.single() as TextInputValue).onChange!!.invoke(TextFieldState(text = "tree"))

    val snapshot1 = tester.snapshot()
    assertEquals(
      listOf(
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
      snapshot1,
    )
  }

  /** A simplified sample to demonstrate the test harness. */
  @Composable
  private fun BasicEmojiSearch() {
    var search by remember { mutableStateOf(TextFieldState()) }
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
