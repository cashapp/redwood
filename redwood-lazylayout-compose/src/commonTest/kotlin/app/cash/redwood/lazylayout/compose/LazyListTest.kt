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
import app.cash.redwood.testing.WidgetValue
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
        DefaultLazyListValue.copy(
          placeholder = List(20) { TextValue(Modifier, "Placeholder") },
        ),
      )
  }

  @Test
  fun populatedLazyColumnVariesItemsAfter_0() =
    populatedLazyColumnVariesItemsAfter(10, 0, 10)

  @Test
  fun populatedLazyColumnVariesItemsAfter_1() =
    populatedLazyColumnVariesItemsAfter(100, 85, 15)

  private fun populatedLazyColumnVariesItemsAfter(
    itemCount: Int,
    expectedItemsAfter: Int,
    expectedItemCount: Int,
  ) = runTest {
    val snapshot = TestSchemaTester {
      setContent {
        LazyColumn(
          state = rememberLazyListState(),
          placeholder = { Text("Placeholder") },
        ) {
          items(itemCount) {
            Text(it.toString())
          }
        }
      }
      awaitSnapshot()
    }

    assertThat(snapshot)
      .containsExactly(
        DefaultLazyListValue.copy(
          itemsAfter = expectedItemsAfter,
          placeholder = List(20) { TextValue(Modifier, "Placeholder") },
          items = List(expectedItemCount) { TextValue(Modifier, it.toString()) },
        ),
      )
  }
}

private val DefaultLazyListValue = LazyListValue(
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
  placeholder = emptyList(),
  items = emptyList(),
)

private fun LazyListValue.copy(
  modifier: Modifier = this.modifier,
  isVertical: Boolean = this.isVertical,
  onViewportChanged: (Int, Int) -> Unit = this.onViewportChanged,
  itemsBefore: Int = this.itemsBefore,
  itemsAfter: Int = this.itemsAfter,
  width: Constraint = this.width,
  height: Constraint = this.height,
  margin: Margin = this.margin,
  crossAxisAlignment: CrossAxisAlignment = this.crossAxisAlignment,
  scrollItemIndex: ScrollItemIndex = this.scrollItemIndex,
  placeholder: List<WidgetValue> = this.placeholder,
  items: List<WidgetValue> = this.items,
) = LazyListValue(
  modifier = modifier,
  isVertical = isVertical,
  onViewportChanged = onViewportChanged,
  itemsBefore = itemsBefore,
  itemsAfter = itemsAfter,
  width = width,
  height = height,
  margin = margin,
  crossAxisAlignment = crossAxisAlignment,
  scrollItemIndex = scrollItemIndex,
  placeholder = placeholder,
  items = items,
)
