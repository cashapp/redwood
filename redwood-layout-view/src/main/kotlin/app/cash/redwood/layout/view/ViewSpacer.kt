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
import app.cash.redwood.Modifier
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import kotlin.math.roundToInt

internal class ViewSpacer(
  context: Context,
) : Spacer<View> {
  private val density = Density(context.resources)

  override val value = Space(context)

  override var modifier: Modifier = Modifier

  override fun width(width: Dp) {
    value.minimumWidth = with(density) { width.toPx() }.roundToInt()
    invalidate()
  }

  override fun height(height: Dp) {
    value.minimumHeight = with(density) { height.toPx() }.roundToInt()
    invalidate()
  }

  private fun invalidate() {
    value.invalidate()
    value.requestLayout()
  }
}
