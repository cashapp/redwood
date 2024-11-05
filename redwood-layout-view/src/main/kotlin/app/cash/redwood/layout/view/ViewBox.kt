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
import android.view.View.LAYOUT_DIRECTION_LTR
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Margin as RedwoodMargin
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget

internal class ViewBox(context: Context) : Box<View> {
  private val delegate = BoxViewGroup(context)
  override val value: View get() = delegate
  override var modifier by delegate::modifier
  override val children get() = delegate.children

  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) = delegate.horizontalAlignment(horizontalAlignment)
  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) = delegate.verticalAlignment(verticalAlignment)
}

private class BoxViewGroup(
  context: Context,
) : ViewGroup(context) {
  private val density = Density(context.resources)
  private var horizontalAlignment = CrossAxisAlignment.Start
  private var verticalAlignment = CrossAxisAlignment.Start
  private var widthConstraint = Constraint.Wrap
  private var heightConstraint = Constraint.Wrap
  private var margin: Margin = Margin.Zero

  var modifier: Modifier = Modifier

  val children = ViewGroupChildren(this)

  private val measurer = Measurer()

  fun width(width: Constraint) {
    this.widthConstraint = width
    requestLayout()
  }

  fun height(height: Constraint) {
    this.heightConstraint = height
    requestLayout()
  }

  fun margin(margin: Margin) {
    this.margin = margin
    requestLayout()
  }

  fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    this.horizontalAlignment = horizontalAlignment
    requestLayout()
  }

  fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    this.verticalAlignment = verticalAlignment
    requestLayout()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    val widthMode = MeasureSpec.getMode(widthMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)
    val heightMode = MeasureSpec.getMode(heightMeasureSpec)

    // Short circuit if this box's dimensions are already fully specified.
    if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
      setMeasuredDimension(widthSize, heightSize)
      return
    }

    measurer.box(
      layoutDirection = layoutDirection,
      boxDensity = density,
      boxHorizontalAlignment = horizontalAlignment,
      boxVerticalAlignment = verticalAlignment,
      boxMargin = margin,
      boxWidth = widthSize,
      boxWidthUnspecified = widthMode == MeasureSpec.UNSPECIFIED,
      boxHeight = heightSize,
      boxHeightUnspecified = heightMode == MeasureSpec.UNSPECIFIED,
    )

    var maxWidth = 0
    var maxHeight = 0
    for (widget in children.widgets) {
      measurer.measure(widget = widget)
      maxWidth = maxOf(maxWidth, measurer.width + measurer.marginWidth)
      maxHeight = maxOf(maxHeight, measurer.height + measurer.marginHeight)
    }

    val boxMarginWidth = measurer.boxMarginStart + measurer.boxMarginEnd
    val boxMarginHeight = measurer.boxMarginTop + measurer.boxMarginBottom

    val resultWidth = when (widthMode) {
      MeasureSpec.EXACTLY -> widthSize
      MeasureSpec.AT_MOST -> (maxWidth + boxMarginWidth).coerceAtMost(widthSize)
      else -> maxWidth + boxMarginWidth
    }
    val resultHeight = when (heightMode) {
      MeasureSpec.EXACTLY -> heightSize
      MeasureSpec.AT_MOST -> (maxHeight + boxMarginHeight).coerceAtMost(heightSize)
      else -> maxHeight + boxMarginHeight
    }

    setMeasuredDimension(resultWidth, resultHeight)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    measurer.box(
      layoutDirection = layoutDirection,
      boxDensity = density,
      boxHorizontalAlignment = horizontalAlignment,
      boxVerticalAlignment = verticalAlignment,
      boxMargin = margin,
      boxWidth = (right - left).coerceAtLeast(0),
      boxHeight = (bottom - top).coerceAtLeast(0),
    )

    for (widget in children.widgets) {
      measurer.measure(widget)
      measurer.layout(widget)
    }
  }
}

/**
 * Measures and lays out one child view at a time.
 *
 * This class is mutable and reused to avoid object allocation.
 */
private class Measurer {
  // Inputs from the box.
  var layoutDirection = LAYOUT_DIRECTION_LTR
  var boxDensity = Density(1.0)
  var boxHorizontalAlignment = CrossAxisAlignment.Start
  var boxVerticalAlignment = CrossAxisAlignment.Start
  var boxMarginStart = 0
  var boxMarginEnd = 0
  var boxMarginTop = 0
  var boxMarginBottom = 0
  var boxWidthUnspecified = false
  var boxHeightUnspecified = false

  // The available space for the child view and its margins.
  var frameWidth = -1
  var frameHeight = -1

  // Inputs from the child widget.
  var horizontalAlignment = CrossAxisAlignment.Start
  var verticalAlignment = CrossAxisAlignment.Start
  var marginStart = 0
  var marginEnd = 0
  var marginTop = 0
  var marginBottom = 0
  var requestedWidth = -1
  var requestedHeight = -1

  // Measurement results.
  var width = -1
  var height = -1

  val marginWidth: Int
    get() = marginStart + marginEnd
  val marginHeight: Int
    get() = marginTop + marginBottom

