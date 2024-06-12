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

      // We get an update with just what's on screen, plus a tiny bit in the direction of scroll.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(50, 60 + 5)
      }

      // When we stop scrolling, we grow the loaded window, slightly favoring the last scroll
      // direction.
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

      // Scroll to position 50...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(50, 60)
      }

      // Confirm we got that.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(50, 60 + 5)
        lazyList.onViewportChanged(6, 16)
      }

      // Scroll up again. We should get 5 more elements in the direction of scroll.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(1, 16)
      }

      // When we stop scrolling we should get 10 elements before and 20 after.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(0, 26)
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

      // Scroll to position 89...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(89, 99)
      }

      // We get an update with what's on screen, but nothing beyond that.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(89, 100)
      }

      // When we stop scrolling, we grow the loaded window.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(79, 100)
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

      // Scroll to position 15...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.onViewportChanged(15, 25)
      }

      // We don't evict anything at the front until we have to...
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(0, 25 + 5)
        lazyList.onViewportChanged(16, 26)
      }

      // But when we need to evict, we evict it all.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(15, 26 + 5)
      }

      // Once "at rest", we can grow the window in both directions.
      with(awaitSnapshot()) {
        val lazyList = single() as LazyListValue
        lazyList.assertLoadedWindow(6, 26 + 20)
      }
    }
  }

  private fun LazyListValue.assertLoadedWindow(position: Int, limit: Int) {
    assertThat(itemsBefore).isEqualTo(position)
    assertThat(itemsBefore + items.size).isEqualTo(limit)
  }
}
