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
package app.cash.redwood.widget

import platform.UIKit.UIView
import platform.UIKit.insertSubview
import platform.UIKit.removeFromSuperview
import platform.UIKit.setNeedsDisplay

public class UIViewChildren(
  private val parent: UIView,
) : Widget.Children<UIView> {
  private val _widgets = MutableListChildren<UIView>()
  public val widgets: List<Widget<UIView>> get() = _widgets

  override fun insert(index: Int, widget: Widget<UIView>) {
    _widgets.insert(index, widget)
    parent.insertSubview(widget.value, index.toLong())
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    val subviews = Array(count) { offset ->
      parent.typedSubviews[fromIndex + offset].also(UIView::removeFromSuperview)
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    subviews.forEachIndexed { offset, view ->
      parent.insertSubview(view, (newIndex + offset).toLong())
    }
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)

    for (i in 0 until count) {
      // Subviews aren't removed immediately from the list.
      parent.typedSubviews[index + i].removeFromSuperview()
    }
  }

  override fun clear() {
    _widgets.clear()

    for (subview in parent.typedSubviews) {
      subview.removeFromSuperview()
    }
  }

  override fun onLayoutModifierUpdated(index: Int) {
    val subview = parent.typedSubviews[index]
    subview.setNeedsDisplay()
  }
}
