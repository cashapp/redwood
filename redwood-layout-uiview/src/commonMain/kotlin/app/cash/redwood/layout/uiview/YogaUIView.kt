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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.YGSize
import app.cash.redwood.yoga.Yoga
import app.cash.redwood.yoga.Yoga.YGNodeLayoutGetHeight
import app.cash.redwood.yoga.Yoga.YGNodeLayoutGetLeft
import app.cash.redwood.yoga.Yoga.YGNodeLayoutGetTop
import app.cash.redwood.yoga.Yoga.YGNodeLayoutGetWidth
import app.cash.redwood.yoga.Yoga.YGUndefined
import app.cash.redwood.yoga.enums.YGMeasureMode
import app.cash.redwood.yoga.enums.YGMeasureMode.YGMeasureModeAtMost
import app.cash.redwood.yoga.enums.YGMeasureMode.YGMeasureModeExactly
import app.cash.redwood.yoga.enums.YGMeasureMode.YGMeasureModeUndefined
import app.cash.redwood.yoga.interfaces.YGMeasureFunc
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
  val rootNode = Yoga.YGNodeNew()

  var width = Constraint.Wrap
  var height = Constraint.Wrap

  var density: Density = Density.Default
  var getModifier: (Int) -> Modifier = { Modifier }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayoutWithSize(YGUnidentifiedSize).toCGSize()
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    val bounds = size.useContents { sizeToBounds(this) }
    val output = calculateLayoutWithSize(bounds)
//    println("sizeThatFits INPUT: [$width $height] OUTPUT: [${output.width} ${output.height}] ${rootNode.children.size} ${subviews.size}")
    return output.toCGSize()
  }

  override fun layoutSubviews() {
    val bounds = bounds.useContents { sizeToBounds(size) }
    calculateLayoutWithSize(bounds)

    for (childNode in rootNode.children) {
      YGApplyLayoutToViewHierarchy(childNode)
    }
  }

  private fun calculateLayoutWithSize(size: YGSize): YGSize {
    // TODO: Figure out how to measure incrementally safely.
    rootNode.markDirtyAndPropogateDownwards()

    YGAttachNodesFromViewHierachy(this)

    for ((index, node) in rootNode.children.withIndex()) {
      node.applyModifier(getModifier(index), density)
    }

    Yoga.YGNodeCalculateLayout(
      node = rootNode,
      ownerWidth = size.width,
      ownerHeight = size.height,
      ownerDirection = rootNode.style.direction(),
    )
    return YGSize(
      width = YGNodeLayoutGetWidth(rootNode),
      height = YGNodeLayoutGetHeight(rootNode),
    )
  }

  private fun sizeToBounds(size: CGSize): YGSize {
    return YGSize(
      width = sizeToBoundsDimension(width, size.width),
      height = sizeToBoundsDimension(height, size.height),
    )
  }

  private fun sizeToBoundsDimension(constraint: Constraint, dimension: Double): Float {
    if (constraint == Constraint.Wrap || dimension == UIViewNoIntrinsicMetric) {
      return YGUndefined
    } else {
      return dimension.toFloat()
    }
  }
}

private fun YGAttachNodesFromViewHierachy(yoga: YogaUIView) {
  if (yoga.typedSubviews.isEmpty()) {
    yoga.rootNode.removeAllChildren()
    yoga.rootNode.setMeasureFunc(ViewMeasureFunction(yoga))
    return
  }

  // Nodes with children cannot have measure functions.
  yoga.rootNode.setMeasureFunc(null as YGMeasureFunc?)

  val currentViews = yoga.rootNode.children.mapNotNull { it.view }
  val subviews = yoga.typedSubviews
  if (currentViews != subviews) {
//    println("YGAttachNodesFromViewHierachy - ${subviews.size}")
    yoga.rootNode.removeAllChildren()
    for (view in subviews) {
      Yoga.YGNodeAddChild(yoga.rootNode, view.asNode())
    }
  } else {
//    println("YGAttachNodesFromViewHierachy - currentViews == subviews")
  }
}

private class ViewMeasureFunction(val view: UIView) : YGMeasureFunc {
  override fun invoke(
    node: YGNode,
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
  ): YGSize {
    val view = node.view!!
//    if (view is YogaUIView) {
//      println("ViewMeasureFunction ${view.flexDirection} $width $widthMode $height $heightMode")
//    } else {
//      println("ViewMeasureFunction CHILD $width $widthMode $height $heightMode")
//    }

    val constrainedWidth = when (widthMode) {
      YGMeasureModeUndefined -> UIViewNoIntrinsicMetric
      else -> width.toDouble()
    }
    val constrainedHeight = when (heightMode) {
      YGMeasureModeUndefined -> UIViewNoIntrinsicMetric
      else -> height.toDouble()
    }

    // The default implementation of sizeThatFits: returns the existing size of
    // the view. That means that if we want to layout an empty UIView, which
    // already has a frame set, its measured size should be CGSizeZero, but
    // UIKit returns the existing size. See https://github.com/facebook/yoga/issues/606
    // for more information.
    val sizeThatFits = if (view.isMemberOfClass(UIView.`class`()) && view.typedSubviews.isEmpty()) {
      YGZeroSize
    } else {
      view.sizeThatFits(CGSizeMake(constrainedWidth, constrainedHeight)).toYGSize()
    }

    return YGSize(
      width = YGSanitizeMeasurement(constrainedWidth, sizeThatFits.width, widthMode),
      height = YGSanitizeMeasurement(constrainedHeight, sizeThatFits.height, heightMode),
    )
  }
}

private fun YGSanitizeMeasurement(
  constrainedSize: Double,
  measuredSize: Float,
  measureMode: YGMeasureMode,
): Float = when (measureMode) {
  YGMeasureModeExactly -> constrainedSize.toFloat()
  YGMeasureModeAtMost -> measuredSize
  YGMeasureModeUndefined -> measuredSize
}

private fun YGApplyLayoutToViewHierarchy(node: YGNode) {
  val left = YGNodeLayoutGetLeft(node)
  val top = YGNodeLayoutGetTop(node)

  val x = left.toDouble()
  val y = top.toDouble()
  val width = YGNodeLayoutGetWidth(node).toDouble()
  val height = YGNodeLayoutGetHeight(node).toDouble()
  node.view!!.setFrame(CGRectMake(x, y, width, height))
//  println("YGApplyLayoutToViewHierarchy ${node.style.flexDirection()} $x $y $width $height")

  for (childNode in node.children) {
    YGApplyLayoutToViewHierarchy(childNode)
  }
}

private fun UIView.asNode(): YGNode {
  val childNode = Yoga.YGNodeNew()
  childNode.setMeasureFunc(ViewMeasureFunction(this))
  return childNode
}

private fun CValue<CGSize>.toYGSize() = useContents { YGSize(width.toFloat(), height.toFloat()) }

private fun YGSize.toCGSize() = CGSizeMake(width.toDouble(), height.toDouble())

private val YGNode.view: UIView?
  get() = (measure.noContext as ViewMeasureFunction?)?.view

private val YGUnidentifiedSize = YGSize(YGUndefined, YGUndefined)

private val YGZeroSize = YGSize(0f, 0f)
