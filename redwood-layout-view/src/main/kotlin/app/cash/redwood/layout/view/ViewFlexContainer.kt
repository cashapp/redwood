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
import app.cash.redwood.flexbox.isVertical
import app.cash.redwood.layout.Grow
import app.cash.redwood.layout.HorizontalAlignment
import app.cash.redwood.layout.Shrink
import app.cash.redwood.layout.VerticalAlignment
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.yoga.Yoga
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGOverflow

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val density = DensityMultiplier * context.resources.displayMetrics.density
  private val yogaLayout = YogaLayout(context).apply {
    Yoga.YGNodeStyleSetFlexDirection(rootNode, direction.toYoga())
  }

  private val hostView = HostView()
  override val value: View get() = hostView

  override val children = ViewGroupChildren(
    parent = yogaLayout,
    onLayoutModifierUpdated = { widget ->
      val childNode = yogaLayout.viewToNode(widget.value) ?: return@ViewGroupChildren

      widget.layoutModifiers.forEach { modifier ->
        when (modifier) {
          is Grow -> {
            Yoga.YGNodeStyleSetFlexGrow(childNode, modifier.value.toFloat())
          }
          is Shrink -> {
            Yoga.YGNodeStyleSetFlexShrink(childNode, modifier.value.toFloat())
          }
          is app.cash.redwood.layout.Margin -> {
            Yoga.YGNodeStyleSetMargin(
              node = childNode,
              edge = YGEdge.YGEdgeLeft,
              points = density * modifier.margin.left,
            )
            Yoga.YGNodeStyleSetMargin(
              node = childNode,
              edge = YGEdge.YGEdgeRight,
              points = density * modifier.margin.right,
            )
            Yoga.YGNodeStyleSetMargin(
              node = childNode,
              edge = YGEdge.YGEdgeTop,
              points = density * modifier.margin.top,
            )
            Yoga.YGNodeStyleSetMargin(
              node = childNode,
              edge = YGEdge.YGEdgeBottom,
              points = density * modifier.margin.bottom,
            )
          }
          is HorizontalAlignment -> if (direction.isVertical) {
            Yoga.YGNodeStyleSetAlignSelf(childNode, modifier.alignment.toYoga())
          }
          is VerticalAlignment -> if (direction.isHorizontal) {
            Yoga.YGNodeStyleSetAlignSelf(childNode, modifier.alignment.toYoga())
          }
        }
      }
    }
  )
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
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeLeft,
      points = density * margin.left,
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeRight,
      points = density * margin.right,
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeTop,
      points = density * margin.top,
    )
    Yoga.YGNodeStyleSetPadding(
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
    Yoga.YGNodeStyleSetAlignItems(yogaLayout.rootNode, alignItems.toYoga())
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    Yoga.YGNodeStyleSetJustifyContent(yogaLayout.rootNode, justifyContent.toYoga())
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
        Yoga.YGNodeStyleSetOverflow(
          yogaLayout.rootNode,
          YGOverflow.YGOverflowScroll,
        )
        addView(newScrollView().apply { addView(yogaLayout) })
      } else {
        Yoga.YGNodeStyleSetOverflow(
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
