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
package app.cash.redwood.layout.uiview

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
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.darwin.NSInteger

internal class UIViewBox :
  Box<UIView>,
  ResizableWidget<UIView> {
  override val value: View = View()

  override var modifier: Modifier = Modifier

  override val children get() = value.children

  override var sizeListener: ResizableWidget.SizeListener? by value::sizeListener

  override fun width(width: Constraint) {
    value.widthConstraint = width
    value.invalidateSize()
  }

  override fun height(height: Constraint) {
    value.heightConstraint = height
    value.invalidateSize()
  }

  override fun margin(margin: Margin) {
    value.margin = margin
    value.invalidateSize()
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.horizontalAlignment = horizontalAlignment
    value.invalidateSize()
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.verticalAlignment = verticalAlignment
    value.invalidateSize()
  }

  internal class View : UIView(CGRectZero.readValue()) {
    var widthConstraint = Constraint.Wrap
    var heightConstraint = Constraint.Wrap
    var margin: Margin = Margin.Zero
    var horizontalAlignment = CrossAxisAlignment.Start
    var verticalAlignment = CrossAxisAlignment.Start
    var sizeListener: ResizableWidget.SizeListener? = null
    private val measurer = Measurer()

    val children = UIViewChildren(
      container = this,
      insert = { index, widget ->
        if (widget is ResizableWidget<*>) {
          widget.sizeListener = object : ResizableWidget.SizeListener {
            override fun invalidateSize() {
              this@View.invalidateSize()
            }
          }
        }
        insertSubview(widget.value, index.convert<NSInteger>())
      },
      remove = { index, count ->
        for (i in index until index + count) {
          typedSubviews[index].removeFromSuperview()
        }
      },
      invalidateSize = ::invalidateSize,
    )

    fun invalidateSize() {
      val sizeListener = sizeListener
      if (sizeListener != null) {
        sizeListener.invalidateSize()
      } else {
        setNeedsLayout() // Update layout of subviews.
      }
    }

    override fun intrinsicContentSize() =
      sizeThatFits(CGSizeMake(UIViewNoIntrinsicMetric, UIViewNoIntrinsicMetric))

    override fun layoutSubviews() {
      super.layoutSubviews()

      measurer.box(
        boxDensity = Density.Default,
        boxHorizontalAlignment = horizontalAlignment,
        boxVerticalAlignment = verticalAlignment,
        boxMargin = margin,
        boxWidth = frame.useContents { size.width },
        boxHeight = frame.useContents { size.height },
      )

      for (widget in children.widgets) {
        measurer.measure(widget)
        measurer.layout(widget)
      }
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      measurer.box(
        boxDensity = Density.Default,
        boxHorizontalAlignment = horizontalAlignment,
        boxVerticalAlignment = verticalAlignment,
        boxMargin = margin,
        boxWidth = size.useContents { width },
        boxHeight = size.useContents { height },
      )

      var maxWidth = 0.0
      var maxHeight = 0.0
      for (widget in children.widgets) {
        measurer.measure(widget)
        maxWidth = maxOf(maxWidth, measurer.width + measurer.marginWidth)
        maxHeight = maxOf(maxHeight, measurer.height + measurer.marginHeight)
      }

      val boxMarginWidth = measurer.boxMarginStart + measurer.boxMarginEnd
      val boxMarginHeight = measurer.boxMarginTop + measurer.boxMarginBottom

      return CGSizeMake(maxWidth + boxMarginWidth, maxHeight + boxMarginHeight)
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
  var boxDensity = Density.Default
  var boxHorizontalAlignment = CrossAxisAlignment.Start
  var boxVerticalAlignment = CrossAxisAlignment.Start
  var boxMarginStart = 0.0
  var boxMarginEnd = 0.0
  var boxMarginTop = 0.0
  var boxMarginBottom = 0.0

  // The available space for the child view and its margins.
  var frameWidth = Double.NaN
  var frameHeight = Double.NaN

  // Inputs from the child widget.
  var horizontalAlignment = CrossAxisAlignment.Start
  var verticalAlignment = CrossAxisAlignment.Start
  var marginStart = 0.0
  var marginEnd = 0.0
  var marginTop = 0.0
  var marginBottom = 0.0
  var requestedWidth = Double.NaN
  var requestedHeight = Double.NaN

  // Measurement results.
  var width = Double.NaN
  var height = Double.NaN

  val marginWidth: CGFloat
    get() = marginStart + marginEnd
  val marginHeight: CGFloat
    get() = marginTop + marginBottom

  /** Configure the enclosing box. */
  fun box(
    boxDensity: Density,
    boxHorizontalAlignment: CrossAxisAlignment,
    boxVerticalAlignment: CrossAxisAlignment,
    boxMargin: Margin,
    boxWidth: CGFloat,
    boxHeight: CGFloat,
  ) {
    this.boxDensity = boxDensity
    this.boxHorizontalAlignment = boxHorizontalAlignment
    this.boxVerticalAlignment = boxVerticalAlignment
    with(boxDensity) {
      boxMarginStart = boxMargin.start.toPx()
      boxMarginEnd = boxMargin.end.toPx()
      boxMarginTop = boxMargin.top.toPx()
      boxMarginBottom = boxMargin.bottom.toPx()
    }
    this.frameWidth = when {
      boxWidth == UIViewNoIntrinsicMetric -> UIViewNoIntrinsicMetric
      else -> (boxWidth - boxMarginStart - boxMarginEnd).coerceAtLeast(0.0)
    }
    this.frameHeight = when {
      boxHeight == UIViewNoIntrinsicMetric -> UIViewNoIntrinsicMetric
      else -> (boxHeight - boxMarginTop - boxMarginBottom).coerceAtLeast(0.0)
    }
  }

  /** Measure [widget]. Always call [box] first. */
  fun measure(widget: Widget<UIView>) {
    this.horizontalAlignment = boxHorizontalAlignment
    this.verticalAlignment = boxVerticalAlignment
    this.marginStart = 0.0
    this.marginEnd = 0.0
    this.marginTop = 0.0
    this.marginBottom = 0.0
    this.requestedWidth = Double.NaN
    this.requestedHeight = Double.NaN

    with(boxDensity) {
      widget.modifier.forEachScoped { childModifier ->
        when (childModifier) {
          is HorizontalAlignment -> horizontalAlignment = childModifier.alignment
          is VerticalAlignment -> verticalAlignment = childModifier.alignment
          is Width -> requestedWidth = childModifier.width.toPx()
          is Height -> requestedHeight = childModifier.height.toPx()
          is Size -> {
            requestedWidth = childModifier.width.toPx()
            requestedHeight = childModifier.height.toPx()
          }

          is RedwoodMargin -> {
            with(Density.Default) {
              marginStart = maxOf(marginStart, childModifier.margin.start.toPx())
              marginEnd = maxOf(marginEnd, childModifier.margin.end.toPx())
              marginTop = maxOf(marginTop, childModifier.margin.top.toPx())
              marginBottom = maxOf(marginBottom, childModifier.margin.bottom.toPx())
            }
          }
        }
      }
    }

    val availableWidth = when {
      frameWidth == UIViewNoIntrinsicMetric -> UIViewNoIntrinsicMetric
      else -> (frameWidth - marginWidth).coerceAtLeast(0.0)
    }
    val availableHeight = when {
      frameHeight == UIViewNoIntrinsicMetric -> UIViewNoIntrinsicMetric
      else -> (frameHeight - marginHeight).coerceAtLeast(0.0)
    }

    val fitWidth = when {
      !requestedWidth.isNaN() -> requestedWidth
      horizontalAlignment == CrossAxisAlignment.Stretch -> availableWidth
      else -> availableWidth
    }
    val fitHeight = when {
      !requestedHeight.isNaN() -> requestedHeight
      verticalAlignment == CrossAxisAlignment.Stretch -> availableHeight
      else -> availableHeight
    }

    // Measure the view if don't have an exact width or height.
    val mustMeasureWidth = requestedWidth.isNaN() &&
      horizontalAlignment != CrossAxisAlignment.Stretch
    val mustMeasureHeight = requestedHeight.isNaN() &&
      verticalAlignment != CrossAxisAlignment.Stretch

    if (!mustMeasureWidth && !mustMeasureHeight) {
      this.width = fitWidth
      this.height = fitHeight
      return
    }

    val view = widget.value
    val measuredSize = view.sizeThatFits(CGSizeMake(fitWidth, fitHeight))

    width = when {
      !requestedWidth.isNaN() -> requestedWidth
      horizontalAlignment == CrossAxisAlignment.Stretch -> availableWidth
      else -> measuredSize.useContents { width }
    }

    height = when {
      !requestedHeight.isNaN() -> requestedHeight
      verticalAlignment == CrossAxisAlignment.Stretch -> availableHeight
      else -> measuredSize.useContents { height }
    }
  }

  fun layout(widget: Widget<UIView>) {
    val view = widget.value

    // Compute the view's offset.
    val x = when (horizontalAlignment) {
      CrossAxisAlignment.Center -> {
        boxMarginStart + marginStart + (frameWidth - width - marginWidth) / 2.0
      }
      CrossAxisAlignment.End -> {
        (boxMarginStart + frameWidth) - marginEnd - width
      }
      else -> boxMarginStart + marginStart
    }

    val y = when (verticalAlignment) {
      CrossAxisAlignment.Center -> {
        boxMarginTop + marginTop + (frameHeight - height - marginHeight) / 2.0
      }
      CrossAxisAlignment.End -> {
        boxMarginTop + frameHeight - marginBottom - height
      }
      else -> boxMarginTop + marginTop
    }

    // Position the view.
    view.setFrame(CGRectMake(x, y, width, height))
  }
}
