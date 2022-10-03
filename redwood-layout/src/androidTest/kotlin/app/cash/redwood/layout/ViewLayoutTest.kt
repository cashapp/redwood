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

package app.cash.redwood.layout

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

@SuppressLint("SetTextI18n")
class ViewLayoutTest {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar"
  )

  @Test
  fun column() {
    val column = ViewColumn(paparazzi.context).apply {
      horizontalAlignment(CrossAxisAlignment.Center)
      verticalAlignment(MainAxisAlignment.Center)
      value.background = ColorDrawable(Color.RED)
    }

    var textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 1"
    }
    column.children.insert(0, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 2"
    }
    column.children.insert(1, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 3"
    }
    column.children.insert(2, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 4"
    }
    column.children.insert(3, textView)

    paparazzi.snapshot(column.value)
  }

  @Test
  fun row() {
    val row = ViewRow(paparazzi.context).apply {
      horizontalAlignment(MainAxisAlignment.Center)
      verticalAlignment(CrossAxisAlignment.Center)
      value.background = ColorDrawable(Color.RED)
    }

    var textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 1"
    }
    row.children.insert(0, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 2"
    }
    row.children.insert(1, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 3"
    }
    row.children.insert(2, textView)
    textView = TextView(paparazzi.context).apply {
      background = ColorDrawable(Color.BLUE)
      textSize = 18f
      text = "TextView 4"
    }
    row.children.insert(3, textView)

    paparazzi.snapshot(row.value)
  }
}
