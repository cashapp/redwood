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
import android.widget.HorizontalScrollView
import android.widget.ScrollView as VerticalScrollView
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexContainer
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.flexcontainer.MeasureResult
import app.cash.redwood.flexcontainer.MeasureSpec as RedwoodMeasureSpec
import android.annotation.SuppressLint
import app.cash.redwood.flexcontainer.Size
import app.cash.redwood.flexcontainer.isHorizontal
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget

internal class ViewFlexContainer(
  context: Context,
  private val direction: FlexDirection,
) {
  private val container = FlexContainer().apply {
    flexDirection = direction
    roundToInt = true
  }

  private val hostView = HostView(context)

  private val scrollView = if (direction.isHorizontal) {
    HorizontalScrollView(context).apply {
      isFillViewport = true
      setTouchEnabled(false)
      addView(hostView)
    }
  } else {
    VerticalScrollView(context).apply {
      isFillViewport = true
      setTouchEnabled(false)
      addView(hostView)
    }
  }

  val view: View get() = scrollView

  private val _children = ViewGroupChildren(hostView)
  val children: Widget.Children<View> get() = _children

  fun padding(padding: Padding) {
    container.padding = padding.toSpacing()
    invalidate()
  }

  fun overflow(overflow: Overflow) {
    scrollView.setTouchEnabled(overflow == Overflow.Scroll)
    invalidate()
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
    scrollView.invalidate()
    scrollView.requestLayout()
  }

  private inner class HostView(context: Context) : ViewGroup(context) {

    private lateinit var measureResult: MeasureResult

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      val widthSpec = RedwoodMeasureSpec.fromAndroid(widthMeasureSpec)
      val heightSpec = RedwoodMeasureSpec.fromAndroid(heightMeasureSpec)
      measureResult = container.measure(widthSpec, heightSpec)
      setMeasuredDimension(measureResult.containerSize.width.toInt(), measureResult.containerSize.height.toInt())
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      container.layout(measureResult, Size((right - left).toDouble(), (bottom - top).toDouble()))
      container.items.forEachIndexed { index, item ->
        getChildAt(index).layout(item.left.toInt(), item.top.toInt(), item.right.toInt(), item.bottom.toInt())
      }
    }

    override fun onViewAdded(child: View) {
      super.onViewAdded(child)
      _children.widgets.forEachIndexed { index, widget ->
        if (widget.value === child) {
          container.items.add(index, widget.value.asItem(widget.layoutModifiers, direction))
          return@forEachIndexed
        }
      }
    }

    override fun onViewRemoved(child: View) {
      super.onViewRemoved(child)
      val index = container.items.indexOfFirst { item ->
        (item.measurable as ViewMeasurable).view === child
      }
      container.items.removeAt(index)
    }
  }
}
