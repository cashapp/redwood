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
package app.cash.redwood.widget.view

import android.view.View
import androidx.activity.OnBackPressedDispatcher
import app.cash.burst.Burst
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.ViewSnapshotter
import app.cash.redwood.snapshot.testing.ViewTestWidgetFactory
import app.cash.redwood.widget.AbstractRedwoodViewTest
import app.cash.redwood.widget.RedwoodLayout
import app.cash.redwood.widget.Widget
import com.android.resources.LayoutDirection
import org.junit.Rule

@Burst
class ViewRedwoodViewTest(
  layoutDirection: LayoutDirection = LayoutDirection.LTR,
) : AbstractRedwoodViewTest<View, RedwoodLayout>() {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override val widgetFactory: TestWidgetFactory<View>
    get() = ViewTestWidgetFactory(paparazzi.context)

  override fun redwoodView() = RedwoodLayout(paparazzi.context, OnBackPressedDispatcher())

  override fun snapshotter(redwoodView: RedwoodLayout) = ViewSnapshotter(paparazzi, redwoodView)

  override fun snapshotter(widget: Widget<View>) = ViewSnapshotter(paparazzi, widget.value)
}
