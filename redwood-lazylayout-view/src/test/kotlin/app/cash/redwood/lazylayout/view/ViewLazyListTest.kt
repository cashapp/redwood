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
import android.graphics.Color
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import com.android.resources.LayoutDirection
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ViewLazyListTest(
  @TestParameter layoutDirection: LayoutDirection,
) : AbstractFlexContainerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): TestFlexContainer<View> {
    return ViewTestFlexContainer(paparazzi.context, direction, backgroundColor)
  }

  override fun row(): Row<View> {
    return ViewRedwoodLayoutWidgetFactory(paparazzi.context).Row()
  }

  override fun column(): Column<View> {
    return ViewRedwoodLayoutWidgetFactory(paparazzi.context).Column()
  }

  override fun widget(backgroundColor: Int) = object : Text<View> {
    override val value = TextView(paparazzi.context).apply {
      setBackgroundColor(backgroundColor)
      textSize = 18f
      setTextColor(Color.BLACK)
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
      value.text = text
    }
  }

  override fun verifySnapshot(container: Widget<View>, name: String?) {
    paparazzi.snapshot(container.value, name)
  }

  class ViewTestFlexContainer private constructor(
    private val delegate: ViewLazyList,
  ) : TestFlexContainer<View>, LazyList<View> by delegate, ChangeListener by delegate {
    private var childCount = 0

    constructor(context: Context, direction: FlexDirection, backgroundColor: Int) : this(
      ViewLazyList(context).apply {
        isVertical(direction == FlexDirection.Column)
        value.setBackgroundColor(backgroundColor)
      },
    )

    override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
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
