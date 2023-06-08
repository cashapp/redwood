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
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
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
      YGApplyLayoutToViewHierarchy(childNode)
    }
  }

  private fun calculateLayoutWithSize(size: Size): Size {
    YGAttachNodesFromViewHierachy(this)

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

private fun YGAttachNodesFromViewHierachy(yoga: YogaUIView) {
  if (yoga.typedSubviews.isEmpty()) {
    yoga.rootNode.children.clear()
    yoga.rootNode.measureCallback = ViewMeasureCallback(yoga)
    return
  }

  // Nodes with children cannot have measure functions.
  yoga.rootNode.measureCallback = null

  val currentViews = yoga.rootNode.children.mapNotNull { it.view }
  val subviews = yoga.typedSubviews
  if (currentViews != subviews) {
    yoga.rootNode.children.clear()
    for (view in subviews) {
      yoga.rootNode.children += view.asNode()
    }
  }
}

private class ViewMeasureCallback(val view: UIView) : MeasureCallback {
  override fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size {
    val view = node.view!!
    val constrainedWidth = when (widthMode) {
      MeasureMode.Undefined -> UIViewNoIntrinsicMetric
      else -> width.toDouble()
    }
    val constrainedHeight = when (heightMode) {
      MeasureMode.Undefined -> UIViewNoIntrinsicMetric
      else -> height.toDouble()
    }

    // The default implementation of sizeThatFits: returns the existing size of
    // the view. That means that if we want to layout an empty UIView, which
    // already has a frame set, its measured size should be CGSizeZero, but
    // UIKit returns the existing size. See https://github.com/facebook/yoga/issues/606
    // for more information.
    val sizeThatFits = if (view.isMemberOfClass(UIView.`class`()) && view.typedSubviews.isEmpty()) {
      Size(0f, 0f)
    } else {
      view.sizeThatFits(CGSizeMake(constrainedWidth, constrainedHeight)).toSize()
    }

    return Size(
      width = YGSanitizeMeasurement(constrainedWidth, sizeThatFits.width, widthMode),
      height = YGSanitizeMeasurement(constrainedHeight, sizeThatFits.height, heightMode),
    )
  }
}

private fun YGSanitizeMeasurement(
  constrainedSize: Double,
  measuredSize: Float,
  measureMode: MeasureMode,
): Float = when (measureMode) {
  MeasureMode.Exactly -> constrainedSize.toFloat()
  MeasureMode.AtMost -> measuredSize
  MeasureMode.Undefined -> measuredSize
  else -> throw AssertionError()
}

private fun YGApplyLayoutToViewHierarchy(node: Node) {
  val x = node.left.toDouble()
  val y = node.top.toDouble()
  val width = node.width.toDouble()
  val height = node.height.toDouble()
  node.view!!.setFrame(CGRectMake(x, y, width, height))

  for (childNode in node.children) {
    YGApplyLayoutToViewHierarchy(childNode)
  }
}

private fun UIView.asNode(): Node {
  val childNode = Node()
  childNode.measureCallback = ViewMeasureCallback(this)
  return childNode
}

private fun CValue<CGSize>.toSize() = useContents {
  Size(width.toFloat(), height.toFloat())
}

private fun Size.toCGSize() = CGSizeMake(width.toDouble(), height.toDouble())

private val Node.view: UIView?
  get() = (measureCallback as ViewMeasureCallback?)?.view
