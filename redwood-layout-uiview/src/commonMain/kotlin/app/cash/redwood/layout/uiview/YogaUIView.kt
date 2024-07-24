/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
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
) : UIScrollView(cValue { CGRectZero }), UIScrollViewDelegateProtocol {
  val rootNode = Node()

  var width = Constraint.Wrap
  var height = Constraint.Wrap

  var onScroll: ((Double) -> Unit)? = null

  init {
    // TODO: Support scroll indicators.
    scrollEnabled = false
    showsVerticalScrollIndicator = false
    showsHorizontalScrollIndicator = false
    contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever
  }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(CGSizeMake(Size.UNDEFINED.toDouble(), Size.UNDEFINED.toDouble()))
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    val constrainedSize = size.useContents { sizeForConstraints(this) }
    return calculateLayoutWithSize(constrainedSize)
  }

  override fun layoutSubviews() {
    super.layoutSubviews()

    // Based on the constraints of Fill or Wrap, we
    // calculate a size that the container should fit in.
    val bounds = bounds.useContents {
      sizeForConstraints(size)
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
      val contentSize = calculateLayoutWithSize(scrollSize)
      setContentSize(contentSize)
      calculateLayoutWithSize(bounds)
    } else {
      // If we're not scrolling, the contentSize should equal the size of the view.
      val containerSize = calculateLayoutWithSize(bounds)
      setContentSize(containerSize)
    }

    // Layout the nodes based on the calculatedLayouts above.
    for (childNode in rootNode.children) {
      layoutNodes(childNode)
    }
  }

  private fun layoutNodes(node: Node) {
    val x = node.left.toDouble()
    val y = node.top.toDouble()
    val width = node.width.toDouble()
    val height = node.height.toDouble()
    node.view.setFrame(CGRectMake(x, y, width, height))

    if (node.view is YogaUIView) {
      // Optimization: for a YogaUIView nested within another YogaUIView,
      // there's no need to call layoutNodes for its children here,
      // as it will happen within its own layoutSubviews() pass.
      return
    }

    for (childNode in node.children) {
      layoutNodes(childNode)
    }
  }

  private fun calculateLayoutWithSize(size: CValue<CGSize>): CValue<CGSize> {
    for ((index, node) in rootNode.children.withIndex()) {
      applyModifier(node, index)
    }

    size.useContents { rootNode.measure(width.toFloat(), height.toFloat()) }

    return CGSizeMake(rootNode.width.toDouble(), rootNode.height.toDouble())
  }

  private fun sizeForConstraints(size: CGSize): CValue<CGSize> {
    return CGSizeMake(
      width = sizeForConstraintsDimension(width, size.width),
      height = sizeForConstraintsDimension(height, size.height),
    )
  }

  private fun sizeForConstraintsDimension(constraint: Constraint, dimension: Double): Double {
    if (constraint == Constraint.Wrap || dimension == UIViewNoIntrinsicMetric) {
      return Size.UNDEFINED.toDouble()
    } else {
      return dimension
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
      val offset = scrollView.contentOffset.useContents {
        if (isColumn()) y else x
      }
      onScroll(offset)
    }
  }

  private fun isColumn(): Boolean {
    return rootNode.flexDirection == FlexDirection.Column
  }
}

private val Node.view: UIView
  get() = (measureCallback as UIViewMeasureCallback).view
