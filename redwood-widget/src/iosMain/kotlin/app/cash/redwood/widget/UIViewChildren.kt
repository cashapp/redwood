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

import kotlinx.cinterop.convert
import platform.UIKit.UIView
import platform.UIKit.insertSubview
import platform.UIKit.removeFromSuperview
import platform.UIKit.setNeedsDisplay
import platform.darwin.NSInteger

public class UIViewChildren(
  private val parent: UIView,
  private val insert: (UIView, Int) -> Unit = { view, index ->
    parent.insertSubview(view, index.convert<NSInteger>())
  },
) : Widget.Children<UIView> {
  private val _widgets = MutableListChildren<UIView>()
  public val widgets: List<Widget<UIView>> get() = _widgets

  override fun insert(index: Int, widget: Widget<UIView>) {
    _widgets.insert(index, widget)
    insert(widget.value, index)
    invalidate()
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    val subviews = Array(count) {
      parent.typedSubviews[fromIndex].also(UIView::removeFromSuperview)
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    subviews.forEachIndexed { offset, view ->
      insert(view, newIndex + offset)
    }
    invalidate()
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)

    repeat(count) {
      parent.typedSubviews[index].removeFromSuperview()
    }
    invalidate()
  }

  override fun clear() {
    _widgets.clear()

    for (subview in parent.typedSubviews) {
      subview.removeFromSuperview()
    }
    invalidate()
  }

  override fun onLayoutModifierUpdated(index: Int) {
    invalidate()
  }

  private fun invalidate() {
    parent.setNeedsDisplay()
  }
}
