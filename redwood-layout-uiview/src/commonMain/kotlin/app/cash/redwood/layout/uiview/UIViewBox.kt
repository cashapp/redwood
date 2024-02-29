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
import app.cash.redwood.widget.UIViewChildren
import kotlinx.cinterop.CValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIEvent
import platform.UIKit.UIView
import platform.darwin.NSInteger

internal class UIViewBox : Box<UIView> {
  override val value: View = View()

  override var modifier: Modifier = Modifier

  override val children get() = value.children

  override fun width(width: Constraint) {
    value.widthConstraint = width
    value.setNeedsLayout()
  }

  override fun height(height: Constraint) {
    value.heightConstraint = height
    value.setNeedsLayout()
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
    value.setNeedsLayout()
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.horizontalAlignment = horizontalAlignment
    value.setNeedsLayout()
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.verticalAlignment = verticalAlignment
    value.setNeedsLayout()
  }

  internal class View : UIView(CGRectZero.readValue()) {
    var widthConstraint = Constraint.Wrap
    var heightConstraint = Constraint.Wrap
    var horizontalAlignment = CrossAxisAlignment.Start
    var verticalAlignment = CrossAxisAlignment.Start

    val children = UIViewChildren(
      parent = this,
      insert = { view, index ->
        insertSubview(view, index.convert<NSInteger>())
        view.setNeedsLayout()
      },
      remove = { index, count ->
        val views = Array(count) {
          typedSubviews[index].also(UIView::removeFromSuperview)
        }
        setNeedsLayout()
        return@UIViewChildren views
      },
    )

    override fun layoutSubviews() {
      super.layoutSubviews()

      children.widgets.forEach { widget ->
        val view = widget.value
        view.sizeToFit()

        // Check for modifier overrides in the children, otherwise default to the Box's alignment values.
        var itemHorizontalAlignment = horizontalAlignment
        var itemVerticalAlignment = verticalAlignment

        var requestedWidth: CGFloat = Double.MIN_VALUE
        var requestedHeight: CGFloat = Double.MIN_VALUE

        widget.modifier.forEach { childModifier ->
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

        // Use requested modifiers, otherwise use the size established from sizeToFit().
        var childWidth: CGFloat = if (requestedWidth != Double.MIN_VALUE) {
          requestedWidth
        } else {
          view.frame.useContents { size.width }
        }
        var childHeight: CGFloat = if (requestedHeight != Double.MIN_VALUE) {
          requestedHeight
        } else {
          view.frame.useContents { size.height }
        }

        // Compute origin and stretch if needed.
        var x: CGFloat = 0.0
        var y: CGFloat = 0.0
        when (itemHorizontalAlignment) {
          CrossAxisAlignment.Stretch -> {
            x = 0.0
            childWidth = frame.useContents { size.width }
          }

          CrossAxisAlignment.Start -> x = 0.0

          CrossAxisAlignment.Center -> x = (frame.useContents { size.width } - childWidth) / 2.0

          CrossAxisAlignment.End -> x = frame.useContents { size.width } - childWidth
        }
        when (itemVerticalAlignment) {
          CrossAxisAlignment.Stretch -> {
            y = 0.0
            childHeight = frame.useContents { size.height }
          }

          CrossAxisAlignment.Start -> y = 0.0

          CrossAxisAlignment.Center -> y = (frame.useContents { size.height } - childHeight) / 2.0

          CrossAxisAlignment.End -> y = frame.useContents { size.height } - childHeight
        }

        // Position the view.
        view.setFrame(CGRectMake(x, y, childWidth, childHeight))
      }
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      var maxItemWidth: CGFloat = 0.0
      var maxItemHeight: CGFloat = 0.0
      var maxRequestedWidth: CGFloat = 0.0
      var maxRequestedHeight: CGFloat = 0.0

      // Get the largest sizes based on explicit widget modifiers.
      children.widgets.forEach { widget ->
        widget.modifier.forEach { childModifier ->
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

    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? {
      // Don't consume touch events that don't hit a subview.
      return typedSubviews.firstNotNullOfOrNull { subview ->
        subview.hitTest(subview.convertPoint(point, fromView = this), withEvent)
      }
    }
  }
}
