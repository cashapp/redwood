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
package app.cash.redwood.lazylayout.compose

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.LazyListValue
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import assertk.assertThat
import assertk.assertions.containsExactly
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.widget.TestSchemaTester
import com.example.redwood.testing.widget.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LazyListTest {
  @Test
  fun emptyLazyColumn() = runTest {
    val snapshot = TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
        }
      }
      awaitSnapshot()
    }

    assertThat(snapshot)
      .containsExactly(
        LazyListValue(
          Modifier,
          isVertical = true,
          onViewportChanged = { _, _ -> },
          itemsBefore = 0,
          itemsAfter = 0,
          width = Constraint.Wrap,
          height = Constraint.Wrap,
          margin = Margin(0.0.dp),
          crossAxisAlignment = CrossAxisAlignment.Start,
          scrollItemIndex = ScrollItemIndex(0, 0),
          placeholder = List(20) { TextValue(Modifier, "Placeholder") },
          items = emptyList(),
        ),
      )
  }
}
