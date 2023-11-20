/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
internal class YogaLayout(
  context: Context,
  private val applyModifier: (Node, Int) -> Unit,
) : ViewGroup(context) {
  val rootNode = Node()

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

  private fun calculateLayout(widthSpec: Int, heightSpec: Int) {
    rootNode.requestedWidth = Size.Undefined
    rootNode.requestedMaxWidth = Size.Undefined
    rootNode.requestedHeight = Size.Undefined
    rootNode.requestedMaxHeight = Size.Undefined

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

    // This must be applied immediately before measure.
    for ((index, node) in rootNode.children.withIndex()) {
      applyModifier(node, index)
    }

    rootNode.measure(Size.Undefined, Size.Undefined)
  }
}

private val Node.view: View?
  get() = (measureCallback as ViewMeasureCallback?)?.view
