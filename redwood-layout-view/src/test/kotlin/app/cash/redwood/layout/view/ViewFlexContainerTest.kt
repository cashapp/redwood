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
package app.cash.redwood.layout.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import com.android.resources.LayoutDirection
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ViewFlexContainerTest(
  @TestParameter layoutDirection: LayoutDirection,
) : AbstractFlexContainerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun flexContainer(direction: FlexDirection): TestFlexContainer<View> {
    return ViewTestFlexContainer(paparazzi.context, direction)
  }

  override fun widget() = object : Text<View> {
    override val value = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.GREEN)
      textSize = 18f
      textDirection = View.TEXT_DIRECTION_LOCALE
      setTextColor(Color.BLACK)
    }

    override var modifier: Modifier = Modifier

    override fun text(text: String) {
      value.text = text
    }
  }

  override fun verifySnapshot(container: TestFlexContainer<View>, name: String?) {
    paparazzi.snapshot(container.value, name)
  }

  class ViewTestFlexContainer private constructor(
    private val delegate: ViewFlexContainer,
  ) : TestFlexContainer<View>, FlexContainer<View> by delegate, ChangeListener by delegate {
    private var childCount = 0

    constructor(context: Context, direction: FlexDirection) : this(
      ViewFlexContainer(context, direction).apply {
        value.setBackgroundColor(Color.argb(51, 0, 0, 255))
      },
    )

    override fun add(widget: Widget<View>) {
      addAt(childCount, widget)
    }

    override fun addAt(index: Int, widget: Widget<View>) {
      delegate.children.insert(index, widget)
      childCount++
    }

    override fun removeAt(index: Int) {
      delegate.children.remove(index = index, count = 1)
      childCount--
    }
  }
}
