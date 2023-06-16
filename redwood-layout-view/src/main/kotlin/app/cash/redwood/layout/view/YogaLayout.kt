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
import androidx.collection.SimpleArrayMap
import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlin.math.roundToInt

internal class YogaLayout(context: Context) : ViewGroup(context) {
  private val nodes = SimpleArrayMap<View, Node>()
  val rootNode = Node()

  var applyModifier: (Node, Int) -> Unit = { _, _ -> }

  init {
    applyLayoutParams(rootNode, layoutParams)
  }

  override fun addView(child: View, index: Int, params: LayoutParams) {
    super.addView(child, index, params)

    val childNode = child.asNode()
    nodes.put(child, childNode)
    rootNode.children += childNode
  }

  override fun removeView(view: View) {
    removeViewFromYogaTree(view)
    super.removeView(view)
  }

  override fun removeViewAt(index: Int) {
    removeViewFromYogaTree(getChildAt(index))
    super.removeViewAt(index)
  }

  override fun removeViewInLayout(view: View) {
    removeViewFromYogaTree(view)
    super.removeViewInLayout(view)
  }

  override fun removeViews(start: Int, count: Int) {
    for (i in start until start + count) {
      removeViewFromYogaTree(getChildAt(i))
    }
    super.removeViews(start, count)
  }

  override fun removeViewsInLayout(start: Int, count: Int) {
    for (i in start until start + count) {
      removeViewFromYogaTree(getChildAt(i))
    }
    super.removeViewsInLayout(start, count)
  }

  override fun removeAllViews() {
    val childCount = childCount
    for (i in 0 until childCount) {
      removeViewFromYogaTree(getChildAt(i))
    }
    super.removeAllViews()
  }

  override fun removeAllViewsInLayout() {
    val childCount = childCount
    for (i in 0 until childCount) {
      removeViewFromYogaTree(getChildAt(i))
    }
    super.removeAllViewsInLayout()
  }

  private fun removeViewFromYogaTree(view: View) {
    val childNode = nodes.remove(view) ?: return
    val owner = childNode.owner ?: return
    owner.children -= childNode
  }

  private fun applyLayout(node: Node, xOffset: Float, yOffset: Float) {
    val view = node.view
    if (view != null && view !== this) {
      if (view.visibility == GONE) return

      val width = node.width.roundToInt()
      val height = node.height.roundToInt()
      val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
      val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
      view.measure(widthSpec, heightSpec)

      val left = (xOffset + node.left).roundToInt()
      val top = (yOffset + node.top).roundToInt()
      val right = left + view.measuredWidth
      val bottom = top + view.measuredHeight
      view.layout(left, top, right, bottom)
    }

    if (view === this) {
      for (child in node.children) {
        applyLayout(child, xOffset, yOffset)
      }
    } else if (view !is YogaLayout) {
      for (child in node.children) {
        val left = xOffset + node.left
        val top = yOffset + node.top
        applyLayout(child, left, top)
      }
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    val widthSpec = MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY)
    val heightSpec = MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY)
    calculateLayout(widthSpec, heightSpec)
    applyLayout(rootNode, 0f, 0f)
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
    for ((index, node) in rootNode.children.withIndex()) {
      applyModifier(node, index)
    }

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
    childNode.measureCallback = ViewMeasureCallback(this)
    applyLayoutParams(childNode, layoutParams)
    return childNode
  }
}

private class ViewMeasureCallback(val view: View) : MeasureCallback {
  override fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size {
    val safeWidth = if (width.isFinite()) width.roundToInt() else 0
    val safeHeight = if (height.isFinite()) height.roundToInt() else 0
    val widthSpec = MeasureSpec.makeMeasureSpec(safeWidth, widthMode.toAndroid())
    val heightSpec = MeasureSpec.makeMeasureSpec(safeHeight, heightMode.toAndroid())
    view.measure(widthSpec, heightSpec)
    return Size(view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
  }
}

private val Node.view: View?
  get() = (measureCallback as ViewMeasureCallback?)?.view
