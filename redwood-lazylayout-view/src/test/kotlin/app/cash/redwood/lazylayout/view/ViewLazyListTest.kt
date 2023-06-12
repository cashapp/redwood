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
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.JustifyContent
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ViewLazyListTest : AbstractFlexContainerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    showSystemUi = false,
  )

  override fun flexContainer(direction: FlexDirection) = ViewTestFlexContainer(paparazzi.context, direction)

  override fun widget(text: String, modifier: Modifier) = object : Widget<View> {
    override val value = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.GREEN)
      textSize = 18f
      setTextColor(Color.BLACK)
      this.text = text
    }
    override var modifier = modifier
  }

  override fun verifySnapshot(container: TestFlexContainer<View>) {
    paparazzi.snapshot(container.value)
  }

  class ViewTestFlexContainer(context: Context, direction: FlexDirection) : TestFlexContainer<View> {
    private val delegate = ViewLazyList(context)
      .apply { isVertical(direction == FlexDirection.Column) }
    private var childCount = 0

    override val value get() = delegate.value

    override fun width(constraint: Constraint) {
      delegate.width(constraint)
    }

    override fun height(constraint: Constraint) {
      delegate.width(constraint)
    }

    override fun alignItems(alignItems: AlignItems) {
    }

    override fun justifyContent(justifyContent: JustifyContent) {
    }

    override fun margin(margin: Margin) {
    }

    override fun add(widget: Widget<View>) {
      delegate.items.insert(childCount++, widget)
    }
  }
}
