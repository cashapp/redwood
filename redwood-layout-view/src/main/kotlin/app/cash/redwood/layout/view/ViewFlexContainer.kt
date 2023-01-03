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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.core.widget.NestedScrollView
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexContainer
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.MeasureSpec as RedwoodMeasureSpec
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.ViewGroupChildren

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : Row<View>, Column<View> {
  private val container = FlexContainer().apply {
    flexDirection = direction
    roundToInt = true
  }
  private val density = DensityMultiplier * context.resources.displayMetrics.density

  private val hostView = HostView(context)
  override val value = newScrollView()

  override val children = ViewGroupChildren(hostView)

  override var layoutModifiers: LayoutModifier = LayoutModifier

  private var scrollEnabled = false

  override fun width(width: Constraint) {
    container.fillWidth = width == Constraint.Fill
    invalidate()
  }

  override fun height(height: Constraint) {
    container.fillHeight = height == Constraint.Fill
    invalidate()
  }

  override fun padding(padding: Padding) {
    container.padding = padding.toSpacing(density)
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    scrollEnabled = overflow == Overflow.Scroll
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
    container.alignItems = alignItems
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    container.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    value.invalidate()
    value.requestLayout()
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun newScrollView(): ViewGroup {
    return if (direction.isHorizontal) {
      object : HorizontalScrollView(context) {
        override fun onTouchEvent(ev: MotionEvent): Boolean {
          return scrollEnabled && super.onTouchEvent(ev)
        }
        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
          return scrollEnabled && super.onInterceptTouchEvent(ev)
        }
      }.apply {
        isFillViewport = true
      }
    } else {
      object : NestedScrollView(context) {
        override fun onTouchEvent(ev: MotionEvent): Boolean {
          return scrollEnabled && super.onTouchEvent(ev)
        }
        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
          return scrollEnabled && super.onInterceptTouchEvent(ev)
        }
      }.apply {
        isFillViewport = true
      }
    }.apply {
      isHorizontalScrollBarEnabled = false
      isVerticalScrollBarEnabled = false
      addView(hostView)
    }
  }

  private inner class HostView(context: Context) : ViewGroup(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      syncItems()
      val widthSpec = RedwoodMeasureSpec.fromAndroid(widthMeasureSpec)
      val heightSpec = RedwoodMeasureSpec.fromAndroid(heightMeasureSpec)
      val (width, height) = container.measure(widthSpec, heightSpec)
      setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      for (item in container.items) {
        val view = (item.measurable as ViewMeasurable).view
        view.layout(item.left.toInt(), item.top.toInt(), item.right.toInt(), item.bottom.toInt())
      }
    }

    private fun syncItems() {
      container.items.clear()
      children.widgets.forEach { widget ->
        container.items += newFlexItem(
          direction = direction,
          density = density,
          layoutModifiers = widget.layoutModifiers,
          measurable = ViewMeasurable(widget.value),
        )
      }
    }
  }
}
