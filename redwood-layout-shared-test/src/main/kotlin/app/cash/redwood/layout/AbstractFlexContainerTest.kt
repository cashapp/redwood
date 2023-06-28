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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Alignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.JustifyContent
import com.google.testing.junit.testparameterinjector.TestParameter
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test

@Suppress("JUnitMalformedDeclaration")
abstract class AbstractFlexContainerTest<T : Any> {
  abstract fun flexContainer(direction: FlexDirection): TestFlexContainer<T>
  abstract fun widget(text: String, modifier: Modifier = Modifier): Widget<T>
  abstract fun verifySnapshot(container: TestFlexContainer<T>, name: String? = null)

  @Test fun emptyLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.toFlexDirection()
    val container = flexContainer(flexDirection)
    container.alignItems(AlignItems.FlexStart)
    verifySnapshot(container)
  }

  @Test fun shortLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.toFlexDirection()
    val container = flexContainer(flexDirection)
    container.alignItems(AlignItems.FlexStart)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun longLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.toFlexDirection()
    val container = flexContainer(flexDirection)
    container.alignItems(AlignItems.FlexStart)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun columnWithMarginAndDifferentAlignments() {
    val container = flexContainer(FlexDirection.Column)
    container.margin(Margin(start = 5.dp, end = 10.dp, top = 20.dp, bottom = 20.dp))
    movies.forEachIndexed { index, movie ->
      val modifier = when (index % 4) {
        0 -> AlignmentImpl(CrossAxisAlignment.Start)
        1 -> AlignmentImpl(CrossAxisAlignment.Center)
        2 -> AlignmentImpl(CrossAxisAlignment.End)
        else -> AlignmentImpl(CrossAxisAlignment.Stretch)
      }
      container.add(widget(movie, modifier))
    }
    verifySnapshot(container)
  }

  @Test fun layoutWithAlignItems(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
    @TestParameter alignItemsEnum: AlignItemsEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    assumeFalse(alignItemsEnum == AlignItemsEnum.Baseline)
    val flexDirection = flexDirectionEnum.toFlexDirection()
    val alignItems = alignItemsEnum.toAlignItems()
    val container = flexContainer(flexDirection)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(alignItems)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test
  fun columnWithUpdatedAlignItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(AlignItems.Center)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container, "Center")
    container.alignItems(AlignItems.FlexEnd)
    verifySnapshot(container, "FlexEnd")
  }

  @Test fun columnWithJustifyContent(
    @TestParameter justifyContentEnum: JustifyContentEnum,
  ) {
    assumeTrue(justifyContentEnum in listOf(JustifyContentEnum.Center, JustifyContentEnum.SpaceBetween, JustifyContentEnum.SpaceAround))
    val justifyContent = justifyContentEnum.toJustifyContent()
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(AlignItems.FlexStart)
    container.justifyContent(justifyContent)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedWidthItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(AlignItems.FlexStart)
    repeat(10) { index ->
      container.add(widget("$index", WidthImpl(50.dp)))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedHeightItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(AlignItems.FlexStart)
    repeat(10) { index ->
      container.add(widget("$index", HeightImpl(50.dp)))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedSizeItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.alignItems(AlignItems.FlexStart)
    repeat(10) { index ->
      container.add(widget("$index", SizeImpl(50.dp, 50.dp)))
    }
    verifySnapshot(container)
  }
}

interface TestFlexContainer<T : Any> {
  val value: T
  fun width(constraint: Constraint)
  fun height(constraint: Constraint)
  fun alignItems(alignItems: AlignItems)
  fun justifyContent(justifyContent: JustifyContent)
  fun margin(margin: Margin)
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

private data class AlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : Alignment

private data class WidthImpl(
  override val width: Dp,
) : Width

private data class HeightImpl(
  override val height: Dp,
) : Height

private data class SizeImpl(
  override val width: Dp,
  override val height: Dp,
) : Size

enum class FlexDirectionEnum {
  Row,
  RowReverse,
  Column,
  ColumnReverse,
}

private fun FlexDirectionEnum.toFlexDirection() = when (this) {
  FlexDirectionEnum.Row -> FlexDirection.Row
  FlexDirectionEnum.RowReverse -> FlexDirection.RowReverse
  FlexDirectionEnum.Column -> FlexDirection.Column
  FlexDirectionEnum.ColumnReverse -> FlexDirection.ColumnReverse
}

enum class AlignItemsEnum {
  FlexStart,
  FlexEnd,
  Center,
  Baseline,
  Stretch,
}

private fun AlignItemsEnum.toAlignItems() = when (this) {
  AlignItemsEnum.FlexStart -> AlignItems.FlexStart
  AlignItemsEnum.FlexEnd -> AlignItems.FlexEnd
  AlignItemsEnum.Center -> AlignItems.Center
  AlignItemsEnum.Baseline -> AlignItems.Baseline
  AlignItemsEnum.Stretch -> AlignItems.Stretch
}

enum class JustifyContentEnum {
  FlexStart,
  FlexEnd,
  Center,
  SpaceBetween,
  SpaceAround,
  SpaceEvenly,
}

private fun JustifyContentEnum.toJustifyContent() = when (this) {
  JustifyContentEnum.FlexStart -> JustifyContent.FlexStart
  JustifyContentEnum.FlexEnd -> JustifyContent.FlexEnd
  JustifyContentEnum.Center -> JustifyContent.Center
  JustifyContentEnum.SpaceBetween -> JustifyContent.SpaceBetween
  JustifyContentEnum.SpaceAround -> JustifyContent.SpaceAround
  JustifyContentEnum.SpaceEvenly -> JustifyContent.SpaceEvenly
}
