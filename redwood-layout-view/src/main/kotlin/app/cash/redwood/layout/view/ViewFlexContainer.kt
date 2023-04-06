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

import android.annotation.SuppressLint
import android.content.Context
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
import app.cash.redwood.yoga.enums.YogaAlign
import app.cash.redwood.yoga.enums.YogaEdge
import app.cash.redwood.yoga.enums.YogaFlexDirection
import app.cash.redwood.yoga.enums.YogaJustify

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val density = DensityMultiplier * context.resources.displayMetrics.density
  private val hostView = YogaLayout(context).apply {
    rootNode.setFlexDirection(when (direction) {
      FlexDirection.Row -> YogaFlexDirection.ROW
      FlexDirection.RowReverse -> YogaFlexDirection.ROW_REVERSE
      FlexDirection.Column -> YogaFlexDirection.COLUMN
      FlexDirection.ColumnReverse -> YogaFlexDirection.COLUMN_REVERSE
      else -> throw AssertionError()
    })
  }
  private var scrollEnabled = false

  private val _value = Container()
  override val value: View = _value

  override val children = ViewGroupChildren(hostView)
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun width(width: Constraint) {
    hostView.updateLayoutParams {
      this.height = when (width) {
        Constraint.Wrap -> WRAP_CONTENT
        Constraint.Fill -> MATCH_PARENT
        else -> throw AssertionError()
      }
    }
    invalidate()
  }

  override fun height(height: Constraint) {
    hostView.updateLayoutParams {
      this.height = when (height) {
        Constraint.Wrap -> WRAP_CONTENT
        Constraint.Fill -> MATCH_PARENT
        else -> throw AssertionError()
      }
    }
    invalidate()
  }

  override fun margin(margin: Margin) {
    hostView.rootNode.setMargin(YogaEdge.LEFT, density * margin.left)
    hostView.rootNode.setMargin(YogaEdge.RIGHT, density * margin.right)
    hostView.rootNode.setMargin(YogaEdge.TOP, density * margin.top)
    hostView.rootNode.setMargin(YogaEdge.BOTTOM, density * margin.bottom)
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    val oldScrollEnabled = scrollEnabled
    scrollEnabled = overflow == Overflow.Scroll
    invalidate(overflowChanged = oldScrollEnabled != scrollEnabled)
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
    hostView.rootNode.setAlignItems(when (alignItems) {
      AlignItems.FlexStart -> YogaAlign.FLEX_START
      AlignItems.FlexEnd -> YogaAlign.FLEX_END
      AlignItems.Center -> YogaAlign.CENTER
      AlignItems.Baseline -> YogaAlign.BASELINE
      AlignItems.Stretch -> YogaAlign.STRETCH
      else -> throw AssertionError()
    })
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    hostView.rootNode.setJustifyContent(when (justifyContent) {
      JustifyContent.FlexStart -> YogaJustify.FLEX_START
      JustifyContent.FlexEnd -> YogaJustify.FLEX_END
      JustifyContent.Center -> YogaJustify.CENTER
      JustifyContent.SpaceBetween -> YogaJustify.SPACE_BETWEEN
      JustifyContent.SpaceAround -> YogaJustify.SPACE_AROUND
      JustifyContent.SpaceEvenly -> YogaJustify.SPACE_EVENLY
      else -> throw AssertionError()
    })
    invalidate()
  }

  private fun invalidate(overflowChanged: Boolean = false) {
    if (overflowChanged) {
      _value.updateViewHierarchy()
    }
    value.invalidate()
    value.requestLayout()
  }

  private inner class Container : FrameLayout(context) {
    init {
      updateViewHierarchy()
    }

    fun updateViewHierarchy() {
      removeAllViews()
      hostView.parent?.let { (it as ViewGroup).removeView(hostView) }
      if (scrollEnabled) {
        addView(newScrollView().apply { addView(hostView) })
      } else {
        addView(hostView)
      }
    }

    @SuppressLint("ClickableViewAccessibility")
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
