/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UILabel
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaUIView(
  private val applyModifier: (Node, Int) -> Unit,
) : UIScrollView(cValue { CGRectZero }) {
  val rootNode = Node()

  var width = Constraint.Wrap
  var height = Constraint.Wrap

  init {
    // TODO: Support Scroll Indicators
    showsVerticalScrollIndicator = false
    showsHorizontalScrollIndicator = false
    contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
  }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(Size(Size.Undefined, Size.Undefined)).toCGSize()
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    val constrainedSize = size.useContents { sizeForConstraints(this) }
    val calculatedSize = calculateLayoutWithSize(constrainedSize)
    return calculatedSize.toCGSize()
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
      val scrollSize = if (rootNode.flexDirection == FlexDirection.Column) {
        Size(bounds.width, Size.Undefined)
      } else {
        Size(Size.Undefined, bounds.height)
      }
      val contentSize = calculateLayoutWithSize(scrollSize)
      setContentSize(contentSize.toCGSize())
      calculateLayoutWithSize(bounds)
    } else {
      // If we're not scrolling, the contentSize should equal the size of the view.
      val containerSize = calculateLayoutWithSize(bounds)
      setContentSize(containerSize.toCGSize())
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

    for (childNode in node.children) {
      layoutNodes(childNode)
    }
  }

  private fun calculateLayoutWithSize(size: Size): Size {
    for ((index, node) in rootNode.children.withIndex()) {
      applyModifier(node, index)
    }

    rootNode.measure(size.width, size.height)

    return Size(rootNode.width, rootNode.height)
  }

  private fun sizeForConstraints(size: CGSize): Size {
    return Size(
      width = sizeForConstraintsDimension(width, size.width),
      height = sizeForConstraintsDimension(height, size.height),
    )
  }

  private fun sizeForConstraintsDimension(constraint: Constraint, dimension: Double): Float {
    if (constraint == Constraint.Wrap || dimension == UIViewNoIntrinsicMetric) {
      return Size.Undefined
    } else {
      return dimension.toFloat()
    }
  }

  override fun setScrollEnabled(scrollEnabled: Boolean) {
    super.setScrollEnabled(scrollEnabled)
    setNeedsLayout()
  }
}

private fun Size.toCGSize() = CGSizeMake(width.toDouble(), height.toDouble())

private val Node.view: UIView
  get() = (measureCallback as UIViewMeasureCallback).view
