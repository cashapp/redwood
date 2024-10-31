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
package app.cash.redwood.widget

import app.cash.redwood.snapshot.testing.Blue
import app.cash.redwood.snapshot.testing.Green
import app.cash.redwood.snapshot.testing.Red
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.color
import app.cash.redwood.snapshot.testing.text
import app.cash.redwood.ui.dp
import kotlin.test.Test

abstract class AbstractRedwoodViewTest<W : Any, R : RedwoodView<W>> {

  abstract val widgetFactory: TestWidgetFactory<W>

  abstract fun redwoodView(): R

  abstract fun snapshotter(redwoodView: R): Snapshotter

  abstract fun snapshotter(widget: Widget<W>): Snapshotter

  /**
   * This test uses a string that wraps to confirm the root view's dimensions aren't unbounded.
   * https://github.com/cashapp/redwood/issues/2128
   */
  @Test
  fun testSingleChildElement() {
    val redwoodView = redwoodView()
    redwoodView.children.insert(0, widgetFactory.text("Hello ".repeat(50)))
    snapshotter(redwoodView).snapshot()
  }

  /** RedwoodView is measured by its content. */
  @Test
  fun testWrapped() {
    // Green, Blue, Green.
    val rootColumn = widgetFactory.column()
    rootColumn.add(widgetFactory.color(Green, 100.dp, 100.dp).value)
    rootColumn.add(
      redwoodView().apply {
        children.insert(0, widgetFactory.color(Blue, 100.dp, 100.dp))
      }.value,
    )
    rootColumn.add(widgetFactory.color(Green, 100.dp, 100.dp).value)

    val scrollWrapper = widgetFactory.scrollWrapper()
    scrollWrapper.content = rootColumn.value

    snapshotter(scrollWrapper).snapshot(scrolling = true)
  }

  /** RedwoodView's height can exceed the height of the screen. */
  @Test
  fun testExceedsScreenSize() {
    // Green, Blue, Red, Blue, Red, ... Blue, Red, Green.
    val rootColumn = widgetFactory.column()
    rootColumn.add(widgetFactory.color(Green, 100.dp, 100.dp).value)
    rootColumn.add(
      redwoodView().apply {
        val column = widgetFactory.column()
        for (i in 0 until 10) {
          column.add(widgetFactory.color(Blue, 100.dp, 100.dp).value)
          column.add(widgetFactory.color(Red, 100.dp, 100.dp).value)
        }
        children.insert(0, column)
      }.value,
    )
    rootColumn.add(widgetFactory.color(Green, 100.dp, 100.dp).value)

    val scrollWrapper = widgetFactory.scrollWrapper()
    scrollWrapper.content = rootColumn.value

    snapshotter(scrollWrapper).snapshot(scrolling = true)
  }
}
