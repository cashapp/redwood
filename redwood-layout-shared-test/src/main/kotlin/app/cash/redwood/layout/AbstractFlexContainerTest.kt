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
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import com.google.testing.junit.testparameterinjector.TestParameter
import org.junit.Assume.assumeTrue
import org.junit.Test

@Suppress("JUnitMalformedDeclaration")
abstract class AbstractFlexContainerTest<T : Any> {
  abstract fun flexContainer(direction: FlexDirection): TestFlexContainer<T>
  abstract fun widget(text: String, modifier: Modifier = Modifier): Text<T>
  abstract fun verifySnapshot(container: TestFlexContainer<T>, name: String? = null)

  @Test fun emptyLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    verifySnapshot(container)
  }

  @Test fun layoutWithConstraints(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
    @TestParameter widthEnum: ConstraintEnum,
    @TestParameter heightEnum: ConstraintEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val width = widthEnum.value
    val height = heightEnum.value
    val container = flexContainer(flexDirection)
    container.width(width)
    container.height(height)
    container.add(widget(movies.first()))
    verifySnapshot(container)
  }

  @Test fun shortLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun longLayout(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun layoutWithMarginAndDifferentAlignments(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.margin(Margin(start = 5.dp, end = 10.dp, top = 20.dp, bottom = 20.dp))
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

  @Test fun layoutWithCrossAxisAlignment(
    @TestParameter flexDirectionEnum: FlexDirectionEnum,
    @TestParameter crossAxisAlignmentEnum: CrossAxisAlignmentEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val crossAxisAlignment = crossAxisAlignmentEnum.value
    val container = flexContainer(flexDirection)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(crossAxisAlignment)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test
  fun columnWithUpdatedCrossAxisAlignment() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Center)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container, "Center")
    container.crossAxisAlignment(CrossAxisAlignment.End)
    container.onEndChanges()
    verifySnapshot(container, "FlexEnd")
  }

  @Test fun columnWithMainAxisAlignment(
    @TestParameter mainAxisAlignmentEnum: MainAxisAlignmentEnum,
  ) {
    assumeTrue(
      mainAxisAlignmentEnum in listOf(
        MainAxisAlignmentEnum.Center,
        MainAxisAlignmentEnum.SpaceBetween,
        MainAxisAlignmentEnum.SpaceAround
      )
    )
    val mainAxisAlignment = mainAxisAlignmentEnum.value
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    container.mainAxisAlignment(mainAxisAlignment)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedWidthItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", WidthImpl(50.dp)))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedHeightItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", HeightImpl(50.dp)))
    }
    verifySnapshot(container)
  }

  @Test fun containerWithFixedSizeItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", SizeImpl(50.dp, 50.dp)))
    }
    verifySnapshot(container)
  }

  @Test fun childWithUpdatedProperty() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    val widget = widget("")
    container.add(widget)
    verifySnapshot(container, "initial")
    widget.text(movies.first())
    verifySnapshot(container, "updated")
  }
}

interface TestFlexContainer<T : Any> : ChangeListener {
  val value: T
  fun width(constraint: Constraint)
  fun height(constraint: Constraint)
  fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment)
  fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment)
  fun margin(margin: Margin)
  fun add(widget: Widget<T>)
}

interface Text<T : Any> : Widget<T> {
  fun text(text: String)
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

private data class CrossAxisAlignmentImpl(
  override val alignment: CrossAxisAlignment,
) : HorizontalAlignment, VerticalAlignment

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

enum class FlexDirectionEnum(val value: FlexDirection) {
  Row(FlexDirection.Row),
  RowReverse(FlexDirection.RowReverse),
  Column(FlexDirection.Column),
  ColumnReverse(FlexDirection.ColumnReverse),
}

enum class ConstraintEnum(val value: Constraint) {
  Wrap(Constraint.Wrap),
  Fill(Constraint.Fill),
}

enum class CrossAxisAlignmentEnum(val value: CrossAxisAlignment) {
  Start(CrossAxisAlignment.Start),
  Center(CrossAxisAlignment.Center),
  End(CrossAxisAlignment.End),
  Stretch(CrossAxisAlignment.Stretch),
}

enum class MainAxisAlignmentEnum(val value: MainAxisAlignment) {
  Start(MainAxisAlignment.Start),
  Center(MainAxisAlignment.Center),
  End(MainAxisAlignment.End),
  SpaceBetween(MainAxisAlignment.SpaceBetween),
  SpaceAround(MainAxisAlignment.SpaceAround),
  SpaceEvenly(MainAxisAlignment.SpaceEvenly),
}
