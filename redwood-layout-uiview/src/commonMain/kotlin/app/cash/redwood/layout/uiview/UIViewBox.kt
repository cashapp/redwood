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
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.UIViewChildren
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
import platform.darwin.NSInteger

internal class UIViewBox : Box<UIView> {
  override val value: View = View()

  override var modifier: Modifier = Modifier

  override val children = value.children

  override fun width(width: Constraint) {
    value.widthConstraint = width
  }

  override fun height(height: Constraint) {
    value.heightConstraint = height
  }

  override fun margin(margin: Margin) {
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.horizontalAlignment = horizontalAlignment
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.verticalAlignment = verticalAlignment
  }

  internal class View() : UIView(CGRectZero.readValue()) {
    var widthConstraint = Constraint.Wrap
    var heightConstraint = Constraint.Wrap

    var horizontalAlignment = CrossAxisAlignment.Start
    var verticalAlignment = CrossAxisAlignment.Start

    val children = UIViewChildren(
      this,
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

      children.widgets.forEach {
        val view = it.value
        view.sizeToFit()
        var childWidth: CGFloat = view.frame.useContents { this.size.width }
        var childHeight: CGFloat = view.frame.useContents { this.size.height }

        // Check for modifier overrides in the children, otherwise default to the Box's alignment values
        var itemHorizontalAlignment = horizontalAlignment
        var itemVerticalAlignment = verticalAlignment
        it.modifier.forEach { childModifier ->
          when (childModifier) {
            is HorizontalAlignment -> {
              itemHorizontalAlignment = childModifier.alignment
            }
            is VerticalAlignment -> {
              itemVerticalAlignment = childModifier.alignment
            }
          }
        }

        // Compute origin and stretch if needed
        var x: CGFloat = 0.0
        var y: CGFloat = 0.0
        when (itemHorizontalAlignment) {
          CrossAxisAlignment.Stretch -> {
            x = 0.0
            childWidth = frame.useContents { this.size.width }
          }
          CrossAxisAlignment.Start -> x = 0.0
          CrossAxisAlignment.Center -> x = (frame.useContents { this.size.width } - childWidth) / 2.0
          CrossAxisAlignment.End -> x = frame.useContents { this.size.width } - childWidth
        }
        when (itemVerticalAlignment) {
          CrossAxisAlignment.Stretch -> {
            y = 0.0
            childHeight = frame.useContents { this.size.height }
          }
          CrossAxisAlignment.Start -> y = 0.0
          CrossAxisAlignment.Center -> y = (frame.useContents { this.size.height } - childHeight) / 2.0
          CrossAxisAlignment.End -> y = frame.useContents { this.size.height } - childHeight
        }

        // Position the view
        view.setFrame(CGRectMake(x, y, childWidth, childHeight))
      }
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      var width: CGFloat = 0.0
      var height: CGFloat = 0.0

      // Calculate the size based on Constraint Values
      when (widthConstraint) {
        Constraint.Fill -> {
          when (heightConstraint) {
            Constraint.Fill -> {
              width = size.useContents { this.width }
              height = size.useContents { this.height }
            }
            Constraint.Wrap -> {
              height = size.useContents { this.height }

              width = typedSubviews
                .map { it.sizeThatFits(size).useContents { this.width } }
                .max()
            }
          }
        }
        Constraint.Wrap -> {
          when (heightConstraint) {
            Constraint.Fill -> {
              width = size.useContents { this.width }

              // calculate the height of the biggest item
              height = typedSubviews
                .map { it.sizeThatFits(size).useContents { this.height } }
                .max()
            }
            Constraint.Wrap -> {
              val unconstrainedSizes = typedSubviews
                .map { it.sizeThatFits(size) }

              width = unconstrainedSizes
                .map { it.useContents { this.width } }
                .max()

              height = unconstrainedSizes
                .map { it.useContents { this.height } }
                .max()
            }
          }
        }
      }
      return CGSizeMake(width, height)
    }
  }
}
