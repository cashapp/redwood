/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaUIView : UIView(cValue { CGRectZero }) {
  val rootNode = Node()

  var width = Constraint.Wrap
  var height = Constraint.Wrap

  var applyModifier: (Node, Int) -> Unit = { _, _ -> }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(Size(Size.Undefined, Size.Undefined)).toCGSize()
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    val bounds = size.useContents { sizeToBounds(this) }
    val output = calculateLayoutWithSize(bounds)
    return output.toCGSize()
  }

  override fun layoutSubviews() {
    val bounds = bounds.useContents { sizeToBounds(size) }
    calculateLayoutWithSize(bounds)

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

  private fun sizeToBounds(size: CGSize): Size {
    return Size(
      width = sizeToBoundsDimension(width, size.width),
      height = sizeToBoundsDimension(height, size.height),
    )
  }

  private fun sizeToBoundsDimension(constraint: Constraint, dimension: Double): Float {
    if (constraint == Constraint.Wrap || dimension == UIViewNoIntrinsicMetric) {
      return Size.Undefined
    } else {
      return dimension.toFloat()
    }
  }
}

private fun Size.toCGSize() = CGSizeMake(width.toDouble(), height.toDouble())

private val Node.view: UIView
  get() = (measureCallback as UIViewMeasureCallback).view
