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
package app.cash.redwood.layout.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.layout.view.YogaLayout.ViewMeasureFunction
import app.cash.redwood.yoga.YogaNode
import app.cash.redwood.yoga.YogaNodeFactory.create
import java.util.LinkedList

/**
 * Much like a [YogaLayout], except this class does not render itself (the container) to the
 * screen.  As a result, *do not use this if you wish the container to have a background or
 * foreground*.  However, all of its children will still render as expected.
 *
 * In practice, this class never added to the View tree, and all its children become children of its
 * parent.  As a result, all the layout (such as the traversal of the tree) is performed by Yoga
 * (and so natively) increasing performance.
 */
internal class VirtualYogaLayout : ViewGroup {
  private val mChildren: MutableList<View> = LinkedList()
  private val mYogaNodes: MutableMap<View, YogaNode?> = HashMap()
  val yogaNode = create()

  constructor(context: Context) : super(context)

  @JvmOverloads
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(
    context,
    attrs,
    defStyleAttr,
  ) {
    val lp = YogaLayout.LayoutParams(context, attrs)
    YogaLayout.applyLayoutParams(lp, yogaNode, this)
  }

  /**
   * Called to add a view, creating a new yoga node for it and adding that yoga node to the parent.
   * If the child is a [VirtualYogaLayout], we simply transfer all its children to this one
   * in a manner that maintains the tree, and add its root to the tree.
   *
   * @param child the View to add
   * @param index the position at which to add it (ignored)
   * @param params the layout parameters to apply
   */
  override fun addView(child: View, index: Int, params: LayoutParams) {
    if (child is VirtualYogaLayout) {
      child.transferChildren(this)
      val childNode = child.yogaNode
      yogaNode.addChildAt(childNode, yogaNode.getChildCount())
      return
    }
    val node = create()
    val lp = YogaLayout.LayoutParams(params)
    YogaLayout.applyLayoutParams(lp, node, child)
    node.setData(child)
    node.setMeasureFunction(ViewMeasureFunction())
    yogaNode.addChildAt(node, yogaNode.getChildCount())
    addView(child, node)
  }

  /**
   * Called to add a view with a corresponding node, but not to change the Yoga tree in any way.
   *
   * @param child the View to add
   * @param node the corresponding yoga node
   */
  fun addView(child: View, node: YogaNode?) {
    mChildren.add(child)
    mYogaNodes[child] = node
  }

  /**
   * Gives up children `View`s to the parent, maintaining the Yoga tree.  This function calls
   * [YogaLayout.addView] or [VirtualYogaLayout.addView]
   * on the parent to add the `View` without generating new yoga nodes.
   *
   * @param parent the parent to pass children to (must be a YogaLayout or a VirtualYogaLayout)
   */
  fun transferChildren(parent: ViewGroup) {
    if (parent is VirtualYogaLayout) {
      for (child in mChildren) {
        parent.addView(child, mYogaNodes[child])
      }
    } else if (parent is YogaLayout) {
      for (child in mChildren) {
        parent.addView(child, mYogaNodes[child])
      }
    } else {
      throw RuntimeException(
        "VirtualYogaLayout cannot transfer children to ViewGroup of type "
          + parent.javaClass + ".  Must either be a VirtualYogaLayout or a " +
          "YogaLayout.",
      )
    }
    mChildren.clear()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    throw RuntimeException("Attempting to layout a VirtualYogaLayout")
  }

  override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
    return YogaLayout.LayoutParams(context, attrs)
  }

  override fun generateDefaultLayoutParams(): LayoutParams {
    return YogaLayout.LayoutParams(
      LayoutParams.MATCH_PARENT,
      LayoutParams.MATCH_PARENT,
    )
  }

  override fun generateLayoutParams(p: LayoutParams): LayoutParams {
    return YogaLayout.LayoutParams(p)
  }

  override fun checkLayoutParams(p: LayoutParams): Boolean {
    return p is YogaLayout.LayoutParams
  }
}
