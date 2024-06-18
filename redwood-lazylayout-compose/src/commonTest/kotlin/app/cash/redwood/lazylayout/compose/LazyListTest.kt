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
import app.cash.redwood.lazylayout.testing.LazyListValue
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.testing.TestSchemaTester
import com.example.redwood.testapp.testing.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LazyListTest {
  @Test
  fun emptyLazyColumn() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
        }
      }

      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.itemsAfter).isEqualTo(0)
        assertThat(lazyList.items).isEmpty()
        assertThat(lazyList.placeholder)
          .isEqualTo(List(20) { TextValue(Modifier, "Placeholder") })
      }
    }
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
    TestSchemaTester {
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

      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.itemsAfter).isEqualTo(expectedItemsAfter)
        assertThat(lazyList.placeholder)
          .isEqualTo(List(20) { TextValue(Modifier, "Placeholder") })
        assertThat(lazyList.items)
          .isEqualTo(List(expectedItemCount) { TextValue(Modifier, it.toString()) })
      }
    }
  }

  @Test
  fun scrollPopulatedLazyColumn() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(50, 60)
      }

      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(50 - 5)
        assertThat(lazyList.itemsAfter).isEqualTo(100 - (60 + 20))
        assertThat(lazyList.placeholder)
          .isEqualTo(List(20) { TextValue(Modifier, "Placeholder") })
        assertThat(lazyList.items)
          .isEqualTo(List(35) { TextValue(Modifier, (it + 45).toString()) })
      }
    }
  }

  @Test
  fun scrollDoesNotTriggerRecompose() = runTest {
    TestSchemaTester {
      var index5ComposeCount = 0
      setContent {
        val lazyListState = rememberLazyListState().apply {
          preloadItems = false
        }
        LazyColumn(
          state = lazyListState,
          placeholder = { Text("Placeholder") },
        ) {
          items(100) {
            if (it == 5) index5ComposeCount++
            Text(it.toString())
          }
        }
      }

      // Initially, the item at index 5 is never composed.
      val lazyList = awaitSnapshot().filterIsInstance<LazyListValue>().single()
      assertThat(index5ComposeCount).isEqualTo(0)

      // Growing the scroll window to include the preceding item doesn't change that.
      lazyList.onViewportChanged(0, 4)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(0)

      // But scrolling to include it causes the first composition.
      lazyList.onViewportChanged(2, 6)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(1)

      // Further scrolling doesn't cause it to be recomposed.
      lazyList.onViewportChanged(4, 8)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(1)

      // Even when it's scrolled off-screen.
      lazyList.onViewportChanged(6, 10)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(1)

      // The item at index 5 remains composed because (6, 10) is contiguous with (4, 8).
      lazyList.onViewportChanged(4, 8)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(1)

      // Scrolling to a non-contiguous range causes a recomposition.
      lazyList.onViewportChanged(9, 13)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(1)

      lazyList.onViewportChanged(4, 8)
      awaitSnapshot()
      assertThat(index5ComposeCount).isEqualTo(2)
    }
  }
}
