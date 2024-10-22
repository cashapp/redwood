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
package app.cash.redwood.layout.view

import android.graphics.drawable.ColorDrawable
import android.view.View
import app.cash.burst.Burst
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.ViewSnapshotter
import app.cash.redwood.snapshot.testing.ViewTestWidgetFactory
import com.android.resources.LayoutDirection
import org.junit.Rule

@Burst
class ViewBoxTest(
  layoutDirection: LayoutDirection,
) : AbstractBoxTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override val widgetFactory: ViewTestWidgetFactory
    get() = ViewTestWidgetFactory(paparazzi.context)

  override fun box(): Box<View> {
    return ViewBox(paparazzi.context).apply {
      background = ColorDrawable(0x88000000.toInt())
      applyDefaults()
    }
  }

  override fun snapshotter(widget: View): Snapshotter = ViewSnapshotter(paparazzi, widget)
}
