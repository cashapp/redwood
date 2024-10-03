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
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
internal class YogaLayout(context: Context) : ViewGroup(context) {
  val rootNode = Node()
    .apply {
      this.context = this@YogaLayout
    }

  internal var widthConstraint = Constraint.Wrap
  internal var heightConstraint = Constraint.Wrap

  private fun applyLayout(node: Node, xOffset: Float, yOffset: Float) {
    val view = node.context as View
    if (view !== this) {
      if (view.visibility == GONE) return

      val left = (xOffset + node.left).roundToInt()
      val top = (yOffset + node.top).roundToInt()
      val right = left + node.width.roundToInt()
      val bottom = top + node.height.roundToInt()

      // We already know how big we want this view to be. But we measure it to trigger side-effects
      // that the view needs to render itself correctly. In particular, `TextView` needs this
      // otherwise it won't apply gravity correctly.
      val width = right - left
      val height = bottom - top
      if (width != view.measuredWidth || height != view.measuredHeight) {
        view.measure(
          MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
      }
      view.layout(left, top, right, bottom)
    }

    for (child in node.children) {
      applyLayout(
        node = child,
        xOffset = xOffset + node.left,
        yOffset = yOffset + node.top,
      )
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    calculateLayout(
      requestedWidth = (right - left).toFloat(),
      requestedHeight = (bottom - top).toFloat(),
    )
    applyLayout(rootNode, left.toFloat(), top.toFloat())
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthSpec)
    val widthMode = MeasureSpec.getMode(widthSpec)
    val heightSize = MeasureSpec.getSize(heightSpec)
    val heightMode = MeasureSpec.getMode(heightSpec)

    calculateLayout(
      requestedWidth = when {
        widthMode == MeasureSpec.EXACTLY -> widthSize.toFloat()
        widthConstraint == Constraint.Fill -> widthSize.toFloat()
        else -> Size.UNDEFINED
      },
      requestedHeight = when {
        heightMode == MeasureSpec.EXACTLY -> heightSize.toFloat()
        heightConstraint == Constraint.Fill -> heightSize.toFloat()
        else -> Size.UNDEFINED
      },
    )

    val width = rootNode.width.roundToInt()
    val height = rootNode.height.roundToInt()
    setMeasuredDimension(width, height)
  }

  private fun calculateLayout(
    requestedWidth: Float,
    requestedHeight: Float,
  ) {
    rootNode.requestedWidth = requestedWidth
    rootNode.requestedMaxWidth = Size.UNDEFINED
    rootNode.requestedHeight = requestedHeight
    rootNode.requestedMaxHeight = Size.UNDEFINED

    // Sync widget layout requests to the Yoga node tree.
    for (node in rootNode.children) {
      if ((node.context as View).isLayoutRequested) {
        node.markDirty()
      }
    }

    rootNode.measureOnly(Size.UNDEFINED, Size.UNDEFINED)
  }
}
