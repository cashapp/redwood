/*
 * Copyright (C) 2022 Square, Inc.
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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.widget.Widget
import org.junit.Test

abstract class AbstractFlexContainerTest<T : Any> {
  abstract fun flexContainer(direction: FlexDirection): TestFlexContainer<T>
  abstract fun widget(text: String, layoutModifier: LayoutModifier = LayoutModifier): Widget<T>
  abstract fun verifySnapshot(container: TestFlexContainer<T>)

  @Test fun shortRow() {
    val container = flexContainer(FlexDirection.Row)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun shortColumn() {
    val container = flexContainer(FlexDirection.Column)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun longRow() {
    val container = flexContainer(FlexDirection.Row)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun longColumn() {
    val container = flexContainer(FlexDirection.Column)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun columnWithPaddingAndDifferentAlignments() {
    val container = flexContainer(FlexDirection.Column)
    container.padding(Padding(horizontal = 10, vertical = 20))
    movies.forEachIndexed { index, movie ->
      val modifier = when (index % 4) {
        0 -> CrossAxisAlignmentImpl(CrossAxisAlignment.Start)
        1 -> CrossAxisAlignmentImpl(CrossAxisAlignment.Center)
        2 -> CrossAxisAlignmentImpl(CrossAxisAlignment.End)
        else -> CrossAxisAlignmentImpl(CrossAxisAlignment.Stretch)
      }
      container.add(widget(movie, modifier))
    }
    verifySnapshot(container)
  }
}

interface TestFlexContainer<T : Any> {
  val value: T
  fun alignItems(alignItems: AlignItems)
  fun justifyContent(justifyContent: JustifyContent)
  fun padding(padding: Padding)
  fun add(widget: Widget<T>)
}

private val movies = listOf(
  "The Shawshank Redemption",
  "The Godfather",
  "The Dark Knight",
  "The Godfather Part II",
  "12 Angry Men",
  "Schindler's List",
  "The Lord of the Rings: The Return of the King",
  "Pulp Fiction",
  "The Lord of the Rings: The Fellowship of the Ring",
  "The Good, the Bad and the Ugly",
  "Forrest Gump",
  "Fight Club",
  "Inception",
  "The Lord of the Rings: The Two Towers",
  "Star Wars: Episode V - The Empire Strikes Back",
  "The Matrix",
  "Goodfellas",
  "One Flew Over the Cuckoo's Nest",
  "Se7en",
  "Seven Samurai",
)

private data class CrossAxisAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment, VerticalAlignment
