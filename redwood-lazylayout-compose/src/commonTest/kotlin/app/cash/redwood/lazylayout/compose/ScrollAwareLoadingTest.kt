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

import app.cash.redwood.lazylayout.testing.LazyListValue
import assertk.assertThat
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

  private fun LazyListValue.assertLoadedWindow(position: Int, limit: Int) {
    assertThat(itemsBefore).isEqualTo(position)
    assertThat(itemsBefore + items.size).isEqualTo(limit)
  }
}
