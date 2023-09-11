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
package app.cash.redwood.lazylayout.widget

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class WindowedLazyListTest {
  @Test fun updateViewport_0() = updateViewport(40..49, 30..60)
  @Test fun updateViewport_1() = updateViewport(41..50, 30..70)
  @Test fun updateViewport_2() = updateViewport(42..51, 30..70)
  @Test fun updateViewport_3() = updateViewport(49..58, 30..70)
  @Test fun updateViewport_4() = updateViewport(59..68, 40..80)

  private fun updateViewport(
    visibleItemIndexRange: IntRange,
    expectedViewportChange: IntRange,
  ) = runTest {
    val windowedLazyList = FakeWindowedLazyList(this, "foo")
    repeat(100) {
      windowedLazyList.items.insert(
        it,
        object : Widget<String> {
          override val value: String = it.toString()
          override var modifier: Modifier = Modifier
        },
      )
    }
    windowedLazyList.updateViewport(visibleItemIndexRange.first, visibleItemIndexRange.last)
    windowedLazyList.viewportChanges.test {
      assertThat(awaitItem()).isEqualTo(expectedViewportChange)
    }
  }

  @Test
  fun viewportUpdatesAreDeduplicated() = runTest {
    val windowedLazyList = FakeWindowedLazyList(this, "foo")
    repeat(100) {
      windowedLazyList.items.insert(
        it,
        object : Widget<String> {
          override val value: String = it.toString()
          override var modifier: Modifier = Modifier
        },
      )
    }
    windowedLazyList.updateViewport(41, 50)
    windowedLazyList.updateViewport(41, 50)
    windowedLazyList.updateViewport(49, 58)
    windowedLazyList.updateViewport(59, 68)
    windowedLazyList.viewportChanges.test {
      assertThat(awaitItem()).isEqualTo(30..70)
      assertThat(awaitItem()).isEqualTo(40..80)
    }
  }
}

private class FakeWindowedLazyList<W : Any>(
  private val scope: CoroutineScope,
  override val value: W,
) : WindowedLazyList<W>(NoOpListUpdateCallback) {
  private val _viewportChanges = MutableSharedFlow<IntRange>()
  val viewportChanges: SharedFlow<IntRange> = _viewportChanges

  override var modifier: Modifier = Modifier
  override val placeholder: Widget.Children<W> = MutableListChildren()

  init {
    onViewportChanged { firstPagedItemIndex, lastPagedItemIndex ->
      scope.launch {
        _viewportChanges.emit(firstPagedItemIndex..lastPagedItemIndex)
      }
    }
  }

  override fun isVertical(isVertical: Boolean) = error("unexpected call")
  override fun width(width: Constraint) = error("unexpected call")
  override fun height(height: Constraint) = error("unexpected call")
  override fun margin(margin: Margin) = error("unexpected call")
  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) = error("unexpected call")
  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) = error("unexpected call")
}

private object NoOpListUpdateCallback : ListUpdateCallback {
  override fun onInserted(position: Int, count: Int) = Unit
  override fun onMoved(fromPosition: Int, toPosition: Int, count: Int) = Unit
  override fun onRemoved(position: Int, count: Int) = Unit
}
