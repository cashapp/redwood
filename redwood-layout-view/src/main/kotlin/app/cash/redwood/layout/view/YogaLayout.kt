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
import app.cash.redwood.Modifier
import app.cash.redwood.ui.Density
import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import app.cash.redwood.yoga.measure
import kotlin.math.roundToInt

internal class YogaLayout(context: Context) : ViewGroup(context) {
  private val nodes = mutableMapOf<View, Node>()
  val rootNode = Node()

  var density: Density = Density(1.0)
  var getModifier: (Int) -> Modifier = { Modifier }

  init {
    rootNode.measureCallback = ViewMeasureFunction(this)
    applyLayoutParams(rootNode, layoutParams)
  }

  override fun addView(child: View, index: Int, params: LayoutParams) {
    // Nodes with children cannot have measure functions.
    rootNode.measureCallback = null

    super.addView(child, index, params)

    val childNode = child.asNode()
    nodes[child] = childNode
    rootNode.children += childNode
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
    val owner = childNode.owner ?: return

    owner.children -= childNode
    nodes -= view

    if (inLayout) {
      for ((index, node) in rootNode.children.withIndex()) {
        node.applyModifier(getModifier(index), density)
      }

      rootNode.measure(Size.Undefined, Size.Undefined)
    }
  }

  private fun applyLayoutRecursive(node: Node, xOffset: Float, yOffset: Float) {
    val view = (node.measureCallback as ViewMeasureFunction?)?.view
    if (view != null && view !== this) {
      if (view.visibility == GONE) return

      val left = (xOffset + node.left).roundToInt()
      val top = (yOffset + node.top).roundToInt()
      val width = node.width.roundToInt()
      val height = node.height.roundToInt()
      val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
      val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
      view.measure(widthSpec, heightSpec)
      val right = left + view.measuredWidth
      val bottom = top + view.measuredHeight
      view.layout(left, top, right, bottom)
    }

    if (view === this) {
      for (child in node.children) {
        applyLayoutRecursive(child, xOffset, yOffset)
      }
    } else if (view !is YogaLayout) {
      for (child in node.children) {
        applyLayoutRecursive(
          node = child,
          xOffset = xOffset + node.left,
          yOffset = yOffset + node.top,
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
    val width = rootNode.width.roundToInt()
    val height = rootNode.height.roundToInt()
    setMeasuredDimension(width, height)
  }

  private fun applyLayoutParams(node: Node, layoutParams: LayoutParams?) {
    if (layoutParams != null) {
      val width = layoutParams.width
      if (width >= 0) {
        node.requestedWidth = width.toFloat()
      }
      val height = layoutParams.height
      if (height >= 0) {
        node.requestedHeight = height.toFloat()
      }
    }
  }

  private fun calculateLayout(widthSpec: Int, heightSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthSpec).toFloat()
    when (MeasureSpec.getMode(widthSpec)) {
      MeasureSpec.EXACTLY -> rootNode.requestedWidth = widthSize
      MeasureSpec.AT_MOST -> rootNode.requestedMaxWidth = widthSize
      MeasureSpec.UNSPECIFIED -> {}
    }
    val heightSize = MeasureSpec.getSize(heightSpec).toFloat()
    when (MeasureSpec.getMode(heightSpec)) {
      MeasureSpec.EXACTLY -> rootNode.requestedHeight = heightSize
      MeasureSpec.AT_MOST -> rootNode.requestedMaxHeight = heightSize
      MeasureSpec.UNSPECIFIED -> {}
    }
    rootNode.measure(Size.Undefined, Size.Undefined)
  }

  private fun View.asNode(): Node {
    val childNode = Node()
    childNode.measureCallback = ViewMeasureFunction(this)
    applyLayoutParams(childNode, layoutParams)
    return childNode
  }
}

private class ViewMeasureFunction(val view: View) : MeasureCallback {
  override fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size {
    val widthSpec = MeasureSpec.makeMeasureSpec(width.roundToInt(), widthMode.toAndroid())
    val heightSpec = MeasureSpec.makeMeasureSpec(height.roundToInt(), heightMode.toAndroid())
    view.measure(widthSpec, heightSpec)
    return Size(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
  }
}
