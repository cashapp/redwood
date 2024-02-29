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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMarginsRelative
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ViewGroupChildren

internal class ViewBox(
  context: Context,
) : FrameLayout(context), Box<View> {
  private val density = Density(context.resources)
  private var horizontalAlignment = CrossAxisAlignment.Start
  private var verticalAlignment = CrossAxisAlignment.Start
  private var width = Constraint.Wrap
  private var height = Constraint.Wrap

  override var modifier: Modifier = Modifier

  override val value get() = this

  override val children = ViewGroupChildren(this)

  override fun generateDefaultLayoutParams(): LayoutParams {
    return LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  }

  override fun width(width: Constraint) {
    this.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    this.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    updateLayoutParams<MarginLayoutParams> {
      with(density) {
        updateMarginsRelative(
          start = margin.start.toPxInt(),
          top = margin.top.toPxInt(),
          end = margin.end.toPxInt(),
          bottom = margin.bottom.toPxInt(),
        )
      }
    }
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    this.horizontalAlignment = horizontalAlignment
    invalidate()
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    this.verticalAlignment = verticalAlignment
    invalidate()
  }

  /** Flush Redwood's modifiers into FrameLayout's LayoutParams before it measures. */
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthMode = if (width == Constraint.Fill) {
      MeasureSpec.EXACTLY
    } else {
      MeasureSpec.getMode(widthMeasureSpec)
    }
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)

    val heightMode = if (height == Constraint.Fill) {
      MeasureSpec.EXACTLY
    } else {
      MeasureSpec.getMode(heightMeasureSpec)
    }
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)

    for (child in children.widgets) {
      child.value.updateLayoutParams<LayoutParams> {
        setFrom(child.modifier)
      }
    }

    super.onMeasure(
      MeasureSpec.makeMeasureSpec(widthSize, widthMode),
      MeasureSpec.makeMeasureSpec(heightSize, heightMode),
    )
  }

  private fun LayoutParams.setFrom(modifier: Modifier) {
    var horizontalAlignment = horizontalAlignment
    var verticalAlignment = verticalAlignment
    var requestedWidth = Int.MIN_VALUE
    var requestedHeight = Int.MIN_VALUE

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

        is Size -> {
          requestedWidth = with(density) { childModifier.width.toPxInt() }
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

    width = if (requestedWidth != Int.MIN_VALUE) {
      requestedWidth
    } else {
      horizontalAlignment.toDimension()
    }
    height = if (requestedHeight != Int.MIN_VALUE) {
      requestedHeight
    } else {
      verticalAlignment.toDimension()
    }
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
      else -> throw AssertionError()
    }
    val verticalGravity = when (verticalAlignment) {
      CrossAxisAlignment.Start -> Gravity.TOP
      CrossAxisAlignment.Center -> Gravity.CENTER_VERTICAL
      CrossAxisAlignment.End -> Gravity.BOTTOM
      CrossAxisAlignment.Stretch -> Gravity.FILL_VERTICAL
      else -> throw AssertionError()
    }
    return horizontalGravity or verticalGravity
  }

  private fun CrossAxisAlignment.toDimension(): Int {
    return when (this) {
      CrossAxisAlignment.Start -> WRAP_CONTENT
      CrossAxisAlignment.Center -> WRAP_CONTENT
      CrossAxisAlignment.End -> WRAP_CONTENT
      CrossAxisAlignment.Stretch -> MATCH_PARENT
      else -> throw AssertionError()
    }
  }
}
