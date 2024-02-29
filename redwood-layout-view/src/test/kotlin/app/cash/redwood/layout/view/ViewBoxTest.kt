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
import android.widget.FrameLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.Color
import app.cash.redwood.layout.widget.Box
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ViewBoxTest : AbstractBoxTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
  )

  override fun Box(block: Box<View>.() -> Unit): Box<View> {
    val box = ViewBox(paparazzi.context).apply(block)
    box.background = ColorDrawable(0x88000000.toInt())
    return box
  }

  override fun Color(block: Color<View>.() -> Unit): Color<View> {
    return ViewColor(paparazzi.context).apply(block)
  }

  override fun verifySnapshot(value: View) {
    val container = object : FrameLayout(paparazzi.context) {
      override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Allow children to wrap.
        super.onMeasure(
          MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST),
          MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST),
        )
      }
    }
    container.addView(value)
    paparazzi.snapshot(container)
  }
}
