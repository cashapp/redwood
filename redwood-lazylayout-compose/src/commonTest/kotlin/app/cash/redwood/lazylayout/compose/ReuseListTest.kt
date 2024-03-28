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
@file:Suppress(
  "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)
package app.cash.redwood.lazylayout.compose


import androidx.compose.runtime.mutableStateListOf
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.widget.ColumnValue
import app.cash.redwood.layout.widget.MutableColumn
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isSameInstanceAs
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.widget.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ReuseListTest {
  @Test
  fun happyPath() = runTest {
    val items = mutableStateListOf("a", "b", "c")
    viewRecyclingTest {
      // Set the initial content. New widgets are created.
      setContent {
        Column {
          ReuseList(
            items = items,
            key = { it },
          ) {
            Text(it)
          }
        }
      }

      assertThat(awaitSnapshot()).containsExactly(
        ColumnValue(
          children = listOf(
            TextValue(text = "a"),
            TextValue(text = "b"),
            TextValue(text = "c"),
          )
        )
      )

      val snapshot1Column = widgets.single() as MutableColumn
      val snapshot1ColumnTextA = snapshot1Column.children[0]

      items.removeAt(0)
      items.add("d")
      assertThat(awaitSnapshot()).containsExactly(
        ColumnValue(
          children = listOf(
            TextValue(text = "b"),
            TextValue(text = "c"),
            TextValue(text = "d"),
          )
        )
      )
      val snapshot2Column = widgets.single() as MutableColumn
      val snapshot2ColumnTextD = snapshot2Column.children[2]
      assertThat(snapshot2ColumnTextD).isSameInstanceAs(snapshot1ColumnTextA)
    }
  }
}
