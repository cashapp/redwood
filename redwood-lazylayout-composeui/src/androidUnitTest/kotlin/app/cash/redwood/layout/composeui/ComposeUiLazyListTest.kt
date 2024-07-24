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
import app.cash.redwood.layout.Transparent
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.lazylayout.composeui.ComposeUiLazyList
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import app.cash.redwood.yoga.FlexDirection
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

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): ComposeTestFlexContainer {
    return ComposeTestFlexContainer(direction, backgroundColor)
  }

  override fun row(): Row<@Composable () -> Unit> {
    return ComposeUiRedwoodLayoutWidgetFactory().Row()
  }

  override fun column(): Column<@Composable () -> Unit> {
    return ComposeUiRedwoodLayoutWidgetFactory().Column()
  }

  override fun text() = object : Text<@Composable () -> Unit> {
    private var text by mutableStateOf("")
    private var bgColor by mutableStateOf(Transparent)

    override val value = @Composable {
      BasicText(
        text = this.text,
        style = TextStyle(fontSize = 18.sp, color = Color.Black),
        modifier = Modifier.background(Color(bgColor)),
      )
    }

    override var modifier: RedwoodModifier = RedwoodModifier

    override fun text(text: String) {
      this.text = text
    }

    override fun bgColor(color: Int) {
      bgColor = color
    }
  }

  override fun verifySnapshot(container: Widget<@Composable () -> Unit>, name: String?) {
    paparazzi.snapshot(name) {
      container.value()
    }
  }

  class ComposeTestFlexContainer private constructor(
    private val delegate: ComposeUiLazyList,
  ) : TestFlexContainer<@Composable () -> Unit>,
    LazyList<@Composable () -> Unit> by delegate {

    // Work around https://youtrack.jetbrains.com/issue/KT-68850
    override val value: @Composable () -> Unit get() = delegate.value

    private var childCount = 0
    private var onScroll: ((Px) -> Unit)? = null

    constructor(direction: FlexDirection, backgroundColor: Int) : this(
      ComposeUiLazyList().apply {
        isVertical(direction == FlexDirection.Column)
        testOnlyModifier = Modifier.background(Color(backgroundColor))
      },
    )

    override val children: ComposeWidgetChildren = delegate.items

    override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    }

    override fun onScroll(onScroll: ((Px) -> Unit)?) {
      this.onScroll = onScroll
    }

    override fun scroll(offset: Px) {
      onScroll?.invoke(offset)
    }

    override fun overflow(overflow: Overflow) {
    }

    override fun add(widget: Widget<@Composable () -> Unit>) {
      addAt(childCount, widget)
    }

    override fun addAt(index: Int, widget: Widget<@Composable () -> Unit>) {
      delegate.items.insert(index, widget)
      childCount++
    }

    override fun removeAt(index: Int) {
      delegate.items.remove(index = index, count = 1)
      childCount--
    }

    override fun onEndChanges() {
    }
  }
}
