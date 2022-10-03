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
package app.cash.redwood.layout

import app.cash.redwood.flexbox.MeasureSpec as RedwoodMeasureSpec
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.FlexboxEngine
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget

internal class ViewLayout(context: Context, direction: FlexDirection) {
  private val engine = FlexboxEngine().apply {
    flexDirection = direction
  }

  private val _view = HostView(context)
  val view: View get() = _view

  val children: Widget.Children<View> = MutableListChildren(
    onUpdate = { views ->
      engine.nodes.clear()
      _view.removeAllViews()
      views.forEach {
        engine.nodes += it.asNode()
        _view.addView(it)
      }
    },
  )

  var layoutModifiers: LayoutModifier = LayoutModifier

  fun padding(padding: Padding) {
    engine.padding = padding.toSpacing()
    invalidate()
  }

  @SuppressLint("ClickableViewAccessibility")
  fun overflow(overflow: Overflow) {
    if (overflow == Overflow.Scroll) {
      _view.setOnTouchListener(null)
    } else {
      _view.setOnTouchListener { _, _ -> true }
    }
    invalidate()
  }

  fun alignItems(alignItems: AlignItems) {
    engine.alignItems = alignItems
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    engine.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    _view.invalidate()
    _view.requestLayout()
  }

  private inner class HostView(context: Context) : ViewGroup(context) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      val widthSpec = RedwoodMeasureSpec.fromAndroid(widthMeasureSpec)
      val heightSpec = RedwoodMeasureSpec.fromAndroid(heightMeasureSpec)
      val (width, height) = engine.measure(widthSpec, heightSpec)
      setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      engine.layout(left, top, right, bottom)
    }
  }
}
