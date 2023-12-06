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
import app.cash.redwood.layout.modifier.Flex
import app.cash.redwood.layout.modifier.Grow
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Shrink
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlin.test.Test

abstract class AbstractFlexContainerTest<T : Any> {
  abstract fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int = Color(51, 0, 0, 255),
  ): TestFlexContainer<T>

  abstract fun row(): Row<T>

  abstract fun column(): Column<T>

  abstract fun widget(
    backgroundColor: Int = Green,
  ): Text<T>

  fun widget(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Int = Green,
  ): Text<T> {
    val widget = widget(backgroundColor)
    widget.text(text)
    widget.modifier = modifier
    return widget
  }

  abstract fun verifySnapshot(
    container: Widget<T>,
    name: String? = null,
  )

  @Test fun testEmptyLayout_Column() {
    emptyLayout(FlexDirection.Column)
  }

  @Test fun testEmptyLayout_Row() {
    emptyLayout(FlexDirection.Row)
  }

  private fun emptyLayout(
    flexDirection: FlexDirection,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLayoutWithConstraints_Column_Wrap_Wrap() {
    layoutWithConstraints(FlexDirection.Column, Constraint.Wrap, Constraint.Wrap)
  }

  @Test fun testLayoutWithConstraints_Column_Wrap_Fill() {
    layoutWithConstraints(FlexDirection.Column, Constraint.Wrap, Constraint.Fill)
  }

  @Test fun testLayoutWithConstraints_Column_Fill_Wrap() {
    layoutWithConstraints(FlexDirection.Column, Constraint.Fill, Constraint.Wrap)
  }

  @Test fun testLayoutWithConstraints_Column_Fill_Fill() {
    layoutWithConstraints(FlexDirection.Column, Constraint.Fill, Constraint.Fill)
  }

  @Test fun testLayoutWithConstraints_Row_Wrap_Wrap() {
    layoutWithConstraints(FlexDirection.Row, Constraint.Wrap, Constraint.Wrap)
  }

  @Test fun testLayoutWithConstraints_Row_Wrap_Fill() {
    layoutWithConstraints(FlexDirection.Row, Constraint.Wrap, Constraint.Fill)
  }

  @Test fun testLayoutWithConstraints_Row_Fill_Wrap() {
    layoutWithConstraints(FlexDirection.Row, Constraint.Fill, Constraint.Wrap)
  }

  @Test fun testLayoutWithConstraints_Row_Fill_Fill() {
    layoutWithConstraints(FlexDirection.Row, Constraint.Fill, Constraint.Fill)
  }

  private fun layoutWithConstraints(
    flexDirection: FlexDirection,
    width: Constraint,
    height: Constraint,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
    val container = flexContainer(flexDirection)
    container.width(width)
    container.height(height)
    container.add(widget(movies.first()))
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testShortLayout_Column() {
    shortLayout(FlexDirection.Column)
  }

  @Test fun testShortLayout_Row() {
    shortLayout(FlexDirection.Row)
  }

  private fun shortLayout(
    flexDirection: FlexDirection,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.take(5).forEach { movie ->
      container.add(widget(movie))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLongLayout_Column() {
    longLayout(FlexDirection.Column)
  }

  @Test fun testLongLayout_Row() {
    longLayout(FlexDirection.Row)
  }

  private fun longLayout(
    flexDirection: FlexDirection,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
    val container = flexContainer(flexDirection)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    movies.forEach { movie ->
      container.add(widget(movie))
    }
    container.onEndChanges()
    verifySnapshot(container)
  }

  @Test fun testLayoutWithMarginAndDifferentAlignments_Column() {
    layoutWithMarginAndDifferentAlignments(FlexDirection.Column)
  }

  @Test fun testLayoutWithMarginAndDifferentAlignments_Row() {
    layoutWithMarginAndDifferentAlignments(FlexDirection.Row)
  }

  private fun layoutWithMarginAndDifferentAlignments(
    flexDirection: FlexDirection,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
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
    layoutWithCrossAxisAlignment(FlexDirection.Column, CrossAxisAlignment.Start)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_Center() {
    layoutWithCrossAxisAlignment(FlexDirection.Column, CrossAxisAlignment.Center)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_End() {
    layoutWithCrossAxisAlignment(FlexDirection.Column, CrossAxisAlignment.End)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Column_Stretch() {
    layoutWithCrossAxisAlignment(FlexDirection.Column, CrossAxisAlignment.Stretch)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Start() {
    layoutWithCrossAxisAlignment(FlexDirection.Row, CrossAxisAlignment.Start)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Center() {
    layoutWithCrossAxisAlignment(FlexDirection.Row, CrossAxisAlignment.Center)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_End() {
    layoutWithCrossAxisAlignment(FlexDirection.Row, CrossAxisAlignment.End)
  }

  @Test fun testLayoutWithCrossAxisAlignment_Row_Stretch() {
    layoutWithCrossAxisAlignment(FlexDirection.Row, CrossAxisAlignment.Stretch)
  }

  private fun layoutWithCrossAxisAlignment(
    flexDirection: FlexDirection,
    crossAxisAlignment: CrossAxisAlignment,
  ) {
    assumeTrue(flexDirection in listOf(FlexDirection.Row, FlexDirection.Column))
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
    columnWithMainAxisAlignment(MainAxisAlignment.Center)
  }

  @Test fun testColumnWithMainAxisAlignment_SpaceBetween() {
    columnWithMainAxisAlignment(MainAxisAlignment.SpaceBetween)
  }

  @Test fun testColumnWithMainAxisAlignment_SpaceAround() {
    columnWithMainAxisAlignment(MainAxisAlignment.SpaceAround)
  }

  private fun columnWithMainAxisAlignment(
    mainAxisAlignment: MainAxisAlignment,
  ) {
    assumeTrue(mainAxisAlignment in listOf(MainAxisAlignment.Center, MainAxisAlignment.SpaceBetween, MainAxisAlignment.SpaceAround))
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

  @Test
  fun testDynamicElementUpdates() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widget("A"))
    container.add(widget("B"))
    container.add(widget("D"))
    container.add(widget("E"))

    container.onEndChanges()
    verifySnapshot(container, "ABDE")

    container.addAt(index = 2, widget = widget("C"))
    container.onEndChanges()
    verifySnapshot(container, "ABCDE")

    container.removeAt(index = 0)
    container.onEndChanges()
    verifySnapshot(container, "BCDE")
  }

  @Test
  fun testDynamicContainerSize() {
    val parent = column().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    parent.children.insert(
      0,
      flexContainer(FlexDirection.Column).apply {
        modifier = GrowImpl(1.0)
        width(Constraint.Fill)
        mainAxisAlignment(MainAxisAlignment.SpaceBetween)
        add(
          widget(
            "A",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.Start)),
          ),
        )
        add(
          widget(
            "B",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.End)),
          ),
        )
      },
    )

    parent.children.insert(
      1,
      flexContainer(FlexDirection.Column).apply {
        modifier = GrowImpl(1.0)
        width(Constraint.Fill)
        mainAxisAlignment(MainAxisAlignment.SpaceBetween)
        add(
          widget(
            "C",
            GrowImpl(1.0)
              .then(CrossAxisAlignmentImpl(CrossAxisAlignment.Start)),
          ),
        )
        add(
          widget(
            "D",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.End)),
          ),
        )
      },
    )

    verifySnapshot(parent, "both")

    parent.children.remove(index = 1, count = 1)
    verifySnapshot(parent, "single")
  }

  @Test
  fun testFlexDistributesWeightEqually() {
    val container = flexContainer(FlexDirection.Row)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widget("REALLY LONG TEXT", FlexImpl(1.0)))
    container.add(widget("SHORTER TEXT", FlexImpl(1.0)))
    container.add(widget("A", FlexImpl(1.0)))
    container.add(widget("LINE1\nLINE2\nLINE3", FlexImpl(1.0)))
    verifySnapshot(container)
  }

  @Test
  fun testFlexDistributesWeightUnequally() {
    val container = flexContainer(FlexDirection.Row)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widget("REALLY LONG TEXT", FlexImpl(3.0)))
    container.add(widget("SHORTER TEXT", FlexImpl(1.0)))
    container.add(widget("A", FlexImpl(1.0)))
    container.add(widget("LINE1\nLINE2\nLINE3", FlexImpl(1.0)))
    verifySnapshot(container)
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
  fun addAt(index: Int, widget: Widget<T>)
  fun removeAt(index: Int)
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
  override val margin: Margin,
) : MarginModifier {
  constructor(all: Dp = 0.dp) : this(Margin(all))
}

private data class GrowImpl(
  override val value: Double,
) : Grow

private data class ShrinkImpl(
  override val value: Double,
) : Shrink

private data class FlexImpl(
  override val value: Double,
) : Flex
