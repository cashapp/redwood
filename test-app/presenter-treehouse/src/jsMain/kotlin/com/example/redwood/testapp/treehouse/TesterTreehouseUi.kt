/*
 * Copyright (C) 2024 Square, Inc.
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
package com.example.redwood.testapp.treehouse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.treehouse.TreehouseUi
import com.example.redwood.testapp.compose.Button
import com.example.redwood.testapp.compose.TextInput

class TesterTreehouseUi : TreehouseUi {
  @Composable
  override fun Show() {
    var content by remember { mutableStateOf(Content.InitialValue) }
    content.Show { newContent ->
      content = newContent
    }
  }

  enum class Content {
    InitialValue {
      @Composable
      override fun Show(changeContent: (Content) -> Unit) {
        TextInput(
          text = "what would you like to see?",
          customType = null,
          onChange = { nextStateName ->
            changeContent(Content.valueOf(nextStateName))
          },
        )
      }
    },

    TreehouseTesterTestHappyPathStep2 {
      @Composable
      override fun Show(changeContent: (Content) -> Unit) {
        Button(
          text = "This is TreehouseTesterTestHappyPathStep2",
          onClick = {
          },
        )
      }
    },

    ;

    @Composable
    abstract fun Show(changeContent: (Content) -> Unit)
  }
}
