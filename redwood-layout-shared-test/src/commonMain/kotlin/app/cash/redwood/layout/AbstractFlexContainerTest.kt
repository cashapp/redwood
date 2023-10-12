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
package app.cash.redwood.layout

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.modifier.Grow
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Shrink
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlin.test.Test
import kotlin.test.assertTrue

abstract class AbstractFlexContainerTest<T : Any> {
  abstract fun flexContainer(direction: FlexDirection): TestFlexContainer<T>
  abstract fun widget(): Text<T>
  abstract fun verifySnapshot(container: TestFlexContainer<T>, name: String? = null)

  private fun widget(text: String, modifier: Modifier = Modifier): Text<T> = widget().apply {
    text(text)
    this.modifier = modifier
  }

  @Test fun testEmptyLayout_Column() {
    emptyLayout(FlexDirectionEnum.Column)
  }

  @Test fun testEmptyLayout_Row() {
    emptyLayout(FlexDirectionEnum.Row)
  }

  private fun emptyLayout(
    flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLayoutWithConstraints_Column_Wrap_Wrap() {
    layoutWithConstraints(FlexDirectionEnum.Column, ConstraintEnum.Wrap, ConstraintEnum.Wrap)
  }

  @Test fun testLayoutWithConstraints_Column_Wrap_Fill() {
    layoutWithConstraints(FlexDirectionEnum.Column, ConstraintEnum.Wrap, ConstraintEnum.Fill)
  }

  @Test fun testLayoutWithConstraints_Column_Fill_Wrap() {
    layoutWithConstraints(FlexDirectionEnum.Column, ConstraintEnum.Fill, ConstraintEnum.Wrap)
  }

  @Test fun testLayoutWithConstraints_Column_Fill_Fill() {
    layoutWithConstraints(FlexDirectionEnum.Column, ConstraintEnum.Fill, ConstraintEnum.Fill)
  }

  @Test fun testLayoutWithConstraints_Row_Wrap_Wrap() {
    layoutWithConstraints(FlexDirectionEnum.Row, ConstraintEnum.Wrap, ConstraintEnum.Wrap)
  }

  @Test fun testLayoutWithConstraints_Row_Wrap_Fill() {
    layoutWithConstraints(FlexDirectionEnum.Row, ConstraintEnum.Wrap, ConstraintEnum.Fill)
  }

  @Test fun testLayoutWithConstraints_Row_Fill_Wrap() {
    layoutWithConstraints(FlexDirectionEnum.Row, ConstraintEnum.Fill, ConstraintEnum.Wrap)
  }

  @Test fun testLayoutWithConstraints_Row_Fill_Fill() {
    layoutWithConstraints(FlexDirectionEnum.Row, ConstraintEnum.Fill, ConstraintEnum.Fill)
  }

  private fun layoutWithConstraints(
    flexDirectionEnum: FlexDirectionEnum,
    widthEnum: ConstraintEnum,
    heightEnum: ConstraintEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val width = widthEnum.value
    val height = heightEnum.value
    val container = flexContainer(flexDirection)
    container.width(width)
    container.height(height)
    container.add(widget(movies.first()))
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testShortLayout_Column() {
    shortLayout(FlexDirectionEnum.Column)
  }

  @Test fun testShortLayout_Row() {
    shortLayout(FlexDirectionEnum.Row)
  }

  private fun shortLayout(
    flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLongLayout_Column() {
    longLayout(FlexDirectionEnum.Column)
  }

  @Test fun testLongLayout_Row() {
    longLayout(FlexDirectionEnum.Row)
  }

  private fun longLayout(
    flexDirectionEnum: FlexDirectionEnum,
  ) {
    assumeTrue(flexDirectionEnum in listOf(FlexDirectionEnum.Row, FlexDirectionEnum.Column))
    val flexDirection = flexDirectionEnum.value
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLayoutWithMarginAndDifferentAlignments_Column() {
    layoutWithMarginAndDifferentAlignments(FlexDirectionEnum.Column)
  }

  @Test fun testLayoutWithMarginAndDifferentAlignments_Row() {
    layoutWithMarginAndDifferentAlignments(FlexDirectionEnum.Row)
  }

  private fun layoutWithMarginAndDifferentAlignments(
    flexDirectionEnum: FlexDirectionEnum,
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
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_Start() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Column, CrossAxisAlignmentEnum.Start)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_Center() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Column, CrossAxisAlignmentEnum.Center)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_End() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Column, CrossAxisAlignmentEnum.End)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_Stretch() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Column, CrossAxisAlignmentEnum.Stretch)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Start() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Row, CrossAxisAlignmentEnum.Start)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Center() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Row, CrossAxisAlignmentEnum.Center)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_End() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Row, CrossAxisAlignmentEnum.End)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Stretch() {
    layoutWithCrossAxisAlignment(FlexDirectionEnum.Row, CrossAxisAlignmentEnum.Stretch)
  }

  private fun layoutWithCrossAxisAlignment(
    flexDirectionEnum: FlexDirectionEnum,
    crossAxisAlignmentEnum: CrossAxisAlignmentEnum,
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
    container.onEndChanges()
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
    container.onEndChanges()
    verifySnapshot(container, "Center")
    container.crossAxisAlignment(CrossAxisAlignment.End)
    container.onEndChanges()
    verifySnapshot(container, "FlexEnd")
  }

  @Test fun testColumnWithMainAxisAlignment_Center() {
    columnWithMainAxisAlignment(MainAxisAlignmentEnum.Center)
  }

  @Test fun testColumnWithMainAxisAlignment_SpaceBetween() {
    columnWithMainAxisAlignment(MainAxisAlignmentEnum.SpaceBetween)
  }

  @Test fun testColumnWithMainAxisAlignment_SpaceAround() {
    columnWithMainAxisAlignment(MainAxisAlignmentEnum.SpaceAround)
  }

  private fun columnWithMainAxisAlignment(
    mainAxisAlignmentEnum: MainAxisAlignmentEnum,
  ) {
    assumeTrue(mainAxisAlignmentEnum in listOf(MainAxisAlignmentEnum.Center, MainAxisAlignmentEnum.SpaceBetween, MainAxisAlignmentEnum.SpaceAround))
    val mainAxisAlignment = mainAxisAlignmentEnum.value
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    container.mainAxisAlignment(mainAxisAlignment)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testContainerWithFixedWidthItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", WidthImpl(50.dp)))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testContainerWithFixedHeightItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", HeightImpl(50.dp)))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testContainerWithFixedSizeItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widget("$index", SizeImpl(50.dp, 50.dp)))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testChildWithUpdatedProperty() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    val widget = widget("")
    container.add(widget)
    container.onEndChanges()
    verifySnapshot(container, "initial")
    widget.text(movies.first())
    container.onEndChanges()
    verifySnapshot(container, "updated")
  }

  @Test fun testColumnThenRow() {
    val column = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(10.dp))
        add(widget("first (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
        add(widget("second (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
      },
    )

    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(10.dp))
        add(widget("first (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
        add(widget("second (grow 0.0)", GrowImpl(0.0).then(MarginImpl(5.dp))))
      },
    )

    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(10.dp))
        add(widget("first (grow 0.0)", GrowImpl(0.0).then(MarginImpl(5.dp))))
        add(widget("second (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
      },
    )

    verifySnapshot(column)
  }

  /** This test demonstrates that margins are lost unless `shrink(1.0)` is added. */
  @Test fun testRowMargins() {
    val column = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    column.add(widget("All rows have a 100 px margin on the right!"))

    column.add(widget("1 element + no shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widget("x ".repeat(100), GrowImpl(1.0)))
      },
    )

    column.add(widget("1 element + shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widget("x ".repeat(100), GrowImpl(1.0).then(ShrinkImpl(1.0))))
      },
    )

    column.add(widget("2 elements + no shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widget("x ".repeat(100), GrowImpl(1.0)))
        add(widget("abcdef", MarginImpl(Margin(start = 10.dp))))
      },
    )

    column.add(widget("2 elements + shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widget("x ".repeat(100), GrowImpl(1.0).then(ShrinkImpl(1.0))))
        add(widget("abcdef", MarginImpl(Margin(start = 10.dp))))
      },
    )

    verifySnapshot(column)
  }

  /** We don't have assume() on kotlin.test. Tests that fail here should be skipped instead. */
  private fun assumeTrue(b: Boolean) {
    assertTrue(b)
  }
}

interface TestFlexContainer<T : Any> : Widget<T>, ChangeListener {
  override val value: T
  fun width(width: Constraint)
  fun height(height: Constraint)
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

private data class MarginImpl(
  override val margin: app.cash.redwood.ui.Margin,
) : app.cash.redwood.layout.modifier.Margin {
  constructor(all: Dp = 0.dp) : this(Margin(all))
}

private data class GrowImpl(
  override val `value`: Double,
) : Grow

private data class ShrinkImpl(
  override val `value`: Double,
) : Shrink

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
