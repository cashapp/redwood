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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.cash.burst.Burst
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.snapshot.testing.ComposeSnapshotter
import app.cash.redwood.snapshot.testing.ComposeUiTestWidgetFactory
import app.cash.redwood.ui.Px
import app.cash.redwood.yoga.FlexDirection
import com.android.resources.LayoutDirection
import kotlinx.coroutines.runBlocking
import org.junit.Rule

@Burst
class ComposeUiFlexContainerTest(
  layoutDirection: LayoutDirection = LayoutDirection.LTR,
) : AbstractFlexContainerTest<@Composable () -> Unit>() {

  override val widgetFactory = ComposeUiTestWidgetFactory

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): TestFlexContainer<@Composable () -> Unit> {
    return ComposeTestFlexContainer(direction, backgroundColor)
      .apply { (this as TestFlexContainer<*>).applyDefaults() }
  }

  override fun row(): Row<@Composable () -> Unit> = ComposeUiRow()
    .apply {
      container.testOnlyModifier = Modifier.background(Color(defaultBackgroundColor))
      applyDefaults()
    }

  override fun column(): Column<@Composable () -> Unit> = ComposeUiColumn()
    .apply {
      container.testOnlyModifier = Modifier.background(Color(defaultBackgroundColor))
      applyDefaults()
    }

  override fun spacer(backgroundColor: Int): Spacer<@Composable () -> Unit> {
    return ComposeUiSpacer().apply {
      testOnlyModifier = Modifier.background(Color(backgroundColor))
    }
  }

  override fun snapshotter(widget: @Composable () -> Unit) = ComposeSnapshotter(paparazzi, widget)

  class ComposeTestFlexContainer private constructor(
    private val delegate: ComposeUiFlexContainer,
  ) : TestFlexContainer<@Composable () -> Unit>,
    YogaFlexContainer<@Composable () -> Unit> by delegate {

    constructor(direction: FlexDirection, backgroundColor: Int) : this(
      ComposeUiFlexContainer(direction).apply {
        testOnlyModifier = Modifier.background(Color(backgroundColor))
      },
    )

    override val value get() = delegate.value
    override var modifier by delegate::modifier

    override val children get() = delegate.children

    override fun width(width: Constraint) = delegate.width(width)
    override fun height(height: Constraint) = delegate.height(height)
    override fun overflow(overflow: Overflow) = delegate.overflow(overflow)
    override fun onScroll(onScroll: ((Px) -> Unit)?) = delegate.onScroll(onScroll)

    override fun scroll(offset: Px) {
      runBlocking {
        delegate.scrollState?.scrollTo(offset.value.toInt())
      }
    }

    override fun onEndChanges() {
    }
  }
}
