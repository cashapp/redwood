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
import app.cash.redwood.flexcontainer.Size
import app.cash.redwood.flexcontainer.isHorizontal
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget

internal class ViewFlexContainer(context: Context, direction: FlexDirection) {
  private val container = FlexContainer().apply {
    flexDirection = direction
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

  val children: Widget.Children<View> = MutableListChildren(
    onUpdate = { children ->
      container.items.clear()
      hostView.removeAllViews()
      children.forEach {
        container.items += it.value.asItem()
        hostView.addView(it.value)
      }
    },
  )

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
      setMeasuredDimension(measureResult.containerSize.width, measureResult.containerSize.height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      container.layout(measureResult, Size(right - left, bottom - top))
      container.items.forEachIndexed { index, item ->
        getChildAt(index).layout(item.left, item.top, item.right, item.bottom)
      }
    }
  }
}
