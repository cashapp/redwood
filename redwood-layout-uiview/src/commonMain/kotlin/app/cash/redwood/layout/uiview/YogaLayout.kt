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

import app.cash.redwood.flexbox.Size
import app.cash.redwood.layout.api.Constraint
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
import platform.CoreGraphics.CGPointZero
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaLayout {
  val rootNode = Yoga.YGNodeNew()
  val view: UIView = View()

  var width = Constraint.Wrap
  var height = Constraint.Wrap

  fun applyLayout() {
    YGAttachNodesFromViewHierachy(this)

    val size = view.bounds.useContents {
      val widthBounds = when (width) {
          Constraint.Wrap -> YGUndefined
          else -> size.width.toFloat()
      }
      val heightBounds = when (height) {
          Constraint.Wrap -> YGUndefined
          else -> size.height.toFloat()
      }
      YGSize(widthBounds, heightBounds)
    }
    calculateLayoutWithSize(size)

    YGApplyLayoutToViewHierarchy(rootNode, view)
  }

  private fun calculateLayoutWithSize(size: YGSize): Size {
    // TODO: Figure out how to measure incrementally safely.
    rootNode.markDirtyAndPropogateDownwards()

    Yoga.YGNodeCalculateLayout(
      node = rootNode,
      ownerWidth = size.width,
      ownerHeight = size.height,
      ownerDirection = rootNode.style.direction()
    )
    return Size(
      width = YGNodeLayoutGetWidth(rootNode).toDouble(),
      height = YGNodeLayoutGetHeight(rootNode).toDouble(),
    )
  }

  private inner class View : UIView(cValue { CGRectZero }) {
    override fun intrinsicContentSize(): CValue<CGSize> {
      return calculateLayoutWithSize(YGSize(YGUndefined, YGUndefined)).toCGSize()
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      return size.useContents {
        val width = if (width == UIViewNoIntrinsicMetric) YGUndefined else width.toFloat()
        val height = if (height == UIViewNoIntrinsicMetric) YGUndefined else height.toFloat()
        calculateLayoutWithSize(YGSize(width, height)).toCGSize()
      }
    }

    override fun layoutSubviews() {
      super.layoutSubviews()
      applyLayout()
    }
  }
}

private fun YGAttachNodesFromViewHierachy(yoga: YogaLayout) {
  if (yoga.view.typedSubviews.isEmpty()) {
    yoga.rootNode.removeAllChildren()
    yoga.rootNode.setMeasureFunc(ViewMeasureFunction(yoga.view))
    return
  }

  // Nodes with children cannot have measure functions.
  yoga.rootNode.setMeasureFunc(null as YGMeasureFunc?)

  val currentViews = yoga.rootNode.children.mapNotNull { it.view }
  val subviews = yoga.view.typedSubviews
  if (currentViews != subviews) {
    println("YGAttachNodesFromViewHierachy - ${subviews.size}")
    yoga.rootNode.removeAllChildren()
    for (view in subviews) {
      Yoga.YGNodeAddChild(yoga.rootNode, view.asNode())
    }
  } else {
    println("YGAttachNodesFromViewHierachy - currentViews == subviews")
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
    println("ViewMeasureFunction ${node.view} $width $widthMode $height $heightMode")

    val constrainedWidth = when (widthMode) {
        YGMeasureModeUndefined -> Double.MAX_VALUE
        else -> width.toDouble()
    }
    val constrainedHeight = when (heightMode) {
        YGMeasureModeUndefined -> Double.MAX_VALUE
        else -> height.toDouble()
    }

    // The default implementation of sizeThatFits: returns the existing size of
    // the view. That means that if we want to layout an empty UIView, which
    // already has a frame set, its measured size should be CGSizeZero, but
    // UIKit returns the existing size. See https://github.com/facebook/yoga/issues/606
    // for more information.
    val sizeThatFits = if (view.isMemberOfClass(UIView.`class`()) && view.typedSubviews.isEmpty()) {
      Size.Zero
    } else {
      view.sizeThatFits(CGSizeMake(constrainedWidth, constrainedHeight)).toSize()
    }

    return YGSize(
      width = YGSanitizeMeasurement(constrainedWidth, sizeThatFits.width, widthMode).toFloat(),
      height = YGSanitizeMeasurement(constrainedHeight, sizeThatFits.height, heightMode).toFloat()
    )
  }
}

private fun YGSanitizeMeasurement(
  constrainedSize: Double,
  measuredSize: Double,
  measureMode: YGMeasureMode,
): Double = when (measureMode) {
  YGMeasureModeExactly -> constrainedSize
  YGMeasureModeAtMost -> measuredSize
  YGMeasureModeUndefined -> measuredSize
}

private fun YGApplyLayoutToViewHierarchy(
  node: YGNode,
  view: UIView = node.view!!,
) {
  val left = YGNodeLayoutGetLeft(node)
  val top = YGNodeLayoutGetTop(node)

  val x = left.toDouble()
  val y = top.toDouble()
  val width = YGNodeLayoutGetWidth(node).toDouble()
  val height = YGNodeLayoutGetHeight(node).toDouble()
  view.setFrame(CGRectMake(x, y, width, height))
  println("YGApplyLayoutToViewHierarchy ${node.style.flexDirection()} $x $y $width $height")

  for (childNode in node.children) {
    YGApplyLayoutToViewHierarchy(childNode)
  }
}

private fun UIView.asNode(): YGNode {
  val childNode = Yoga.YGNodeNew()
  childNode.setMeasureFunc(ViewMeasureFunction(this))
  return childNode
}

private fun CValue<CGSize>.toSize() = useContents { Size(width, height) }

private fun Size.toCGSize() = CGSizeMake(width, height)

private val YGNode.view: UIView?
  get() = (measure.noContext as ViewMeasureFunction?)?.view
