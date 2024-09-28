/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.ui.Px
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
import platform.UIKit.UIScrollViewDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaUIView(
  private val applyModifier: (Node, Int) -> Unit,
  private val incremental: Boolean,
) : UIScrollView(cValue { CGRectZero }), UIScrollViewDelegateProtocol {
  val rootNode = Node()

  var widthConstraint = Constraint.Wrap
  var heightConstraint = Constraint.Wrap

  var onScroll: ((Px) -> Unit)? = null

  init {
    // TODO: Support scroll indicators.
    scrollEnabled = false
    showsVerticalScrollIndicator = false
    showsHorizontalScrollIndicator = false
    contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever
  }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(Size.UNDEFINED, Size.UNDEFINED, exact = false)
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    return calculateLayoutWithSize(size = size, exact = false)
  }

  override fun layoutSubviews() {
    super.layoutSubviews()

    // Based on the constraints of Fill or Wrap, we
    // calculate a size that the container should fit in.
    val bounds = bounds.useContents {
      CGSizeMake(size.width, size.height)
    }

    if (scrollEnabled) {
      // When scrolling is enabled, we want to calculate and apply the contentSize
      // separately and have it grow a much as needed in the flexDirection.
      // This duplicates the calculation we're doing above, and should be
      // combined into one call.
      val scrollSize = bounds.useContents {
        if (isColumn()) {
          CGSizeMake(width, Size.UNDEFINED.toDouble())
        } else {
          CGSizeMake(Size.UNDEFINED.toDouble(), height)
        }
      }
      val contentSize = calculateLayoutWithSize(size = scrollSize, exact = false)
      setContentSize(contentSize)
      calculateLayoutWithSize(size = bounds, exact = true)
    } else {
      // If we're not scrolling, the contentSize should equal the size of the view.
      val containerSize = calculateLayoutWithSize(size = bounds, exact = true)
      setContentSize(containerSize)
    }

    // Layout the nodes based on the calculatedLayouts above.
    for (childNode in rootNode.children) {
      childNode.view.setFrame(
        CGRectMake(
          x = childNode.left.toDouble(),
          y = childNode.top.toDouble(),
          width = childNode.width.toDouble(),
          height = childNode.height.toDouble(),
        ),
      )
    }
  }

  private fun calculateLayoutWithSize(size: CValue<CGSize>, exact: Boolean): CValue<CGSize> {
    return size.useContents {
      calculateLayoutWithSize(width.toYoga(), height.toYoga(), exact)
    }
  }

  private fun calculateLayoutWithSize(
    width: Float,
    height: Float,
    exact: Boolean,
  ): CValue<CGSize> {
    if (exact) {
      rootNode.requestedMinWidth = Size.UNDEFINED
      rootNode.requestedWidth = width
      rootNode.requestedMaxWidth = Size.UNDEFINED
      rootNode.requestedMinHeight = Size.UNDEFINED
      rootNode.requestedHeight = height
      rootNode.requestedMaxHeight = Size.UNDEFINED

    } else {
      rootNode.requestedMinWidth = Size.UNDEFINED
      rootNode.requestedWidth = Size.UNDEFINED
      rootNode.requestedMaxWidth = Size.UNDEFINED
      rootNode.requestedMinHeight = Size.UNDEFINED
      rootNode.requestedHeight = Size.UNDEFINED
      rootNode.requestedMaxHeight = Size.UNDEFINED

      rootNode.requestedWidth = when (widthConstraint) {
        Constraint.Fill -> width
        else -> Size.UNDEFINED
      }

      rootNode.requestedHeight = when (heightConstraint) {
        Constraint.Fill -> height
        else -> Size.UNDEFINED
      }
    }

    for ((index, node) in rootNode.children.withIndex()) {
      applyModifier(node, index)
    }

    if (!incremental) {
      rootNode.markEverythingDirty()
    }

    rootNode.measureOnly(Size.UNDEFINED, Size.UNDEFINED)

    return CGSizeMake(rootNode.width.toDouble(), rootNode.height.toDouble())
  }

  /** Convert a UIView dimension (a double) to a Yoga dimension (a float). */
  private fun Double.toYoga(): Float {
    return when (this) {
      UIViewNoIntrinsicMetric -> Size.UNDEFINED
      else -> this.toFloat()
    }
  }

  override fun setScrollEnabled(scrollEnabled: Boolean) {
    delegate = if (scrollEnabled) this else null

    val previousScrollEnabled = this.scrollEnabled

    super.setScrollEnabled(scrollEnabled)

    if (scrollEnabled != previousScrollEnabled) {
      setNeedsLayout()
    }
  }

  override fun scrollViewDidScroll(scrollView: UIScrollView) {
    val onScroll = onScroll
    if (onScroll != null) {
      val max = scrollView.contentSize.useContents {
        if (isColumn()) height else width
      }
      val offset = scrollView.contentOffset.useContents {
        if (isColumn()) y else x
      }.coerceIn(minimumValue = 0.0, maximumValue = max)
      onScroll(Px(offset))
    }
  }

  private fun isColumn(): Boolean {
    return rootNode.flexDirection == FlexDirection.Column
  }
}

private val Node.view: UIView
  get() = (measureCallback as UIViewMeasureCallback).view
