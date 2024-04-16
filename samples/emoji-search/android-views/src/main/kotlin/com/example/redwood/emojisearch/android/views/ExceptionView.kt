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
package com.example.redwood.emojisearch.android.views

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import app.cash.redwood.treehouse.TreehouseLayout
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.dp

/**
 * Renders an emoji, plus the first line of the exception message, centered and wrapped. The view
 * has a light-yellow background.
 *
 * ```
 *                         🦨
 *          app.cash.zipline.ZiplineException
 *                  RuntimeException
 *                        boom!
 * ```
 */
@SuppressLint("ViewConstructor")
internal class ExceptionView(
  parent: TreehouseLayout,
  private val exception: Throwable,
) : LinearLayout(parent.context) {

  init {
    orientation = VERTICAL
    gravity = CENTER
    setBackgroundColor(Color.argb(255, 255, 250, 225))

    addView(
      AppCompatTextView(context).apply {
        textAlignment = TEXT_ALIGNMENT_CENTER
        setTextColor(Color.BLACK)
        textSize = 40f
        text = "🦨"
      },
    )

    addView(
      AppCompatTextView(context).apply {
        textAlignment = TEXT_ALIGNMENT_CENTER
        setTextColor(Color.BLACK)
        textSize = 16f
        text = exception.toString().substringBefore("\n").replace(": ", "\n")
        ellipsize = TextUtils.TruncateAt.END
      },
    )
  }

  override fun generateDefaultLayoutParams(): LayoutParams {
    return LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
      with(Density(resources)) {
        setMargins(
          10f.dp.toPxInt(),
          5f.dp.toPxInt(),
          10f.dp.toPxInt(),
          5f.dp.toPxInt(),
        )
      }
    }
  }
}
