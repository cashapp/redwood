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

import app.cash.redwood.layout.modifier.Margin as MarginModifier
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ViewGroupChildren

internal class ViewBox(
  context: Context,
) : FrameLayout(context), Box<View> {
  override var modifier: Modifier = Modifier

  override val value = this

  override val children = ViewGroupChildren(this)

  private val density = Density(context.resources)

  private var defaultHorizontalAlignment = CrossAxisAlignment.Start
  private var defaultVerticalAlignment = CrossAxisAlignment.Start
  private var width = Constraint.Fill
  private var height = Constraint.Fill

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
  }

  override fun width(width: Constraint) {
    this.width = width
  }

  override fun height(height: Constraint) {
    this.height = height
  }

  override fun margin(margin: Margin) {
    updateLayoutParams {
      val layoutParams = this as MarginLayoutParams
      with(density) {
        layoutParams.setMargins(
          margin.start.toPxInt(),
          margin.top.toPxInt(),
          margin.end.toPxInt(),
          margin.bottom.toPxInt(),
        )
      }
    }
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    this.defaultHorizontalAlignment = horizontalAlignment
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    this.defaultVerticalAlignment = verticalAlignment
  }

  /** Flush Redwood's modifiers into FrameLayout's LayoutParams before it measures. */
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var widthMode = MeasureSpec.getMode(widthMeasureSpec)
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    var heightMode = MeasureSpec.getMode(heightMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)

    for (child in children.widgets) {
      val layoutParams = child.value.layoutParams as LayoutParams
      layoutParams.setFrom(child.modifier)
      child.value.layoutParams = layoutParams // To force layout.
    }

    // If we're supposed to fill, never measure a size smaller than what's offered.
    // (This will turn MeasureSpec.AT_MOST into MeasureSpec.EXACTLY.)
    if (width == Constraint.Fill) widthMode = MeasureSpec.EXACTLY
    if (height == Constraint.Fill) heightMode = MeasureSpec.EXACTLY

    super.onMeasure(
      MeasureSpec.makeMeasureSpec(widthSize, widthMode),
      MeasureSpec.makeMeasureSpec(heightSize, heightMode),
    )
  }

  private fun LayoutParams.setFrom(modifier: Modifier) {
    var horizontalAlignment = defaultHorizontalAlignment
    var verticalAlignment = defaultVerticalAlignment

    var requestedWidth: Int? = null
    var requestedHeight: Int? = null

    modifier.forEach { childModifier ->
      // Check for modifier overrides in the children, otherwise default to the Box's alignment
      // values.
      when (childModifier) {
        is HorizontalAlignment -> {
          horizontalAlignment = childModifier.alignment
        }

        is VerticalAlignment -> {
          verticalAlignment = childModifier.alignment
        }

        is Width -> {
          requestedWidth = with(density) { childModifier.width.toPxInt() }
        }

        is Height -> {
          requestedHeight = with(density) { childModifier.height.toPxInt() }
        }

        is MarginModifier -> {
          with(density) {
            marginStart = childModifier.margin.start.toPxInt()
            marginEnd = childModifier.margin.end.toPxInt()
            topMargin = childModifier.margin.top.toPxInt()
            bottomMargin = childModifier.margin.bottom.toPxInt()
          }
        }
      }
    }

    if (horizontalAlignment == CrossAxisAlignment.Stretch) {
      requestedWidth = MATCH_PARENT
    }

    if (verticalAlignment == CrossAxisAlignment.Stretch) {
      requestedHeight = MATCH_PARENT
    }

    width = requestedWidth ?: horizontalAlignment.toWidth()
    height = requestedHeight ?: verticalAlignment.toWidth()
    gravity = toGravity(horizontalAlignment, verticalAlignment)
  }

  private fun toGravity(
    horizontalAlignment: CrossAxisAlignment,
    verticalAlignment: CrossAxisAlignment,
  ): Int {
    val horizontalGravity = when (horizontalAlignment) {
      CrossAxisAlignment.Start -> Gravity.LEFT
      CrossAxisAlignment.Center -> Gravity.CENTER_HORIZONTAL
      CrossAxisAlignment.End -> Gravity.RIGHT
      CrossAxisAlignment.Stretch -> Gravity.FILL_HORIZONTAL
      else -> 0
    }
    val verticalGravity = when (verticalAlignment) {
      CrossAxisAlignment.Start -> Gravity.TOP
      CrossAxisAlignment.Center -> Gravity.CENTER_VERTICAL
      CrossAxisAlignment.End -> Gravity.BOTTOM
      CrossAxisAlignment.Stretch -> Gravity.FILL_VERTICAL
      else -> 0
    }
    return horizontalGravity or verticalGravity
  }

  private fun CrossAxisAlignment.toWidth(defaultValue: Int = WRAP_CONTENT): Int {
    return when (this) {
      CrossAxisAlignment.Start -> defaultValue
      CrossAxisAlignment.Center -> defaultValue
      CrossAxisAlignment.End -> defaultValue
      CrossAxisAlignment.Stretch -> MATCH_PARENT
      else -> defaultValue
    }
  }
}
