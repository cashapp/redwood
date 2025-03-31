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

import app.cash.burst.Burst
import app.cash.burst.burstValues
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.snapshot.testing.Blue
import app.cash.redwood.snapshot.testing.Green
import app.cash.redwood.snapshot.testing.Red
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.argb
import app.cash.redwood.snapshot.testing.color
import app.cash.redwood.snapshot.testing.text
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import kotlin.test.Test

@Burst
abstract class AbstractBoxTest<T : Any> {

  abstract val widgetFactory: TestWidgetFactory<T>

  abstract fun box(): Box<T>

  /**
   * Explicitly apply defaults to our Box instance. This is only necessary in tests; in production
   * the framework explicitly sets every property.
   */
  protected fun Box<T>.applyDefaults() {
    width(Constraint.Wrap)
    height(Constraint.Wrap)
    margin(Margin.Zero)
    horizontalAlignment(CrossAxisAlignment.Start)
    verticalAlignment(CrossAxisAlignment.Start)
  }

  abstract fun snapshotter(widget: T): Snapshotter

  @Test
  fun testEmpty_Defaults() {
    val widget = box()
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testEmpty_Wrap() {
    val widget = box().apply {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testEmpty_Fill() {
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testChildren(
    constraint: Constraint = burstValues(
      Constraint.Fill,
      Constraint.Wrap,
    ),
    horizontalAlignment: CrossAxisAlignment = burstValues(
      CrossAxisAlignment.Start,
      CrossAxisAlignment.Center,
      CrossAxisAlignment.End,
      CrossAxisAlignment.Stretch,
    ),
    verticalAlignment: CrossAxisAlignment = burstValues(
      CrossAxisAlignment.Start,
      CrossAxisAlignment.Center,
      CrossAxisAlignment.End,
      CrossAxisAlignment.Stretch,
    ),
  ) {
    val widget = box().apply {
      width(constraint)
      height(constraint)
      horizontalAlignment(horizontalAlignment)
      verticalAlignment(verticalAlignment)
      children.insert(
        0,
        widgetFactory.text(
          text = longText(),
          backgroundColor = Red,
        ),
      )
      children.insert(
        1,
        widgetFactory.text(
          text = mediumText(),
          backgroundColor = Green,
        ),
      )
      children.insert(
        2,
        widgetFactory.text(
          text = shortText(),
          backgroundColor = Blue,
        ),
      )
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testMargins() {
    // Different margins allow us to know which direction start and end get applied.
    val asymmetric = Margin(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp)

    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)

      // Ensure Box applies its margins correctly to the parent.
      margin(asymmetric)

      children.insert(
        0,
        // Ensure Box honors margins correctly from its children.
        widgetFactory.color(
          modifier = MarginImpl(asymmetric),
          color = Red,
          width = 100.dp,
          height = 100.dp,
        ),
      )
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testBoxMeasurementIncludesMargins() {
    val container = widgetFactory.column()
    container.add(
      box().apply {
        children.insert(0, widgetFactory.text("box 1"))
      }.value,
    )

    container.add(
      box().apply {
        horizontalAlignment(CrossAxisAlignment.End)
        margin(Margin(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp))
        children.insert(0, widgetFactory.text("box 2"))
      }.value,
    )

    container.add(
      box().apply {
        horizontalAlignment(CrossAxisAlignment.Center)
        children.insert(0, widgetFactory.text("box 3"))
      }.value,
    )

    container.add(
      box().apply {
        margin(Margin(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp))
        children.insert(0, widgetFactory.text("box 4"))
      }.value,
    )

    container.add(
      box().apply {
        horizontalAlignment(CrossAxisAlignment.End)
        children.insert(0, widgetFactory.text("box 5"))
      }.value,
    )

    val scrollWrapper = widgetFactory.scrollWrapper()
    scrollWrapper.content = container.value
    snapshotter(scrollWrapper.value).snapshot()
  }

  @Test
  fun testMarginsAndAlignment() {
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)

      children.insert(
        0,
        widgetFactory.text(
          text = "start x, end y",
          backgroundColor = Red,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 20.dp, top = 10.dp, end = 40.dp, bottom = 30.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.Start))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.End)),
        ),
      )
      children.insert(
        1,
        widgetFactory.text(
          text = "end x, start y",
          backgroundColor = Green,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 40.dp, top = 30.dp, end = 20.dp, bottom = 10.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.End))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.Start)),
        ),
      )
      children.insert(
        2,
        widgetFactory.text(
          text = "center both",
          backgroundColor = Blue,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 10.dp, top = 50.dp, end = 50.dp, bottom = 10.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.Center))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.Center)),
        ),
      )
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testMarginsAndStretch() {
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)

      children.insert(
        0,
        widgetFactory.text(
          text = "stretch both",
          backgroundColor = Red,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 20.dp, top = 10.dp, end = 40.dp, bottom = 30.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch)),
        ),
      )
      children.insert(
        1,
        widgetFactory.text(
          text = "end x, stretch y",
          backgroundColor = Green,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 40.dp, top = 30.dp, end = 20.dp, bottom = 10.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.End))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch)),
        ),
      )
      children.insert(
        2,
        widgetFactory.text(
          text = "stretch x, end y",
          backgroundColor = Blue,
          modifier = Modifier
            .then(MarginImpl(Margin(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp)))
            .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
            .then(VerticalAlignmentImpl(CrossAxisAlignment.End)),
        ),
      )
    }
    snapshotter(widget.value).snapshot()
  }

  @Test
  fun testChildrenModifierChanges() {
    val redColor = widgetFactory.text(
      modifier = MarginImpl(30.dp),
      text = longText(),
      backgroundColor = Red,
    )
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      children.insert(0, redColor)
      children.insert(
        1,
        widgetFactory.text(
          text = mediumText(),
          backgroundColor = Blue,
        ),
      )
      children.insert(
        2,
        widgetFactory.text(
          text = shortText(),
          backgroundColor = Green,
        ),
      )
    }
    val snapshotter = snapshotter(widget.value)
    snapshotter.snapshot("Margin")
    redColor.modifier = Modifier
    widget.children.onModifierUpdated(0, redColor)
    snapshotter.snapshot("Empty")
  }

  /** The view shouldn't crash if its displayed after being detached. */
  @Test
  fun testLayoutAfterDetach() {
    val widget = box().apply {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
      horizontalAlignment(CrossAxisAlignment.Start)
      verticalAlignment(CrossAxisAlignment.Start)
    }
    val snapshotter = snapshotter(widget.value)

    // Render before calling detach().
    widget.children.insert(
      0,
      widgetFactory.text(
        text = mediumText(),
        backgroundColor = Green,
        modifier = MarginImpl(10.dp),
      ),
    )
    widget.children.insert(
      1,
      widgetFactory.text(
        text = shortText(),
        backgroundColor = Blue,
        modifier = MarginImpl(0.dp),
      ),
    )
    snapshotter.snapshot("Before")

    // Detach after changes are applied but before they're rendered.
    widget.children.insert(
      0,
      widgetFactory.text(
        text = longText(),
        backgroundColor = Red,
        modifier = MarginImpl(20.dp),
      ),
    )
    widget.children.detach()
    snapshotter.snapshot("After")
  }

  @Test
  fun testDynamicWidgetResizing() {
    val container = box()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Start)
        verticalAlignment(CrossAxisAlignment.Start)
      }
    val snapshotter = snapshotter(container.value)

    val a = widgetFactory.text(
      modifier = HorizontalAlignmentImpl(CrossAxisAlignment.Start),
      text = "AAA",
      backgroundColor = Red,
    ).also { container.children.insert(0, it) }
    val b = widgetFactory.text(
      modifier = HorizontalAlignmentImpl(CrossAxisAlignment.Center),
      text = "BBB",
      backgroundColor = Blue,
    ).also { container.children.insert(1, it) }
    val c = widgetFactory.text(
      modifier = HorizontalAlignmentImpl(CrossAxisAlignment.End),
      text = "CCC",
      backgroundColor = Green,
    ).also { container.children.insert(2, it) }
    snapshotter.snapshot("v1")

    b.text("BBB_v2")
    snapshotter.snapshot("v2")
  }

  @Test
  fun testChildExplicitHeight() {
    val container = box()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Stretch)
        verticalAlignment(CrossAxisAlignment.Center)
      }
    val snapshotter = snapshotter(container.value)

    val box = box()
      .apply {
        modifier = MarginImpl(all = 24.dp)
        width(Constraint.Fill)
        height(Constraint.Wrap)
        horizontalAlignment(CrossAxisAlignment.Stretch)
        verticalAlignment(CrossAxisAlignment.Center)
      }
      .also { container.children.insert(0, it) }

    val a = widgetFactory.color().apply {
      modifier = HeightImpl(100.dp)
    }.also { box.children.insert(0, it) }

    val b = widgetFactory.text(
      text = "foreground",
    ).also { box.children.insert(1, it) }

    snapshotter.snapshot()
  }

  @Test
  fun testChildExplicitWidth() {
    val container = box()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Center)
        verticalAlignment(CrossAxisAlignment.Stretch)
      }
    val snapshotter = snapshotter(container.value)

    val box = box()
      .apply {
        modifier = MarginImpl(all = 24.dp)
        width(Constraint.Wrap)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Center)
        verticalAlignment(CrossAxisAlignment.Stretch)
      }
      .also { container.children.insert(0, it) }

    widgetFactory.color().apply {
      modifier = WidthImpl(200.dp)
    }.also { box.children.insert(0, it) }

    widgetFactory.text(
      text = "foreground",
    ).also { box.children.insert(1, it) }

    snapshotter.snapshot()
  }

  /** We had a bug where stretch alignment impacted measurement. It shouldn't. */
  @Test
  fun testStretchDoesNotImpactMeasurement() {
    val container = box()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Start)
        verticalAlignment(CrossAxisAlignment.Start)
      }
    val snapshotter = snapshotter(container.value)

    val box = box()
      .apply {
        width(Constraint.Wrap)
        height(Constraint.Wrap)
        horizontalAlignment(CrossAxisAlignment.Stretch)
        verticalAlignment(CrossAxisAlignment.Stretch)
        modifier = MarginImpl(Margin(all = 5.dp))
      }
      .also { container.children.insert(0, it) }

    widgetFactory.text(
      text = "This text is in a green box",
      backgroundColor = argb(0, 0, 0, 0),
    ).apply {
      modifier = MarginImpl(Margin(all = 20.dp))
    }.also { box.children.insert(0, it) }

    widgetFactory.color().apply {
      color(argb(51, 0, 255, 0))
      modifier = Modifier
        .then(VerticalAlignmentImpl(CrossAxisAlignment.Stretch))
        .then(HorizontalAlignmentImpl(CrossAxisAlignment.Stretch))
        .then(MarginImpl(Margin(all = 5.dp)))
    }.also { box.children.insert(1, it) }

    snapshotter.snapshot()
  }
}
