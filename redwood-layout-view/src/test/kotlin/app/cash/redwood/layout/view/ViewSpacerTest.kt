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
package app.cash.redwood.layout.view

import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.AbstractSpacerTest
import app.cash.redwood.layout.api.dp
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.widget.Widget
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ViewSpacerTest : AbstractSpacerTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    renderingMode = SessionParams.RenderingMode.SHRINK,
    showSystemUi = false,
  )

  override fun widget(
    width: Int,
    height: Int,
    layoutModifier: LayoutModifier,
  ): Spacer<View> = ViewSpacer(paparazzi.context).apply {
    layoutModifiers = layoutModifier
    width(width.dp)
    height(height.dp)
  }

  override fun wrap(widget: Widget<View>, horizontal: Boolean): View {
    return LinearLayout(paparazzi.context).apply {
      orientation = if (horizontal) HORIZONTAL else VERTICAL
      addView(TextView(paparazzi.context).apply { text = "Text 1" })
      addView(widget.value)
      addView(TextView(paparazzi.context).apply { text = "Text 2" })
    }
  }

  override fun verifySnapshot(value: View) {
    paparazzi.snapshot(value)
  }
}
