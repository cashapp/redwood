/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.yoga.YogaConstants
import app.cash.redwood.yoga.YogaMeasureOutput.make
import app.cash.redwood.yoga.YogaNode
import app.cash.redwood.yoga.YogaNodeFactory
import app.cash.redwood.yoga.enums.YogaMeasureMode
import app.cash.redwood.yoga.interfaces.YogaMeasureFunction
import app.cash.redwood.yoga.internal.YGSize
import kotlin.math.roundToInt

/**
 * A `ViewGroup` based on the Yoga layout engine.
 *
 * Under the hood, all views added to this `ViewGroup` are laid out using flexbox rules
 * and the Yoga engine.
 */
internal class YogaLayout(context: Context) : ViewGroup(context) {
  private val nodes = mutableMapOf<View, YogaNode>()
  val rootNode = YogaNodeFactory.create()

  init {
    rootNode.setData(this)
    rootNode.setMeasureFunction(ViewMeasureFunction())
  }

  override fun addView(child: View, index: Int, params: LayoutParams) {
    // Nodes with measure functions cannot have children.
    rootNode.setMeasureFunction(null)

    super.addView(child, index, params)

    val childNode: YogaNode
    if (child is YogaLayout) {
      childNode = child.rootNode
    } else {
      childNode = YogaNodeFactory.create()
      childNode.setData(child)
      childNode.setMeasureFunction(ViewMeasureFunction())
    }
    nodes[child] = childNode
    rootNode.addChildAt(childNode, rootNode.getChildCount())
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
    for (i in 0 until owner.getChildCount()) {
      if (owner.getChildAt(i) == node) {
        owner.removeChildAt(i)
        break
      }
    }
    node.setData(null)
    nodes.remove(view)
    if (inLayout) {
      rootNode.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
    }
  }

  private fun applyLayoutRecursive(node: YogaNode, xOffset: Float, yOffset: Float) {
    val view = node.getData() as View?
    if (view != null && view !== this) {
      if (view.visibility == GONE) {
        return
      }
      val left = (xOffset + node.getLayoutX()).roundToInt()
      val top = (yOffset + node.getLayoutY()).roundToInt()
      view.measure(
        MeasureSpec.makeMeasureSpec(
          node.getLayoutWidth().roundToInt(),
          MeasureSpec.EXACTLY,
        ),
        MeasureSpec.makeMeasureSpec(
          node.getLayoutHeight().roundToInt(),
          MeasureSpec.EXACTLY,
        ),
      )
      view.layout(left, top, left + view.measuredWidth, top + view.measuredHeight)
    }

    val childCount = node.getChildCount()
    for (i in 0 until childCount) {
      when (view) {
        this -> applyLayoutRecursive(
          node = node.getChildAt(i),
          xOffset = xOffset,
          yOffset = yOffset,
        )
        !is YogaLayout -> applyLayoutRecursive(
          node = node.getChildAt(i),
          xOffset = xOffset + node.getLayoutX(),
          yOffset = yOffset + node.getLayoutY(),
        )
      }
    }
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    // Either we are a root of a tree, or this function is called by our owner's onLayout, in which
    // case our r-l and b-t are the size of our node.
    if (parent !is YogaLayout) {
      createLayout(
        MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(b - t, MeasureSpec.EXACTLY),
      )
    }
    applyLayoutRecursive(rootNode, 0f, 0f)
  }

  /**
   * This function is mostly unneeded, because Yoga is doing the measuring. Hence we only need to
   * return accurate results if we are the root.
   */
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    if (parent !is YogaLayout) {
      createLayout(widthMeasureSpec, heightMeasureSpec)
    }
    setMeasuredDimension(
      rootNode.getLayoutWidth().roundToInt(),
      rootNode.getLayoutHeight().roundToInt(),
    )
  }

  private fun createLayout(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    val heightSize = MeasureSpec.getSize(heightMeasureSpec)
    val widthMode = MeasureSpec.getMode(widthMeasureSpec)
    val heightMode = MeasureSpec.getMode(heightMeasureSpec)
    if (heightMode == MeasureSpec.EXACTLY) {
      rootNode.setHeight(heightSize.toFloat())
    }
    if (widthMode == MeasureSpec.EXACTLY) {
      rootNode.setWidth(widthSize.toFloat())
    }
    if (heightMode == MeasureSpec.AT_MOST) {
      rootNode.setMaxHeight(heightSize.toFloat())
    }
    if (widthMode == MeasureSpec.AT_MOST) {
      rootNode.setMaxWidth(widthSize.toFloat())
    }
    rootNode.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
  }
}

/**
 * Wrapper around measure function for yoga leaves.
 */
private class ViewMeasureFunction : YogaMeasureFunction {
  /**
   * A function to measure leaves of the Yoga tree. Yoga needs some way to know how large
   * elements want to be. This function passes that question directly through to the relevant
   * `View`'s measure function.
   */
  override fun measure(
    node: YogaNode,
    width: Float,
    widthMode: YogaMeasureMode,
    height: Float,
    heightMode: YogaMeasureMode,
  ): YGSize {
    val view = node.getData() as View?
    if (view == null || view is YogaLayout) {
      return make(0, 0)
    }
    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
      width.toInt(),
      widthMode.toAndroid(),
    )
    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
      height.toInt(),
      heightMode.toAndroid(),
    )
    view.measure(widthMeasureSpec, heightMeasureSpec)
    return make(view.measuredWidth, view.measuredHeight)
  }
}
