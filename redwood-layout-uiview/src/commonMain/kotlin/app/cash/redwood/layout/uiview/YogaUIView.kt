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
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaUIView(
  private val applyModifier: (Node, Int) -> Unit,
) : UIView(zeroSize) {
  val rootNode = Node()
  var width = Constraint.Wrap
  var height = Constraint.Wrap

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(undefinedSize)
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

    calculateLayoutWithSize(bounds)

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

    size.useContents {
      rootNode.measure(width.toFloat(), height.toFloat())
    }

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
}
