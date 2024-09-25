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
package app.cash.redwood.lazylayout.view

import android.graphics.Color
import android.view.View
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier
import app.cash.redwood.lazylayout.AbstractLazyListTest
import app.cash.redwood.lazylayout.Text
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.ViewSnapshotter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ViewLazyListTest : AbstractLazyListTest<View>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun text(): Text<View> {
    return object : Text<View> {
      override val value = TextView(paparazzi.context).apply {
        textSize = 18f
        setTextColor(Color.BLACK)
      }

      override var modifier: Modifier = Modifier

      override fun text(text: String) {
        value.text = text
      }

      override fun bgColor(color: Int) {
        value.setBackgroundColor(color)
      }
    }
  }

  override fun lazyList(backgroundColor: Int): LazyList<View> {
    return ViewLazyList(paparazzi.context).apply {
      value.setBackgroundColor(backgroundColor)
    }
  }

  override fun snapshotter(widget: View): Snapshotter = ViewSnapshotter(paparazzi, widget)
}
