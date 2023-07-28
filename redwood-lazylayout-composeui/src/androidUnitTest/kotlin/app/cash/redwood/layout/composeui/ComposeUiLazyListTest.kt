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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.composeui.ComposeUiLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.JustifyContent
import com.android.resources.LayoutDirection
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ComposeUiLazyListTest(
  @TestParameter layoutDirection: LayoutDirection,
) : AbstractFlexContainerTest<@Composable () -> Unit>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun flexContainer(direction: FlexDirection) = ComposeTestFlexContainer(direction)

  override fun widget(text: String, modifier: RedwoodModifier) = object : Text<@Composable () -> Unit> {
    private var text by mutableStateOf(text)

    override val value = @Composable {
      BasicText(
        text = this.text,
        style = TextStyle(fontSize = 18.sp, color = Color.Black),
        modifier = Modifier.background(Color.Green),
      )
    }

    override var modifier = modifier

    override fun text(text: String) {
      this.text = text
    }
  }

  override fun verifySnapshot(container: TestFlexContainer<@Composable () -> Unit>, name: String?) {
    paparazzi.snapshot(name) {
      container.value()
    }
  }

  class ComposeTestFlexContainer(direction: FlexDirection) : TestFlexContainer<@Composable () -> Unit> {
    private val delegate = ComposeUiLazyList().apply {
      isVertical(direction == FlexDirection.Column)
      testOnlyModifier = Modifier.background(Color.Blue)
    }
    private var childCount = 0

    override val value get() = delegate.value

    override fun width(constraint: Constraint) {
      delegate.width(constraint)
    }

    override fun height(constraint: Constraint) {
      delegate.height(constraint)
    }

    override fun alignItems(alignItems: AlignItems) {
      val crossAxisAlignment = when (alignItems) {
        AlignItems.FlexStart -> CrossAxisAlignment.Start
        AlignItems.Center -> CrossAxisAlignment.Center
        AlignItems.FlexEnd -> CrossAxisAlignment.End
        AlignItems.Stretch -> CrossAxisAlignment.Stretch
        else -> throw AssertionError()
      }
      delegate.crossAxisAlignment(crossAxisAlignment)
    }

    override fun justifyContent(justifyContent: JustifyContent) {
    }

    override fun margin(margin: Margin) {
      delegate.margin(margin)
    }

    override fun add(widget: Widget<@Composable () -> Unit>) {
      delegate.items.insert(childCount++, widget)
    }
  }
}
