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
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.core.view.updateLayoutParams
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
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaFlexDirection
import com.facebook.yoga.YogaJustify
import com.facebook.yoga.YogaOverflow
import com.facebook.yoga.android.YogaLayout

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val density = DensityMultiplier * context.resources.displayMetrics.density
  private val yogaLayout = YogaLayout(context).apply {
    yogaNode.flexDirection = when (direction) {
      FlexDirection.Row -> YogaFlexDirection.ROW
      FlexDirection.RowReverse -> YogaFlexDirection.ROW_REVERSE
      FlexDirection.Column -> YogaFlexDirection.COLUMN
      FlexDirection.ColumnReverse -> YogaFlexDirection.COLUMN_REVERSE
      else -> throw AssertionError()
    }
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
    yogaLayout.yogaNode.setPadding(YogaEdge.LEFT, density * margin.left)
    yogaLayout.yogaNode.setPadding(YogaEdge.RIGHT, density * margin.right)
    yogaLayout.yogaNode.setPadding(YogaEdge.TOP, density * margin.top)
    yogaLayout.yogaNode.setPadding(YogaEdge.BOTTOM, density * margin.bottom)
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
//    hostView.scrollEnabled = when (overflow) {
//      Overflow.Clip -> false
//      Overflow.Scroll -> true
//      else -> throw AssertionError()
//    }
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
    yogaLayout.yogaNode.alignItems = when (alignItems) {
      AlignItems.FlexStart -> YogaAlign.FLEX_START
      AlignItems.FlexEnd -> YogaAlign.FLEX_END
      AlignItems.Center -> YogaAlign.CENTER
      AlignItems.Baseline -> YogaAlign.BASELINE
      AlignItems.Stretch -> YogaAlign.STRETCH
      else -> throw AssertionError()
    }
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    yogaLayout.yogaNode.justifyContent = when (justifyContent) {
      JustifyContent.FlexStart -> YogaJustify.FLEX_START
      JustifyContent.FlexEnd -> YogaJustify.FLEX_END
      JustifyContent.Center -> YogaJustify.CENTER
      JustifyContent.SpaceBetween -> YogaJustify.SPACE_BETWEEN
      JustifyContent.SpaceAround -> YogaJustify.SPACE_AROUND
      JustifyContent.SpaceEvenly -> YogaJustify.SPACE_EVENLY
      else -> throw AssertionError()
    }
    invalidate()
  }

  private fun applyLayoutParams() {
    yogaLayout.updateLayoutParams {
      width = MATCH_PARENT
      height = WRAP_CONTENT
    }
  }

  private fun invalidate() {
    applyLayoutParams()
    for (i in 0 until yogaLayout.yogaNode.childCount) {
      yogaLayout.yogaNode.getChildAt(i).dirty()
    }
    yogaLayout.invalidate()
    yogaLayout.requestLayout()
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
      //updateViewHierarchy()
      setBackgroundColor(Color.GREEN)
      yogaLayout.setBackgroundColor(Color.BLUE)
    }

    private fun updateViewHierarchy() {
      removeAllViews()
      (yogaLayout.parent as ViewGroup?)?.removeView(yogaLayout)

      if (scrollEnabled) {
        yogaLayout.yogaNode.overflow = YogaOverflow.SCROLL
        addView(newScrollView().apply { addView(yogaLayout) })
      } else {
        yogaLayout.yogaNode.overflow = YogaOverflow.VISIBLE
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
