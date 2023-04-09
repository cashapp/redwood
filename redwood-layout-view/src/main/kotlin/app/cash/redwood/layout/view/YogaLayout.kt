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
import app.cash.redwood.yoga.GlobalMembers
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.YGSize
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
  val rootNode = GlobalMembers.YGNodeNew()

  init {
    rootNode.setMeasureFunc(ViewMeasureFunction(this))
  }

  override fun addView(child: View, index: Int, params: LayoutParams) {
    // Nodes with measure functions cannot have children.
    rootNode.setMeasureFunc(null as YGMeasureFunc?)

    super.addView(child, index, params)

    val childNode = GlobalMembers.YGNodeNew()
    childNode.setMeasureFunc(ViewMeasureFunction(child))
    nodes[child] = childNode
    rootNode.insertChild(childNode, rootNode.getChildren().size)
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
    val node = nodes[view] ?: return
    val owner = node.getOwner() ?: return
    val children = owner.getChildren().listIterator()
    while (children.hasNext()) {
      if (children.next() == node) {
        children.remove()
        break
      }
    }
    node.setMeasureFunc(null as YGMeasureFunc?)
    nodes.remove(view)
    if (inLayout) {
      GlobalMembers.YGNodeCalculateLayoutWithContext(
        node = rootNode,
        ownerWidth = Float.NaN,
        ownerHeight = Float.NaN,
        ownerDirection = GlobalMembers.YGNodeStyleGetDirection(rootNode)!!,
        layoutContext = null,
      )
    }
  }

  private fun applyLayoutRecursive(node: YGNode, xOffset: Float, yOffset: Float) {
    val view = (node.getMeasure().noContext as ViewMeasureFunction?)?.view
    if (view != null && view !== this) {
      if (view.visibility == GONE) {
        return
      }
      val left = (xOffset + GlobalMembers.YGNodeLayoutGetLeft(node)).roundToInt()
      val top = (yOffset + GlobalMembers.YGNodeLayoutGetTop(node)).roundToInt()
      view.measure(
        MeasureSpec.makeMeasureSpec(GlobalMembers.YGNodeLayoutGetWidth(node).roundToInt(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(GlobalMembers.YGNodeLayoutGetHeight(node).roundToInt(), MeasureSpec.EXACTLY),
      )
      view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
    }

    val childCount = node.getChildren().size
    for (i in 0 until childCount) {
      applyLayoutRecursive(
        node = node.getChild(i),
        xOffset = xOffset,
        yOffset = yOffset,
      )
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    // Either we are a root of a tree, or this function is called by our owner's onLayout, in which
    // case our r-l and b-t are the size of our node.
    createLayout(
      MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY),
    )
    applyLayoutRecursive(rootNode, 0f, 0f)
  }

  /**
   * This function is mostly unneeded, because Yoga is doing the measuring. Hence we only need to
   * return accurate results if we are the root.
   */
  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    createLayout(widthSpec, heightSpec)
    setMeasuredDimension(
      GlobalMembers.YGNodeLayoutGetWidth(rootNode).roundToInt(),
      GlobalMembers.YGNodeLayoutGetHeight(rootNode).roundToInt(),
    )
  }

  private fun createLayout(widthSpec: Int, heightSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthSpec).toFloat()
    when (MeasureSpec.getMode(widthSpec)) {
      MeasureSpec.EXACTLY -> GlobalMembers.YGNodeStyleSetWidth(rootNode, widthSize)
      MeasureSpec.AT_MOST -> GlobalMembers.YGNodeStyleSetMaxWidth(rootNode, widthSize)
      MeasureSpec.UNSPECIFIED -> {}
    }
    val heightSize = MeasureSpec.getSize(heightSpec).toFloat()
    when (MeasureSpec.getMode(heightSpec)) {
      MeasureSpec.EXACTLY -> GlobalMembers.YGNodeStyleSetHeight(rootNode, heightSize)
      MeasureSpec.AT_MOST -> GlobalMembers.YGNodeStyleSetMaxHeight(rootNode, heightSize)
      MeasureSpec.UNSPECIFIED -> {}
    }
    GlobalMembers.YGNodeCalculateLayoutWithContext(
      node = rootNode,
      ownerWidth = Float.NaN,
      ownerHeight = Float.NaN,
      ownerDirection = GlobalMembers.YGNodeStyleGetDirection(rootNode)!!,
      layoutContext = null,
    )
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
    val widthSpec = MeasureSpec.makeMeasureSpec(width.toInt(), widthMode.toAndroid())
    val heightSpec = MeasureSpec.makeMeasureSpec(height.toInt(), heightMode.toAndroid())
    view.measure(widthSpec, heightSpec)
    return YGSize(view.measuredWidth, view.measuredHeight)
  }
}
