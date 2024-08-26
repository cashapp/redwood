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
package app.cash.redwood.layout

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import kotlin.test.Test

abstract class AbstractLazyListTest<T : Any> {
  abstract fun text(): Text<T>

  private fun coloredText(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Int = Green,
  ) = text().apply {
    this.modifier = modifier
    text(text)
    bgColor(backgroundColor)
  }

  abstract fun lazyList(
    backgroundColor: Int = argb(51, 0, 0, 255),
  ): LazyList<T>

  private fun defaultLazyList(): LazyList<T> {
    val result = lazyList()
    for (i in 0 until 10) {
      result.placeholder.insert(i, coloredText(text = "..."))
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

  abstract fun verifySnapshot(
    container: Widget<T>,
    name: String? = null,
  )

  @Test fun happyPath() {
    val lazyList = defaultLazyList()

    for ((index, value) in movies.take(5).withIndex()) {
      lazyList.items.insert(index, coloredText(text = value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()

    verifySnapshot(lazyList)
  }

  @Test fun placeholderToLoadedAndLoadedToPlaceholder() {
    val lazyList = defaultLazyList()

    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList, "0 empty")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(10)
    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList, "1 placeholders")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(0)
    for ((index, value) in movies.take(10).withIndex()) {
      lazyList.items.insert(index, coloredText(text = value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList, "2 loaded")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(10)
    lazyList.items.remove(0, 10)
    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList, "3 placeholders")

    lazyList.itemsBefore(0)
    lazyList.itemsAfter(0)
    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList, "4 empty")
  }

  @Test fun placeholdersExhausted() {
    val lazyList = defaultLazyList()

    lazyList.itemsBefore(11)
    for ((index, value) in movies.take(1).withIndex()) {
      lazyList.items.insert(index, coloredText(text = value))
    }
    (lazyList as? ChangeListener)?.onEndChanges()
    verifySnapshot(lazyList)
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
