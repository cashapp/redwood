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
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import org.junit.Rule
import org.junit.Test

@SuppressLint("SetTextI18n")
class ViewFlexContainerTest {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
  )

  private val textViews by lazy {
    listOf(
      TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.CYAN)
        textSize = 18f
        text = "TextView 1"
      },
      TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.CYAN)
        textSize = 18f
        text = "TextView 2"
      },
      TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.CYAN)
        textSize = 18f
        text = "TextView 3"
      },
      TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.CYAN)
        textSize = 18f
        text = "TextView 4"
      },
    )
  }

  @Test
  fun columnStart() {
    val column = ViewColumn(paparazzi.context).apply {
      horizontalAlignment(CrossAxisAlignment.Center)
      verticalAlignment(MainAxisAlignment.Start)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      column.children.insert(index, textView)
    }
    paparazzi.snapshot(column.value)
  }

  @Test
  fun columnCenter() {
    val column = ViewColumn(paparazzi.context).apply {
      horizontalAlignment(CrossAxisAlignment.Center)
      verticalAlignment(MainAxisAlignment.Center)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      column.children.insert(index, textView)
    }
    paparazzi.snapshot(column.value)
  }

  @Test
  fun columnEnd() {
    val column = ViewColumn(paparazzi.context).apply {
      horizontalAlignment(CrossAxisAlignment.Center)
      verticalAlignment(MainAxisAlignment.End)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      column.children.insert(index, textView)
    }
    paparazzi.snapshot(column.value)
  }

  @Test
  fun rowStart() {
    val row = ViewRow(paparazzi.context).apply {
      horizontalAlignment(MainAxisAlignment.Start)
      verticalAlignment(CrossAxisAlignment.Center)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      row.children.insert(index, textView)
    }
    paparazzi.snapshot(row.value)
  }

  @Test
  fun rowCenter() {
    val row = ViewRow(paparazzi.context).apply {
      horizontalAlignment(MainAxisAlignment.Center)
      verticalAlignment(CrossAxisAlignment.Center)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      row.children.insert(index, textView)
    }
    paparazzi.snapshot(row.value)
  }

  @Test
  fun rowEnd() {
    val row = ViewRow(paparazzi.context).apply {
      horizontalAlignment(MainAxisAlignment.End)
      verticalAlignment(CrossAxisAlignment.Center)
      value.background = ColorDrawable(Color.LTGRAY)
    }

    textViews.forEachIndexed { index, textView ->
      row.children.insert(index, textView)
    }
    paparazzi.snapshot(row.value)
  }
}
