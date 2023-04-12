/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.view

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import app.cash.redwood.yoga.Yoga
import app.cash.redwood.yoga.Yoga.YGUndefined
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.YGSize
import app.cash.redwood.yoga.enums.YGDirection
import app.cash.redwood.yoga.enums.YGMeasureMode
import app.cash.redwood.yoga.interfaces.YGMeasureFunc
import kotlin.math.roundToInt

/**
 * A `ViewGroup` based on the Yoga layout engine.
 *
 * Under the hood, all views added to this `ViewGroup` are laid out using flexbox rules
 * and the Yoga engine.
 */
internal class YogaLayout(context: Context) : ViewGroup(context) {
  private val nodes = mutableMapOf<View, YGNode>()
  val rootNode = Yoga.YGNodeNew()

  init {
    rootNode.setMeasureFunc(ViewMeasureFunction(this))
    applyLayoutParams(layoutParams, rootNode, this)
  }

  fun viewToNode(view: View): YGNode? = nodes[view]

  override fun addView(child: View, index: Int, params: LayoutParams) {
    // Nodes with children cannot have measure functions.
    rootNode.setMeasureFunc(null as YGMeasureFunc?)

    super.addView(child, index, params)

    val childNode = child.asNode()
    nodes[child] = childNode
    Yoga.YGNodeAddChild(rootNode, childNode)
  }

  override fun removeView(view: View) {
    removeViewFromYogaTree(view, false)
    super.removeView(view)
  }

  override fun removeViewAt(index: Int) {
    removeViewFromYogaTree(getChildAt(index), false)
    super.removeViewAt(index)
  }

  override fun removeViewInLayout(view: View) {
    removeViewFromYogaTree(view, true)
    super.removeViewInLayout(view)
  }

  override fun removeViews(start: Int, count: Int) {
    for (i in start until start + count) {
      removeViewFromYogaTree(getChildAt(i), false)
    }
    super.removeViews(start, count)
  }

  override fun removeViewsInLayout(start: Int, count: Int) {
    for (i in start until start + count) {
      removeViewFromYogaTree(getChildAt(i), true)
    }
    super.removeViewsInLayout(start, count)
  }

  override fun removeAllViews() {
    val childCount = childCount
    for (i in 0 until childCount) {
      removeViewFromYogaTree(getChildAt(i), false)
    }
    super.removeAllViews()
  }

  override fun removeAllViewsInLayout() {
    val childCount = childCount
    for (i in 0 until childCount) {
      removeViewFromYogaTree(getChildAt(i), true)
    }
    super.removeAllViewsInLayout()
  }

  private fun removeViewFromYogaTree(view: View, inLayout: Boolean) {
    val childNode = nodes[view] ?: return
    val owner = childNode.getOwner() ?: return

    Yoga.YGNodeRemoveChild(owner, childNode)
    nodes -= view

    if (inLayout) {
      Yoga.YGNodeCalculateLayout(
        node = rootNode,
        ownerWidth = YGUndefined,
        ownerHeight = YGUndefined,
        ownerDirection = rootNode.getStyle().direction(),
      )
    }
  }

  private fun applyLayoutRecursive(node: YGNode, xOffset: Float, yOffset: Float) {
    val view = (node.getMeasure().noContext as ViewMeasureFunction?)?.view
    if (view != null && view !== this) {
      if (view.visibility == GONE) return

      val left = (xOffset + Yoga.YGNodeLayoutGetLeft(node)).roundToInt()
      val top = (yOffset + Yoga.YGNodeLayoutGetTop(node)).roundToInt()
      val width = Yoga.YGNodeLayoutGetWidth(node).roundToInt()
      val height = Yoga.YGNodeLayoutGetHeight(node).roundToInt()
      val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
      val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
      view.measure(widthSpec, heightSpec)
      val right = left + view.measuredWidth
      val bottom = top + view.measuredHeight
      view.layout(left, top, right, bottom)
    }

    if (view === this) {
      for (child in node.getChildren()) {
        applyLayoutRecursive(child, xOffset, yOffset)
      }
    } else if (view !is YogaLayout) {
      for (child in node.getChildren()) {
        applyLayoutRecursive(
          node = child,
          xOffset = xOffset + Yoga.YGNodeLayoutGetLeft(node),
          yOffset = yOffset + Yoga.YGNodeLayoutGetTop(node),
        )
      }
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    val widthSpec = MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY)
    val heightSpec = MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY)
    calculateLayout(widthSpec, heightSpec)
    applyLayoutRecursive(rootNode, 0f, 0f)
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    calculateLayout(widthSpec, heightSpec)
    val width = Yoga.YGNodeLayoutGetWidth(rootNode).roundToInt()
    val height = Yoga.YGNodeLayoutGetHeight(rootNode).roundToInt()
    setMeasuredDimension(width, height)
  }

  private fun applyLayoutParams(layoutParams: LayoutParams?, node: YGNode, view: View) {
    if (view.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
      node.setLayoutDirection(YGDirection.YGDirectionRTL)
    }

    if (layoutParams != null) {
      val width = layoutParams.width
      if (width >= 0) {
        Yoga.YGNodeStyleSetWidth(node, width.toFloat())
      }
      val height = layoutParams.height
      if (height >= 0) {
        Yoga.YGNodeStyleSetHeight(node, height.toFloat())
      }
    }
  }

  private fun calculateLayout(widthSpec: Int, heightSpec: Int) {
    // TODO: Figure out how to measure incrementally safely.
    rootNode.markDirtyAndPropogateDownwards()

    val widthSize = MeasureSpec.getSize(widthSpec).toFloat()
    when (MeasureSpec.getMode(widthSpec)) {
      MeasureSpec.EXACTLY -> Yoga.YGNodeStyleSetWidth(rootNode, widthSize)
      MeasureSpec.AT_MOST -> Yoga.YGNodeStyleSetMaxWidth(rootNode, widthSize)
      MeasureSpec.UNSPECIFIED -> {}
    }
    val heightSize = MeasureSpec.getSize(heightSpec).toFloat()
    when (MeasureSpec.getMode(heightSpec)) {
      MeasureSpec.EXACTLY -> Yoga.YGNodeStyleSetHeight(rootNode, heightSize)
      MeasureSpec.AT_MOST -> Yoga.YGNodeStyleSetMaxHeight(rootNode, heightSize)
      MeasureSpec.UNSPECIFIED -> {}
    }
    Yoga.YGNodeCalculateLayout(
      node = rootNode,
      ownerWidth = YGUndefined,
      ownerHeight = YGUndefined,
      ownerDirection = rootNode.getStyle().direction(),
    )
  }

  private fun View.asNode(): YGNode {
    val childNode = Yoga.YGNodeNew()
    childNode.setMeasureFunc(ViewMeasureFunction(this))
    applyLayoutParams(layoutParams, childNode, this)
    return childNode
  }
}

private class ViewMeasureFunction(val view: View) : YGMeasureFunc {
  override fun invoke(
    node: YGNode,
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
  ): YGSize {
    val widthSpec = MeasureSpec.makeMeasureSpec(width.roundToInt(), widthMode.toAndroid())
    val heightSpec = MeasureSpec.makeMeasureSpec(height.roundToInt(), heightMode.toAndroid())
    view.measure(widthSpec, heightSpec)
    return YGSize(view.measuredWidth, view.measuredHeight)
  }
}
