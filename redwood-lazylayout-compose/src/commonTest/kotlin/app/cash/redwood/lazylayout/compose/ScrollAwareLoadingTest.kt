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
package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import app.cash.redwood.lazylayout.testing.LazyListValue
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.testing.TestSchemaTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ScrollAwareLoadingTest {

  @Test
  fun onlyLoadTheScrollWindowUntilScrollingStops() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      // Scroll to position 50...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(50, 60)
      }

      // We get an update with what's on screen,
      // plus 20 in the direction of the scroll and 5 in the opposite direction.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(50 - 5, 60 + 20)
      }

      // When we stop scrolling, we grow the loaded window.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(50 - 10, 60 + 20)
      }
    }
  }

  @Test
  fun onlyLoadTheScrollWindowUntilScrollingStopsWhenScrollingUp() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      // Scroll down to position 50...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(50, 60)
      }

      // Confirm we got that.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(50 - 5, 60 + 20)
        lazyList.onViewportChanged(6, 16)
      }

      // Scroll back up. We should get up to 20 more elements in the direction of scroll,
      // and 5 in the opposite direction.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(0, 16 + 5)
      }

      // When we stop scrolling we should get up to 20 elements before and 10 after.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(0, 16 + 10)
      }
    }
  }

  @Test
  fun scrollToEndOfRange() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      // Scroll down to position 89...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(89, 99)
      }

      // We get an update with what's on screen, plus 5 elements before.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(89 - 5, 100)
      }

      // When we stop scrolling, we grow the loaded window.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(89 - 10, 100)
      }
    }
  }

  @Test
  fun dontUnloadTheExistingLoadedWindow() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      // Scroll down to position 15...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(15, 25)
      }

      // We don't evict anything at the front until we have to...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(0, 25 + 20)
        lazyList.onViewportChanged(16, 26)
      }

      // But when we need to evict, we evict to fit the current loaded window.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(16 - 5, 26 + 20)
      }

      // Once "at rest", we can grow the window.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(16 - 10, 26 + 20)
      }
    }
  }

  @Test
  fun itemCountIsConsistentDuringScroll() = runTest {
    TestSchemaTester {
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(100) {
            Text(it.toString())
          }
        }
      }

      // Initial load.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(15)
        assertThat(lazyList.itemsAfter).isEqualTo(85)

        // Scroll down to position 15...
        lazyList.onViewportChanged(15, 25)
      }

      // First load at position 15 has 45 elements!
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(45)
        assertThat(lazyList.itemsAfter).isEqualTo(55)
      }

      // Subsequent load has only 40 elements.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(5)
        assertThat(lazyList.items).hasSize(40)
        assertThat(lazyList.itemsAfter).isEqualTo(55)

        // Scroll down to position 89...
        lazyList.onViewportChanged(89, 99)
      }

      // First load at position 89 has 16 elements!
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(84)
        assertThat(lazyList.items).hasSize(16)
        assertThat(lazyList.itemsAfter).isEqualTo(0)
      }

      // Subsequent load has 21 elements.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(79)
        assertThat(lazyList.items).hasSize(21)
        assertThat(lazyList.itemsAfter).isEqualTo(0)

        // Scroll up to position 0...
        lazyList.onViewportChanged(0, 10)
      }

      // First load at position 0 has 15 elements!
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(15)
        assertThat(lazyList.itemsAfter).isEqualTo(85)
      }

      // Subsequent load at position 0 has 20 elements!
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(20)
        assertThat(lazyList.itemsAfter).isEqualTo(80)
      }
    }
  }

  @Test
  fun itemCountChanges() = runTest {
    TestSchemaTester {
      val itemCount = mutableIntStateOf(100)
      setContent {
        LazyColumn(placeholder = { Text("Placeholder") }) {
          items(itemCount.value) {
            Text(it.toString())
          }
        }
      }

      // On the initial load there's 15 elements.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(15)
        assertThat(lazyList.itemsAfter).isEqualTo(85)

        // Scroll down to position 15...
        lazyList.onViewportChanged(15, 25)
      }

      // First load at position 15 has 45 elements!
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(45)
        assertThat(lazyList.itemsAfter).isEqualTo(55)
      }

      // Drop the item count to zero.
      itemCount.value = 0

      // We should see 0 items.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        assertThat(lazyList.itemsBefore).isEqualTo(0)
        assertThat(lazyList.items).hasSize(0)
        assertThat(lazyList.itemsAfter).isEqualTo(0)
      }
    }
  }

  private fun LazyListValue.assertLoadedWindow(position: Int, limit: Int) {
    assertThat(itemsBefore).isEqualTo(position)
    assertThat(itemsBefore + items.size).isEqualTo(limit)
  }
}
