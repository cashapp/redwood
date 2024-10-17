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
package app.cash.redwood.lazylayout.view

import android.content.Context
import android.view.View
import app.cash.burst.Burst
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.snapshot.testing.ViewSnapshotter
import app.cash.redwood.snapshot.testing.ViewTestWidgetFactory
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import com.android.resources.LayoutDirection
import org.junit.Rule

@Burst
class ViewLazyListAsFlexContainerTest(
  layoutDirection: LayoutDirection,
) : AbstractFlexContainerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override val widgetFactory: ViewTestWidgetFactory
    get() = ViewTestWidgetFactory(paparazzi.context)

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): TestFlexContainer<View> {
    return ViewTestFlexContainer(paparazzi.context, direction, backgroundColor)
      .apply { applyDefaults() }
  }

  override fun row(): Row<View> {
    return ViewRedwoodLayoutWidgetFactory(paparazzi.context).Row()
  }

  override fun column(): Column<View> {
    return ViewRedwoodLayoutWidgetFactory(paparazzi.context).Column()
  }

  override fun spacer(backgroundColor: Int): Spacer<View> {
    return ViewRedwoodLayoutWidgetFactory(paparazzi.context).Spacer()
      .apply {
        value.setBackgroundColor(backgroundColor)
      }
  }

  override fun snapshotter(widget: View) = ViewSnapshotter(paparazzi, widget)

  class ViewTestFlexContainer private constructor(
    private val delegate: ViewLazyList,
  ) : TestFlexContainer<View>,
    LazyList<View> by delegate,
    ChangeListener by delegate {
    private var childCount = 0
    private var onScroll: ((Px) -> Unit)? = null

    constructor(context: Context, direction: FlexDirection, backgroundColor: Int) : this(
      ViewLazyList(context).apply {
        isVertical(direction == FlexDirection.Column)
        value.setBackgroundColor(backgroundColor)
      },
    )

    override val children: Widget.Children<View> = delegate.items

    override fun onScroll(onScroll: ((Px) -> Unit)?) {
      this.onScroll = onScroll
    }

    override fun scroll(offset: Px) {
      onScroll?.invoke(offset)
    }

    override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    }

    override fun overflow(overflow: Overflow) {
    }

    override fun add(widget: Widget<View>) {
      addAt(childCount, widget)
    }

    override fun addAt(index: Int, widget: Widget<View>) {
      delegate.items.insert(index, widget)
      childCount++
    }

    override fun removeAt(index: Int) {
      delegate.items.remove(index = index, count = 1)
      childCount--
    }
  }
}
