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
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.core.view.children
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
import kotlin.math.roundToInt

internal class ViewBox(
  context: Context,
) : Box<View> {

  private val density = Density(context.resources)

  override var modifier: Modifier = Modifier

  private var defaultHorizontalAlignment = CrossAxisAlignment.Start
  private var defaultVerticalAlignment = CrossAxisAlignment.Start

  override val value = object : FrameLayout(context) {
    init {
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

      // TODO: Remove this.
      setBackgroundColor(0xFFFFFF66.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      val widgetChildren = this@ViewBox.children.widgets

      for (child in widgetChildren) {
        child.value.layoutParams = toLayoutParams(child.modifier)
        child.value.requestLayout()
      }

      // Get width and height values from layout parameters
      val width = when (layoutParams.width) {
        LayoutParams.MATCH_PARENT -> widthMeasureSpec
        LayoutParams.WRAP_CONTENT -> widgetChildren.maxOfOrNull { it.value.measuredWidth } ?: 0
        else -> layoutParams.width
      }

      val height = when (layoutParams.height) {
        LayoutParams.MATCH_PARENT -> heightMeasureSpec
        LayoutParams.WRAP_CONTENT -> widgetChildren.maxOfOrNull { it.value.measuredHeight } ?: 0
        else -> layoutParams.height
      }

      // Call to super here triggers a layout pass on the children.
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
      setMeasuredDimension(width, height)
    }
  }

  override val children: ViewGroupChildren = ViewGroupChildren(value)

  private fun toLayoutParams(modifier: Modifier): ViewGroup.LayoutParams? {
    var horizontalAlignment = defaultHorizontalAlignment
    var verticalAlignment = defaultVerticalAlignment

    var requestedWidth: Int? = null
    var requestedHeight: Int? = null

    modifier.forEach { childModifier ->
      // Check for modifier overrides in the children, otherwise default to the Box's alignment values.

      when (childModifier) {
        is HorizontalAlignment -> {
          horizontalAlignment = childModifier.alignment
        }

        is VerticalAlignment -> {
          verticalAlignment = childModifier.alignment
        }

        is Width -> {
          requestedWidth = with(density) { childModifier.width.toPx() }.roundToInt()
        }

        is Height -> {
          requestedHeight = with(density) { childModifier.height.toPx() }.roundToInt()
        }
      }
    }

    if (horizontalAlignment == CrossAxisAlignment.Stretch) {
      requestedWidth = MATCH_PARENT
    }

    if (verticalAlignment == CrossAxisAlignment.Stretch) {
      requestedHeight = MATCH_PARENT
    }

    return FrameLayout.LayoutParams(
      requestedWidth ?: horizontalAlignment.toWidth(),
      requestedHeight ?: verticalAlignment.toWidth(),
      toGravity(horizontalAlignment, verticalAlignment),
    )
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

  override fun width(width: Constraint) {
    value.updateLayoutParams {
      this.width = if (width == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
  }

  override fun height(height: Constraint) {
    value.updateLayoutParams {
      this.height = if (height == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
  }

  override fun margin(margin: Margin) {
    value.updateLayoutParams {
      val layoutParams = this as MarginLayoutParams
      with(density) {
        layoutParams.setMargins(
          margin.start.toPx().toInt(),
          margin.top.toPx().toInt(),
          margin.end.toPx().toInt(),
          margin.bottom.toPx().toInt(),
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
}
