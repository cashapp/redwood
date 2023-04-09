/*
 * Copyright (C) 2022 Square, Inc.
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
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.core.widget.NestedScrollView
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.yoga.GlobalMembers
import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGJustify
import app.cash.redwood.yoga.enums.YGOverflow

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val density = DensityMultiplier * context.resources.displayMetrics.density
  private val yogaLayout = YogaLayout(context).apply {
    GlobalMembers.YGNodeStyleSetFlexDirection(
      node = rootNode,
      flexDirection = when (direction) {
        FlexDirection.Row -> YGFlexDirection.YGFlexDirectionRow
        FlexDirection.RowReverse -> YGFlexDirection.YGFlexDirectionRowReverse
        FlexDirection.Column -> YGFlexDirection.YGFlexDirectionColumn
        FlexDirection.ColumnReverse -> YGFlexDirection.YGFlexDirectionColumnReverse
        else -> throw AssertionError()
      },
    )
  }
  private val hostView = HostView()
  override val value: View get() = yogaLayout

  override val children = ViewGroupChildren(yogaLayout)
  override var layoutModifiers: LayoutModifier = LayoutModifier

  private var width = Constraint.Wrap
  private var height = Constraint.Wrap

  override fun width(width: Constraint) {
    this.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    this.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    GlobalMembers.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeLeft,
      points = density * margin.left,
    )
    GlobalMembers.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeRight,
      points = density * margin.right,
    )
    GlobalMembers.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeTop,
      points = density * margin.top,
    )
    GlobalMembers.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeBottom,
      points = density * margin.bottom,
    )
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    hostView.scrollEnabled = when (overflow) {
      Overflow.Clip -> false
      Overflow.Scroll -> true
      else -> throw AssertionError()
    }
    invalidate()
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignItems(horizontalAlignment.toAlignItems())
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    justifyContent(verticalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignItems(verticalAlignment.toAlignItems())
  }

  fun alignItems(alignItems: AlignItems) {
    GlobalMembers.YGNodeStyleSetAlignItems(
      node = yogaLayout.rootNode,
      alignItems = when (alignItems) {
        AlignItems.FlexStart -> YGAlign.YGAlignFlexStart
        AlignItems.FlexEnd -> YGAlign.YGAlignFlexEnd
        AlignItems.Center -> YGAlign.YGAlignCenter
        AlignItems.Baseline -> YGAlign.YGAlignBaseline
        AlignItems.Stretch -> YGAlign.YGAlignStretch
        else -> throw AssertionError()
      },
    )
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    GlobalMembers.YGNodeStyleSetJustifyContent(
      node = yogaLayout.rootNode,
      justifyContent = when (justifyContent) {
        JustifyContent.FlexStart -> YGJustify.YGJustifyFlexStart
        JustifyContent.FlexEnd -> YGJustify.YGJustifyFlexEnd
        JustifyContent.Center -> YGJustify.YGJustifyCenter
        JustifyContent.SpaceBetween -> YGJustify.YGJustifySpaceBetween
        JustifyContent.SpaceAround -> YGJustify.YGJustifySpaceAround
        JustifyContent.SpaceEvenly -> YGJustify.YGJustifySpaceEvenly
        else -> throw AssertionError()
      },
    )
    invalidate()
  }

  private fun invalidate() {
    hostView.invalidate()
    hostView.requestLayout()
  }

  private inner class HostView : FrameLayout(context) {
    var scrollEnabled = false
      set(new) {
        val old = field
        field = new
        if (old != new) {
          updateViewHierarchy()
        }
      }

    init {
      updateViewHierarchy()
    }

    private fun updateViewHierarchy() {
      removeAllViews()
      (yogaLayout.parent as ViewGroup?)?.removeView(yogaLayout)

      if (scrollEnabled) {
        GlobalMembers.YGNodeStyleSetOverflow(
          yogaLayout.rootNode,
          YGOverflow.YGOverflowScroll,
        )
        addView(newScrollView().apply { addView(yogaLayout) })
      } else {
        GlobalMembers.YGNodeStyleSetOverflow(
          yogaLayout.rootNode,
          YGOverflow.YGOverflowVisible,
        )
        addView(yogaLayout)
      }
    }

    private fun newScrollView(): ViewGroup {
      return if (direction.isHorizontal) {
        HorizontalScrollView(context).apply {
          isFillViewport = true
        }
      } else {
        NestedScrollView(context).apply {
          isFillViewport = true
        }
      }.apply {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
      }
    }
  }
}
