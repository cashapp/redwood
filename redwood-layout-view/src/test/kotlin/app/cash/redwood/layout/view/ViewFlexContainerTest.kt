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
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.Color
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.layout.toInt
import app.cash.redwood.widget.Widget
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ViewFlexContainerTest : AbstractFlexContainerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
  )

  override fun flexContainer(direction: FlexDirection) = ViewTestFlexContainer(paparazzi.context, direction)

  override fun widget(text: String, layoutModifier: LayoutModifier) = object : Widget<View> {
    override val value = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.Green.toInt())
      textSize = 18f
      setTextColor(Color.Black.toInt())
      this.text = text
    }
    override var layoutModifiers = layoutModifier
  }

  override fun verifySnapshot(container: TestFlexContainer<View>) {
    paparazzi.snapshot(container.value)
  }

  class ViewTestFlexContainer(context: Context, direction: FlexDirection) : TestFlexContainer<View> {
    private val delegate = ViewFlexContainer(context, direction)
    private var childCount = 0

    override val value get() = delegate.value

    override fun width(constraint: Constraint) {
      delegate.width(constraint)
    }

    override fun height(constraint: Constraint) {
      delegate.width(constraint)
    }

    override fun alignItems(alignItems: AlignItems) {
      delegate.alignItems(alignItems)
    }

    override fun justifyContent(justifyContent: JustifyContent) {
      delegate.justifyContent(justifyContent)
    }

    override fun margin(margin: Margin) {
      delegate.margin(margin)
    }

    override fun background(color: Color) {
      value.background = ColorDrawable(color.toInt())
    }

    override fun add(widget: Widget<View>) {
      delegate.children.insert(childCount++, widget)
    }
  }
}
