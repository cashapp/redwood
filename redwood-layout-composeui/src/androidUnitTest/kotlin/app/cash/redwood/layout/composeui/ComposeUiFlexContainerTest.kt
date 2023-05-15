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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.widget.Widget
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComposeUiFlexContainerTest : AbstractFlexContainerTest<@Composable () -> Unit>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    showSystemUi = false,
  )

  override fun flexContainer(direction: FlexDirection) = ComposeTestFlexContainer(direction)

  override fun widget(text: String, layoutModifier: LayoutModifier) = object : Widget<@Composable () -> Unit> {
    override val value = @Composable {
      BasicText(
        text = text,
        style = TextStyle(fontSize = 18.sp, color = Color.Black),
        modifier = Modifier.background(Color.Green),
      )
    }
    override var layoutModifiers = layoutModifier
  }

  override fun verifySnapshot(container: TestFlexContainer<@Composable () -> Unit>) {
    paparazzi.snapshot {
      container.value()
    }
  }

  class ComposeTestFlexContainer(direction: FlexDirection) : TestFlexContainer<@Composable () -> Unit> {
    private val delegate = ComposeUiFlexContainer(direction)
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

    override fun add(widget: Widget<@Composable () -> Unit>) {
      delegate.children.insert(childCount++, widget)
    }
  }
}
