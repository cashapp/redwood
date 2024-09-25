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
package app.cash.redwood.lazylayout

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.argb
import app.cash.redwood.snapshot.testing.text
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import kotlin.test.Test

abstract class AbstractLazyListTest<T : Any> {
  abstract val widgetFactory: TestWidgetFactory<T>

  abstract fun lazyList(
    backgroundColor: Int = argb(51, 0, 0, 255),
  ): LazyList<T>

  private fun defaultLazyList(): LazyList<T> {
    val result = lazyList()
    for (i in 0 until 10) {
      result.placeholder.insert(i, widgetFactory.text("..."))
    }
    result.isVertical(true)
    result.itemsBefore(0)
    result.itemsAfter(0)
    result.width(Constraint.Fill)
    result.height(Constraint.Fill)
    result.margin(Margin(all = 0.dp))
    result.crossAxisAlignment(CrossAxisAlignment.Stretch)
    result.scrollItemIndex(ScrollItemIndex(id = 0, index = 0))
    return result
  }

  abstract fun snapshotter(widget: T): Snapshotter

  @Test
  fun testHappyPath() {
    val lazyList = defaultLazyList()

    for ((index, value) in movies.take(5).withIndex()) {
      lazyList.items.insert(index, widgetFactory.text(value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()

    snapshotter(lazyList.value).snapshot()
  }

  @Test
  fun testPlaceholderToLoadedAndLoadedToPlaceholder() {
    val lazyList = defaultLazyList()
    val snapshotter = snapshotter(lazyList.value)

    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter.snapshot("0 empty")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(10)
    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter.snapshot("1 placeholders")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(0)
    for ((index, value) in movies.take(10).withIndex()) {
      lazyList.items.insert(index, widgetFactory.text(value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter.snapshot("2 loaded")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(10)
    lazyList.items.remove(0, 10)
    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter.snapshot("3 placeholders")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(0)
    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter.snapshot("4 empty")
  }

  @Test
  fun testPlaceholdersExhausted() {
    val lazyList = defaultLazyList()

    lazyList.itemsBefore(11)
    for ((index, value) in movies.take(1).withIndex()) {
      lazyList.items.insert(index, widgetFactory.text(value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()
    snapshotter(lazyList.value).snapshot()
  }
}

private val movies = listOf(
  "The Godfather",
  "The Dark Knight",
  "12 Angry Men",
  "Schindler's List",
  "Pulp Fiction",
  "Forrest Gump",
  "Fight Club",
  "Inception",
  "The Matrix",
  "Goodfellas",
  "Se7en",
  "Seven Samurai",
)
