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
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.snapshot.testing.Blue
import app.cash.redwood.snapshot.testing.Green
import app.cash.redwood.snapshot.testing.Red
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.argb
import app.cash.redwood.snapshot.testing.text
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Px
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractFlexContainerTest<T : Any> {
  abstract val widgetFactory: TestWidgetFactory<T>

  protected val defaultBackgroundColor = argb(51, 0, 0, 255)

  abstract fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int = defaultBackgroundColor,
  ): TestFlexContainer<T>

  /**
   * Yoga node’s default values for properties like alignment are different from Redwood's default
   * values, so we explicitly apply those defaults here. This is only necessary in tests; in
   * production the framework explicitly sets every property.
   */
  protected fun TestFlexContainer<*>.applyDefaults() {
    width(Constraint.Wrap)
    height(Constraint.Wrap)
    margin(Margin.Zero)
    overflow(Overflow.Clip)
    crossAxisAlignment(CrossAxisAlignment.Start)
    mainAxisAlignment(MainAxisAlignment.Start)
    onScroll(null)
  }

  /**
   * Yoga node’s default values for properties like alignment are different from Redwood's default
   * values, so we explicitly apply those defaults here. This is only necessary in tests; in
   * production the framework explicitly sets every property.
   */
  protected fun Row<*>.applyDefaults() {
    width(Constraint.Wrap)
    height(Constraint.Wrap)
    margin(Margin.Zero)
    overflow(Overflow.Clip)
    verticalAlignment(CrossAxisAlignment.Start)
    horizontalAlignment(MainAxisAlignment.Start)
    onScroll(null)
  }

  /**
   * Yoga node’s default values for properties like alignment are different from Redwood's default
   * values, so we explicitly apply those defaults here. This is only necessary in tests; in
   * production the framework explicitly sets every property.
   */
  protected fun Column<*>.applyDefaults() {
    width(Constraint.Wrap)
    height(Constraint.Wrap)
    margin(Margin.Zero)
    overflow(Overflow.Clip)
    horizontalAlignment(CrossAxisAlignment.Start)
    verticalAlignment(MainAxisAlignment.Start)
    onScroll(null)
  }

  protected fun <T : Any> TestFlexContainer<T>.add(widget: Widget<T>) {
    addAt(children.widgets.size, widget)
  }

  protected fun <T : Any> TestFlexContainer<T>.addAt(index: Int, widget: Widget<T>) {
    children.insert(index, widget)
  }

  protected fun <T : Any> TestFlexContainer<T>.removeAt(index: Int) {
    children.remove(index, 1)
  }

  /** Returns a non-lazy flex container row, even if the test is for a LazyList. */
  abstract fun row(): Row<T>

  /** Returns a non-lazy flex container column, even if the test is for a LazyList. */
  abstract fun column(): Column<T>

  abstract fun spacer(
    backgroundColor: Int = argb(17, 0, 0, 0),
  ): Spacer<T>

  abstract fun snapshotter(widget: T): Snapshotter

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
    snapshotter(container.value).snapshot()
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
    container.add(
      widgetFactory.text(
        text = movies.first(),
        modifier = Modifier
          .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
          .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch)),
      ),
    )
    container.onEndChanges()
    snapshotter(container.value).snapshot()
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
      container.add(widgetFactory.text(movie))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
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
      container.add(widgetFactory.text(movie))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
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
      container.add(widgetFactory.text(movie, modifier))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
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
      container.add(widgetFactory.text(movie))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testColumnWithUpdatedCrossAxisAlignment() {
    val container = flexContainer(FlexDirection.Column)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Center)
    movies.forEach { movie ->
      container.add(widgetFactory.text(movie))
    }
    container.onEndChanges()
    snapshotter.snapshot("Center")
    container.crossAxisAlignment(CrossAxisAlignment.End)
    container.onEndChanges()
    snapshotter.snapshot("FlexEnd")
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
      container.add(widgetFactory.text(movie))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testContainerWithFixedWidthItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widgetFactory.text("$index", WidthImpl(50.dp)))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testContainerWithFixedHeightItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widgetFactory.text("$index", HeightImpl(50.dp)))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testContainerWithFixedSizeItems() {
    val container = flexContainer(FlexDirection.Column)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    repeat(10) { index ->
      container.add(widgetFactory.text("$index", SizeImpl(50.dp, 50.dp)))
    }
    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testRowWithFixedWidthHasChildWithFixedHeight() {
    val container = flexContainer(FlexDirection.Row).apply {
      crossAxisAlignment(CrossAxisAlignment.Start)
      modifier = WidthImpl(200.dp)
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    widgetFactory.text("A ".repeat(10)).apply {
      modifier = HeightImpl(50.dp)
      container.children.insert(0, this)
    }

    widgetFactory.text("B ".repeat(100)).apply {
      container.children.insert(1, this)
    }

    container.onEndChanges()
    snapshotter(container.value).snapshot()
  }

  @Test fun testChildWithUpdatedProperty() {
    val container = flexContainer(FlexDirection.Column)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)
    val widget = widgetFactory.text("")
    container.add(widget)
    container.onEndChanges()
    snapshotter.snapshot("initial")
    widget.text(movies.first())
    container.onEndChanges()
    snapshotter.snapshot("updated")
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
        add(widgetFactory.text("first (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
        add(widgetFactory.text("second (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
      },
    )

    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(10.dp))
        add(widgetFactory.text("first (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
        add(widgetFactory.text("second (grow 0.0)", GrowImpl(0.0).then(MarginImpl(5.dp))))
      },
    )

    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(10.dp))
        add(widgetFactory.text("first (grow 0.0)", GrowImpl(0.0).then(MarginImpl(5.dp))))
        add(widgetFactory.text("second (grow 1.0)", GrowImpl(1.0).then(MarginImpl(5.dp))))
      },
    )

    snapshotter(column.value).snapshot()
  }

  /** This test demonstrates that margins are lost unless `shrink(1.0)` is added. */
  @Test fun testRowMargins() {
    val column = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    column.add(widgetFactory.text("All rows have a 100 px margin on the right!"))

    column.add(widgetFactory.text("1 element + no shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widgetFactory.text("x ".repeat(100), GrowImpl(1.0)))
      },
    )

    column.add(widgetFactory.text("1 element + shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widgetFactory.text("x ".repeat(100), GrowImpl(1.0).then(ShrinkImpl(1.0))))
      },
    )

    column.add(widgetFactory.text("2 elements + no shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widgetFactory.text("x ".repeat(100), GrowImpl(1.0)))
        add(widgetFactory.text("abcdef", MarginImpl(Margin(start = 10.dp))))
      },
    )

    column.add(widgetFactory.text("2 elements + shrink:"))
    column.add(
      flexContainer(FlexDirection.Row).apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
        margin(Margin(end = 100.dp))
        add(widgetFactory.text("x ".repeat(100), GrowImpl(1.0).then(ShrinkImpl(1.0))))
        add(widgetFactory.text("abcdef", MarginImpl(Margin(start = 10.dp))))
      },
    )

    snapshotter(column.value).snapshot()
  }

  @Test fun testDynamicElementUpdates() {
    val container = flexContainer(FlexDirection.Column)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widgetFactory.text("A"))
    container.add(widgetFactory.text("B"))
    container.add(widgetFactory.text("D"))
    container.add(widgetFactory.text("E"))

    container.onEndChanges()
    snapshotter.snapshot("ABDE")

    container.addAt(index = 2, widget = widgetFactory.text("C"))
    container.onEndChanges()
    snapshotter.snapshot("ABCDE")

    container.removeAt(index = 0)
    container.onEndChanges()
    snapshotter.snapshot("BCDE")
  }

  @Test fun testDynamicContainerSize() {
    val parent = column().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }
    val snapshotter = snapshotter(parent.value)

    parent.children.insert(
      0,
      flexContainer(FlexDirection.Column).apply {
        modifier = Modifier
          .then(GrowImpl(1.0))
          .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
          .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch))
        width(Constraint.Fill)
        mainAxisAlignment(MainAxisAlignment.SpaceBetween)
        add(
          widgetFactory.text(
            "A",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.Start)),
          ),
        )
        add(
          widgetFactory.text(
            "B",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.End)),
          ),
        )
      },
    )

    parent.children.insert(
      1,
      flexContainer(FlexDirection.Column).apply {
        modifier = Modifier
          .then(GrowImpl(1.0))
          .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
          .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch))
        width(Constraint.Fill)
        mainAxisAlignment(MainAxisAlignment.SpaceBetween)
        add(
          widgetFactory.text(
            "C",
            GrowImpl(1.0)
              .then(CrossAxisAlignmentImpl(CrossAxisAlignment.Start)),
          ),
        )
        add(
          widgetFactory.text(
            "D",
            GrowImpl(1.0).then(CrossAxisAlignmentImpl(CrossAxisAlignment.End)),
          ),
        )
      },
    )

    snapshotter.snapshot("both")

    parent.children.remove(index = 1, count = 1)
    snapshotter.snapshot("single")
  }

  @Test fun testFlexDistributesWeightEqually() {
    val container = flexContainer(FlexDirection.Row)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widgetFactory.text("REALLY LONG TEXT", FlexImpl(1.0)))
    container.add(widgetFactory.text("SHORTER TEXT", FlexImpl(1.0)))
    container.add(widgetFactory.text("A", FlexImpl(1.0)))
    container.add(widgetFactory.text("LINE1\nLINE2\nLINE3", FlexImpl(1.0)))
    snapshotter(container.value).snapshot()
  }

  @Test fun testFlexDistributesWeightUnequally() {
    val container = flexContainer(FlexDirection.Row)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.add(widgetFactory.text("REALLY LONG TEXT", FlexImpl(3.0)))
    container.add(widgetFactory.text("SHORTER TEXT", FlexImpl(1.0)))
    container.add(widgetFactory.text("A", FlexImpl(1.0)))
    container.add(widgetFactory.text("LINE1\nLINE2\nLINE3", FlexImpl(1.0)))
    snapshotter(container.value).snapshot()
  }

  @Test fun testNestedColumnsWithFlex() {
    val outerContainer = flexContainer(FlexDirection.Column)
    outerContainer.width(Constraint.Fill)
    outerContainer.height(Constraint.Fill)
    outerContainer.crossAxisAlignment(CrossAxisAlignment.Center)

    val innerContainer1 = flexContainer(FlexDirection.Column)
    innerContainer1.width(Constraint.Fill)
    innerContainer1.crossAxisAlignment(CrossAxisAlignment.Center)
    innerContainer1.add(widgetFactory.text("INNER CONTAINER 1 TEXT 1"))
    innerContainer1.add(widgetFactory.text("INNER CONTAINER 1 TEXT 2"))

    val innerContainer2 = flexContainer(FlexDirection.Column)
    innerContainer2.width(Constraint.Fill)
    innerContainer2.crossAxisAlignment(CrossAxisAlignment.Center)
    innerContainer2.mainAxisAlignment(MainAxisAlignment.Center)
    innerContainer2.margin(Margin(bottom = 24.dp))
    innerContainer1.add(widgetFactory.text("INNER CONTAINER 2 TEXT 1"))
    innerContainer1.add(widgetFactory.text("INNER CONTAINER 2 TEXT 2"))

    outerContainer.add(innerContainer1)
    outerContainer.add(innerContainer2)
    innerContainer2.modifier = Modifier.then(FlexImpl(1.0))
    outerContainer.children.onModifierUpdated(1, innerContainer2)
    snapshotter(outerContainer.value).snapshot()
  }

  @Test fun testColumnWithChildModifierChanges() {
    testContainerWithChildrenModifierChanges(FlexDirection.Column)
  }

  @Test fun testRowWithChildModifierChanges() {
    testContainerWithChildrenModifierChanges(FlexDirection.Row)
  }

  private fun testContainerWithChildrenModifierChanges(
    flexDirection: FlexDirection,
  ) {
    val container = flexContainer(flexDirection)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)

    val first = widgetFactory.text(longText(), backgroundColor = Red)
    first.modifier = MarginImpl(30.dp)

    container.add(first)
    container.add(widgetFactory.text(mediumText(), backgroundColor = Green))
    container.add(widgetFactory.text(shortText(), backgroundColor = Blue))
    container.onEndChanges()
    snapshotter.snapshot("Margin")
    first.modifier = Modifier
    container.children.onModifierUpdated(0, first)
    container.onEndChanges()
    snapshotter.snapshot("Empty")
  }

  /** The view shouldn't crash if its displayed after being detached. */
  @Test fun testLayoutAfterDetach() {
    val container = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }
    val snapshotter = snapshotter(container.value)

    // Render before calling detach().
    container.children.insert(0, widgetFactory.text(mediumText(), MarginImpl(10.dp), Green))
    container.children.insert(1, widgetFactory.text(shortText(), MarginImpl(0.dp), Blue))
    container.onEndChanges()
    snapshotter.snapshot("Before")

    // Detach after changes are applied but before they're rendered.
    container.children.insert(0, widgetFactory.text(longText(), MarginImpl(20.dp), Red))
    container.onEndChanges()
    container.children.detach()
    snapshotter.snapshot("After")
  }

  @Test fun testOnScrollListener() {
    var scrolled = false
    val container = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      overflow(Overflow.Scroll)
      onScroll {
        scrolled = true
      }
    }

    container.scroll(Px(1000.0))

    snapshotter(container.value).snapshot()

    assertTrue(scrolled)
  }

  /**
   * Confirm that we don't perform unnecessary measurements of unchanged views.
   *
   * This creates a 3-element layout where each widgets' dimensions are independent. Then it
   * changes one of the widgets' and confirms that only that widget is measured.
   */
  @Test fun testLayoutIsIncremental() {
    val container = flexContainer(FlexDirection.Column)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)

    val a = widgetFactory.text("A")
      .apply { modifier = HeightImpl(100.dp) }
      .also { container.add(it) }
    val b = widgetFactory.text("B")
      .apply { modifier = HeightImpl(100.dp) }
      .also { container.add(it) }
    val c = widgetFactory.text("C")
      .apply { modifier = HeightImpl(100.dp) }
      .also { container.add(it) }
    container.onEndChanges()
    snapshotter.snapshot("v1")
    val aMeasureCountV1 = a.measureCount
    val bMeasureCountV1 = b.measureCount
    val cMeasureCountV1 = c.measureCount

    b.text("B v2")
    snapshotter.snapshot("v2")
    val aMeasureCountV2 = a.measureCount
    val bMeasureCountV2 = b.measureCount
    val cMeasureCountV2 = c.measureCount

    // Only 'b' is measured again.
    assertEquals(aMeasureCountV1, aMeasureCountV2)
    assertTrue(bMeasureCountV1 <= bMeasureCountV2)
    assertEquals(cMeasureCountV1, cMeasureCountV2)

    snapshotter.snapshot("v3")
    val aMeasureCountV3 = a.measureCount
    val bMeasureCountV3 = b.measureCount
    val cMeasureCountV3 = c.measureCount

    // Nothing is measured again.
    assertEquals(aMeasureCountV2, aMeasureCountV3)
    assertEquals(bMeasureCountV2, bMeasureCountV3)
    assertEquals(cMeasureCountV2, cMeasureCountV3)
  }

  @Test fun testRecursiveLayoutIsIncremental() {
    val container = flexContainer(FlexDirection.Column)
    val snapshotter = snapshotter(container.value)
    container.width(Constraint.Fill)
    container.height(Constraint.Fill)
    container.crossAxisAlignment(CrossAxisAlignment.Start)

    val rowA = row()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
      }
      .also { container.add(it) }
    val rowB = row()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
      }
      .also { container.add(it) }
    val rowC = row()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
      }
      .also { container.add(it) }
    val a = widgetFactory.text("A")
      .apply { modifier = HeightImpl(100.dp) }
      .also { rowA.children.insert(0, it) }
    val b = widgetFactory.text("B")
      .apply { modifier = HeightImpl(100.dp) }
      .also { rowB.children.insert(0, it) }
    val c = widgetFactory.text("C")
      .apply { modifier = HeightImpl(100.dp) }
      .also { rowC.children.insert(0, it) }
    container.onEndChanges()
    snapshotter.snapshot("v1")
    val aMeasureCountV1 = a.measureCount
    val bMeasureCountV1 = b.measureCount
    val cMeasureCountV1 = c.measureCount

    b.text("B v2")
    snapshotter.snapshot("v2")
    val aMeasureCountV2 = a.measureCount
    val bMeasureCountV2 = b.measureCount
    val cMeasureCountV2 = c.measureCount

    // Only 'b' is measured again.
    assertEquals(aMeasureCountV1, aMeasureCountV2)
    assertTrue(bMeasureCountV1 <= bMeasureCountV2)
    assertEquals(cMeasureCountV1, cMeasureCountV2)

    snapshotter.snapshot("v3")
    val aMeasureCountV3 = a.measureCount
    val bMeasureCountV3 = b.measureCount
    val cMeasureCountV3 = c.measureCount

    // Nothing is measured again.
    assertEquals(aMeasureCountV2, aMeasureCountV3)
    assertEquals(bMeasureCountV2, bMeasureCountV3)
    assertEquals(cMeasureCountV2, cMeasureCountV3)
  }

  /** Confirm that child element size changes propagate up the view hierarchy. */
  @Test fun testRecursiveLayoutHandlesResizes() {
    val column = flexContainer(FlexDirection.Column)
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        crossAxisAlignment(CrossAxisAlignment.Stretch)
      }
    val snapshotter = snapshotter(column.value)

    val rowA = row()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Wrap)
      }
      .also { column.add(it) }

    val rowA1 = widgetFactory.text(
      text = "A1 ".repeat(50),
      modifier = Modifier
        .then(FlexImpl(1.0))
        .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
        .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch)),
    ).also { rowA.children.insert(0, it) }
    val rowA2 = widgetFactory.text(
      text = "A-TWO ".repeat(50),
      modifier = Modifier
        .then(FlexImpl(1.0))
        .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
        .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch)),
    ).also { rowA.children.insert(1, it) }

    val rowB = widgetFactory.text("B1 ".repeat(5))
      .apply {
        modifier = Modifier
          .then(HorizontalAlignmentImpl(CrossAxisAlignment.Center))
      }
      .also { column.add(it) }
    column.onEndChanges()
    snapshotter.snapshot("v1")

    rowA1.text("A1 ".repeat(5))
    rowA2.text("A-TWO ".repeat(5))
    snapshotter.snapshot("v2")
  }

  /**
   * When a child widget's intrinsic size won't fit in the available space, what happens? We can
   * either let it have its requested size anyway (and overrun the available space) or we confine it
   * to the space available.
   */
  @Test fun testChildIsConstrainedToParentWidth() {
    // Wrap in a parent column to let us configure an exact width for our subject flex container.
    // Otherwise we're relying on the platform-specific snapshot library's unspecified frame width.
    val fullWidthParent = column().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }

    flexContainer(FlexDirection.Column)
      .apply {
        width(Constraint.Fill)
        modifier = WidthImpl(25.dp)
        add(widgetFactory.text("ok")) // This is under 25.dp in width.
        add(widgetFactory.text("1 2 3 4")) // Each character is under 25.dp in width.
        onEndChanges()
      }
      .also { fullWidthParent.children.insert(0, it) }

    flexContainer(FlexDirection.Column)
      .apply {
        width(Constraint.Fill)
        modifier = WidthImpl(25.dp)
        add(widgetFactory.text("overflows parent")) // This is over 25.dp in width.
        add(widgetFactory.text("1 2 3 4")) // Each character is under 25.dp in width.
        onEndChanges()
      }
      .also { fullWidthParent.children.insert(1, it) }

    snapshotter(fullWidthParent.value).snapshot()
  }

  /**
   * We were incorrectly collapsing the dimensions of the widget.
   * https://github.com/cashapp/redwood/issues/2018
   */
  @Test fun testWidgetWithFlexModifierNestedInRowAndColumn() {
    val root = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      margin(Margin(top = 24.dp))
      modifier = Modifier
    }

    val rootChild0 = row().apply {
      width(Constraint.Fill)
      horizontalAlignment(MainAxisAlignment.Center)
      root.children.insert(0, this)
    }

    val rootChild0Child0 = column().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      horizontalAlignment(CrossAxisAlignment.Stretch)
      modifier = Modifier
        .then(MarginImpl(Margin(start = 24.dp, end = 24.dp)))
        .then(FlexImpl(1.0))
      rootChild0.children.insert(0, this)
    }

    val rootChild0Child0Child0 = spacer().apply {
      width(48.dp)
      height(48.dp)
      rootChild0Child0.children.insert(0, this)
    }

    snapshotter(root.value).snapshot()
  }

  /**
   * CrossAxisAlignment.Start forces child to wrap its size on Android
   * https://github.com/cashapp/redwood/issues/2093
   */
  @Test fun testCrossAxisAlignmentStart() {
    val root = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      crossAxisAlignment(CrossAxisAlignment.Start)
    }

    val row = row().apply {
      width(Constraint.Fill)
      horizontalAlignment(MainAxisAlignment.SpaceBetween)
      root.add(this)
    }

    row.children.insert(0, widgetFactory.text("Something"))
    row.children.insert(1, widgetFactory.text("Something else"))

    snapshotter(root.value).snapshot()
  }

  /**
   * Text not wrapping inside a row.
   * https://github.com/cashapp/redwood/issues/2011
   */
  @Test fun testTextWrapsInsideRow() {
    val root = flexContainer(FlexDirection.Row)

    root.add(
      widgetFactory.text(
        "This is a long piece of text that will wrap the screen. ".repeat(3),
      ),
    )

    snapshotter(root.value).snapshot()
  }
}

interface TestFlexContainer<T : Any> :
  Widget<T>,
  ChangeListener {
  override val value: T
  val children: Widget.Children<T>
  fun width(width: Constraint)
  fun height(height: Constraint)
  fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment)
  fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment)
  fun margin(margin: Margin)
  fun overflow(overflow: Overflow)
  fun onScroll(onScroll: ((Px) -> Unit)?)
  fun scroll(offset: Px)
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
