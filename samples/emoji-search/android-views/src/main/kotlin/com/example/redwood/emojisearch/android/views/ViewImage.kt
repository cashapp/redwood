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
package com.example.redwood.emojisearch.android.views

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.cash.redwood.Modifier
import coil3.load
import com.example.redwood.emojisearch.widget.Image

internal class ViewImage(
  override val value: ImageView,
) : Image<View> {
  init {
    val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48F, value.resources.displayMetrics).toInt()
    value.layoutParams = ViewGroup.LayoutParams(size, size)
  }

  override var modifier: Modifier = Modifier

  override fun url(url: String) {
    value.load(url)
  }

  override fun onClick(onClick: (() -> Unit)?) {
    if (onClick != null) {
      value.setOnClickListener { onClick() }
    } else {
      value.setOnClickListener(null)
    }
  }
}
