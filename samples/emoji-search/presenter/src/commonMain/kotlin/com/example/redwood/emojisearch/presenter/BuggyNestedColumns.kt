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
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.compose.Column

@Composable
fun BuggyNestedColumns() {
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
