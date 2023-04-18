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
import android.widget.Space
import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.api.Dp
import app.cash.redwood.layout.api.toPx
import app.cash.redwood.layout.widget.Spacer
import kotlin.math.roundToInt

internal class ViewSpacer(
  context: Context,
) : Spacer<View> {
  private val density = context.resources.displayMetrics.density.toDouble()

  override val value = Space(context)

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun width(width: Dp) {
    value.minimumWidth = width.toPx(density).roundToInt()
    invalidate()
  }

  override fun height(height: Dp) {
    value.minimumHeight = height.toPx(density).roundToInt()
    invalidate()
  }

  private fun invalidate() {
    value.invalidate()
    value.requestLayout()
  }
}
