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
package com.example.redwood.testing.android.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import app.cash.redwood.Modifier
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import com.example.redwood.testing.widget.Rectangle

internal class ViewRectangle(
  context: Context,
) : Rectangle<View> {

  private val density = Density(context.resources)

  val background = GradientDrawable()

  override val value = View(context)

  override var modifier: Modifier = Modifier
  init {
    value.background = background
  }
  override fun backgroundColor(backgroundColor: UInt) {
    background.setColor(backgroundColor.toInt())
  }

  override fun cornerRadius(cornerRadius: Float) {
    background.cornerRadius = with(density) { Dp(cornerRadius.toDouble()).toPx() }.toFloat()
  }
}