  /** Configure the enclosing box. */
  fun box(
    layoutDirection: Int,
    boxDensity: Density,
    boxHorizontalAlignment: CrossAxisAlignment,
    boxVerticalAlignment: CrossAxisAlignment,
    boxMargin: Margin,
    boxWidth: Int,
    boxWidthUnspecified: Boolean = false,
    boxHeight: Int,
    boxHeightUnspecified: Boolean = false,
  ) {
    this.layoutDirection = layoutDirection
    this.boxDensity = boxDensity
    this.boxHorizontalAlignment = boxHorizontalAlignment
    this.boxVerticalAlignment = boxVerticalAlignment
    with(boxDensity) {
      boxMarginStart = boxMargin.start.toPxInt()
      boxMarginEnd = boxMargin.end.toPxInt()
      boxMarginTop = boxMargin.top.toPxInt()
      boxMarginBottom = boxMargin.bottom.toPxInt()
    }
    this.frameWidth = (boxWidth - boxMarginStart - boxMarginEnd).coerceAtLeast(0)
    this.boxWidthUnspecified = boxWidthUnspecified
    this.frameHeight = (boxHeight - boxMarginTop - boxMarginBottom).coerceAtLeast(0)
    this.boxHeightUnspecified = boxHeightUnspecified
  }

  /** Measure [widget]. Always call [box] first. */
  fun measure(widget: Widget<View>) {
    this.horizontalAlignment = boxHorizontalAlignment
    this.verticalAlignment = boxVerticalAlignment
    this.marginStart = 0
    this.marginEnd = 0
    this.marginTop = 0
    this.marginBottom = 0
    this.requestedWidth = -1
    this.requestedHeight = -1

    with(boxDensity) {
      widget.modifier.forEachScoped { childModifier ->
        when (childModifier) {
          is HorizontalAlignment -> horizontalAlignment = childModifier.alignment
          is VerticalAlignment -> verticalAlignment = childModifier.alignment
          is Width -> requestedWidth = childModifier.width.toPxInt()
          is Height -> requestedHeight = childModifier.height.toPxInt()
          is Size -> {
            requestedWidth = childModifier.width.toPxInt()
            requestedHeight = childModifier.height.toPxInt()
          }

          is RedwoodMargin -> {
            marginStart = maxOf(marginStart, childModifier.margin.start.toPxInt())
            marginEnd = maxOf(marginEnd, childModifier.margin.end.toPxInt())
            marginTop = maxOf(marginTop, childModifier.margin.top.toPxInt())
            marginBottom = maxOf(marginBottom, childModifier.margin.bottom.toPxInt())
          }
        }
      }
    }

    val availableWidth = (frameWidth - marginWidth).coerceAtLeast(0)
    val availableHeight = (frameHeight - marginHeight).coerceAtLeast(0)

    val widthMeasureSpec = when {
      requestedWidth != -1 -> makeMeasureSpec(requestedWidth, MeasureSpec.EXACTLY)
      horizontalAlignment == CrossAxisAlignment.Stretch ->
        makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY)
      boxWidthUnspecified -> makeMeasureSpec(availableWidth, MeasureSpec.UNSPECIFIED)
      else -> makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST)
    }

    val heightMeasureSpec = when {
      requestedHeight != -1 -> makeMeasureSpec(requestedHeight, MeasureSpec.EXACTLY)
      verticalAlignment == CrossAxisAlignment.Stretch ->
        makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY)
      boxHeightUnspecified -> makeMeasureSpec(availableHeight, MeasureSpec.UNSPECIFIED)
      else -> makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST)
    }

    widget.value.measure(widthMeasureSpec, heightMeasureSpec)

    width = when {
      requestedWidth != -1 -> requestedWidth
      horizontalAlignment == CrossAxisAlignment.Stretch -> availableWidth
      else -> widget.value.measuredWidth
    }

    height = when {
      requestedHeight != -1 -> requestedHeight
      verticalAlignment == CrossAxisAlignment.Stretch -> availableHeight
      else -> widget.value.measuredHeight
    }
  }

  /** Layout [widget]. Always call [measure] first. */
  fun layout(widget: Widget<View>) {
    val marginLeft: Int
    val marginRight: Int
    val alignRight: Boolean

    if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
      marginLeft = marginEnd + boxMarginEnd
      marginRight = marginStart + boxMarginStart
      alignRight = horizontalAlignment == CrossAxisAlignment.Start
    } else {
      marginLeft = marginStart + boxMarginStart
      marginRight = marginEnd + boxMarginEnd
      alignRight = horizontalAlignment == CrossAxisAlignment.End
    }

    val left = when {
      horizontalAlignment == CrossAxisAlignment.Center -> {
        marginLeft + (frameWidth - width - marginWidth) / 2
      }
      alignRight -> {
        (boxMarginStart + frameWidth + boxMarginEnd) - marginRight - width
      }
      else -> marginLeft
    }

    val top = when (verticalAlignment) {
      CrossAxisAlignment.Center -> {
        boxMarginTop + marginTop + (frameHeight - height - marginHeight) / 2
      }
      CrossAxisAlignment.End -> {
        boxMarginTop + frameHeight - marginBottom - height
      }
      else -> boxMarginTop + marginTop
    }

    widget.value.layout(left, top, left + width, top + height)
  }
}
