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
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIView
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
    value.layoutMargins = with(Density.Default) {
      UIEdgeInsetsMake(
        top = margin.top.toPx(),
        left = margin.start.toPx(),
        bottom = margin.bottom.toPx(),
        right = margin.end.toPx(),
      )
    }
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
    var horizontalAlignment = CrossAxisAlignment.Start
    var verticalAlignment = CrossAxisAlignment.Start
    var sizeListener: ResizableWidget.SizeListener? = null

    val children = UIViewChildren(
      container = this,
      insert = { widget, view, _, index ->
        if (widget is ResizableWidget<*>) {
          widget.sizeListener = object : ResizableWidget.SizeListener {
            override fun invalidateSize() {
              this@View.invalidateSize()
            }
          }
        }
        insertSubview(view, index.convert<NSInteger>())
      },
      remove = { index, count ->
        val views = Array(count) {
          typedSubviews[index].also(UIView::removeFromSuperview)
        }
        return@UIViewChildren views
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

    override fun layoutSubviews() {
      super.layoutSubviews()

      children.widgets.forEach { widget ->
        layoutWidget(widget)
      }
    }

    private fun layoutWidget(widget: Widget<UIView>) {
      val view = widget.value

      // Check for modifier overrides in the children, otherwise default to the Box's alignment values.
      var itemHorizontalAlignment = horizontalAlignment
      var itemVerticalAlignment = verticalAlignment

      val frameWidth = frame.useContents { size.width }
      val frameHeight = frame.useContents { size.height }

      var requestedWidth: CGFloat = Double.NaN
      var requestedHeight: CGFloat = Double.NaN

      widget.modifier.forEachScoped { childModifier ->
        when (childModifier) {
          is HorizontalAlignment -> {
            itemHorizontalAlignment = childModifier.alignment
          }

          is VerticalAlignment -> {
            itemVerticalAlignment = childModifier.alignment
          }

          is Width -> with(Density.Default) {
            requestedWidth = childModifier.width.toPx()
          }

          is Height -> with(Density.Default) {
            requestedHeight = childModifier.height.toPx()
          }

          is Size -> with(Density.Default) {
            requestedWidth = childModifier.width.toPx()
            requestedHeight = childModifier.height.toPx()
          }
        }
      }

      // Initialize this to the view's width before any measurement. Use the frame's dimensions
      // if the user didn't explicitly specify one. This is the final value for Stretch alignment.
      var viewWidth = when {
        !requestedWidth.isNaN() -> requestedWidth
        else -> frameWidth
      }
      var viewHeight = when {
        !requestedHeight.isNaN() -> requestedHeight
        else -> frameHeight
      }

      // Measure the view if don't have an exact width or height.
      val mustMeasureWidth = requestedWidth.isNaN() &&
        itemHorizontalAlignment != CrossAxisAlignment.Stretch
      val mustMeasureHeight = requestedHeight.isNaN() &&
        itemVerticalAlignment != CrossAxisAlignment.Stretch

      if (mustMeasureWidth || mustMeasureHeight) {
        val fittingSize = view.systemLayoutSizeFittingSize(CGSizeMake(viewWidth, viewHeight))

        viewWidth = when {
          !requestedWidth.isNaN() -> requestedWidth
          itemHorizontalAlignment == CrossAxisAlignment.Stretch -> frameWidth
          else -> fittingSize.useContents { width }
        }

        viewHeight = when {
          !requestedHeight.isNaN() -> requestedHeight
          itemVerticalAlignment == CrossAxisAlignment.Stretch -> frameHeight
          else -> fittingSize.useContents { height }
        }
      }

      // Compute the view's offset.
      val x = when (itemHorizontalAlignment) {
        CrossAxisAlignment.Center -> (frameWidth - viewWidth) / 2.0
        CrossAxisAlignment.End -> frameWidth - viewWidth
        else -> 0.0
      }
      val y = when (itemVerticalAlignment) {
        CrossAxisAlignment.Center -> (frameHeight - viewHeight) / 2.0
        CrossAxisAlignment.End -> frameHeight - viewHeight
        else -> 0.0
      }

      // Position the view.
      view.setFrame(CGRectMake(x, y, viewWidth, viewHeight))
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      var maxItemWidth: CGFloat = 0.0
      var maxItemHeight: CGFloat = 0.0
      var maxRequestedWidth: CGFloat = 0.0
      var maxRequestedHeight: CGFloat = 0.0

      // Get the largest sizes based on explicit widget modifiers.
      children.widgets.forEach { widget ->
        widget.modifier.forEachScoped { childModifier ->
          when (childModifier) {
            is Width -> with(Density.Default) {
              maxRequestedWidth = maxOf(maxRequestedWidth, childModifier.width.toPx())
            }

            is Height -> with(Density.Default) {
              maxRequestedHeight = maxOf(maxRequestedHeight, childModifier.height.toPx())
            }

            is Size -> with(Density.Default) {
              maxRequestedWidth = maxOf(maxRequestedWidth, childModifier.width.toPx())
              maxRequestedHeight = maxOf(maxRequestedHeight, childModifier.height.toPx())
            }
          }
        }
      }

      // Calculate the size based on Constraint values.
      when (widthConstraint) {
        Constraint.Fill -> when (heightConstraint) {
          Constraint.Fill -> { // Fill Fill
            size.useContents {
              maxItemWidth = width
              maxItemHeight = height
            }
          }

          Constraint.Wrap -> { // Fill Wrap
            maxItemWidth = size.useContents { width }
            for (subview in typedSubviews) {
              subview.sizeThatFits(size).useContents {
                maxItemHeight = maxOf(maxItemHeight, height)
              }
            }
          }
        }

        Constraint.Wrap -> when (heightConstraint) {
          Constraint.Fill -> { // Wrap Fill
            for (subview in typedSubviews) {
              subview.sizeThatFits(size).useContents {
                maxItemWidth = maxOf(maxItemWidth, width)
              }
            }
            maxItemHeight = size.useContents { height }
          }

          Constraint.Wrap -> { // Wrap Wrap
            for (subview in typedSubviews) {
              subview.sizeThatFits(size).useContents {
                maxItemWidth = maxOf(maxItemWidth, width)
                maxItemHeight = maxOf(maxItemHeight, height)
              }
            }
          }
        }
      }
      return CGSizeMake(
        width = maxOf(maxRequestedWidth, maxItemWidth),
        height = maxOf(maxRequestedHeight, maxItemHeight),
      )
    }
  }
}
