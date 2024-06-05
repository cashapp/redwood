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

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import app.cash.redwood.Modifier
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import kotlin.math.min

internal class ViewSpacer(
  context: Context,
) : View(context),
  Spacer<View> {
  private val density = Density(context.resources)

  override val value get() = this

  override var modifier: Modifier = Modifier

  override fun width(width: Dp) {
    value.minimumWidth = with(density) { width.toPxInt() }
    value.requestLayout()
  }

  override fun height(height: Dp) {
    value.minimumHeight = with(density) { height.toPxInt() }
    value.requestLayout()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    setMeasuredDimension(
      getDefaultSizeSpace(suggestedMinimumWidth, widthMeasureSpec),
      getDefaultSizeSpace(suggestedMinimumHeight, heightMeasureSpec),
    )
  }

  /** Replicates the behavior of [android.widget.Space]. */
  private fun getDefaultSizeSpace(size: Int, measureSpec: Int): Int {
    val specMode = MeasureSpec.getMode(measureSpec)
    val specSize = MeasureSpec.getSize(measureSpec)
    return when (specMode) {
      AT_MOST -> min(size, specSize)
      EXACTLY -> specSize
      else -> size
    }
  }
}
