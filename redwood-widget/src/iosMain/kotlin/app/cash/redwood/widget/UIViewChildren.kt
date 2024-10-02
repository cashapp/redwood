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
import platform.UIKit.UIStackView
import platform.UIKit.UIView
import platform.darwin.NSInteger

@ObjCName("UIViewChildren", exact = true)
public class UIViewChildren(
  private val container: UIView,
  private val insert: (index: Int, widget: Widget<UIView>) -> Unit = when (container) {
    is UIStackView -> { index, widget ->
      container.insertArrangedSubview(widget.value, index.convert())
    }
    else -> { index, widget ->
      container.insertSubview(widget.value, index.convert<NSInteger>())
    }
  },
  private val remove: (index: Int, count: Int) -> Unit = when (container) {
    is UIStackView -> { index, count ->
      container.typedArrangedSubviews.removeFromSuperview(index, count)
    }
    else -> { index, count ->
      container.typedSubviews.removeFromSuperview(index, count)
    }
  },
  private val invalidateSize: () -> Unit = { (container.superview ?: container).setNeedsLayout() },
  private val onModifierUpdated: (index: Int, widget: Widget<UIView>) -> Unit = { _, _ ->
    invalidateSize()
  },
) : Widget.Children<UIView> {
  private val _widgets = ArrayList<Widget<UIView>>()
  override val widgets: List<Widget<UIView>> get() = _widgets

  override fun insert(index: Int, widget: Widget<UIView>) {
    _widgets.add(index, widget)
    insert.invoke(index, widget)
    invalidateSize()
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    remove.invoke(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    for (i in newIndex until newIndex + count) {
      insert.invoke(i, widgets[i])
    }
    invalidateSize()
  }

  override fun remove(index: Int, count: Int) {
    for (i in index until index + count) {
      val widget = _widgets[i]
      if (widget is ResizableWidget<*>) {
        widget.sizeListener = null // Break a reference cycle.
      }
    }
    _widgets.remove(index, count)

    remove.invoke(index, count)
    invalidateSize()
  }

  override fun onModifierUpdated(index: Int, widget: Widget<UIView>) {
    onModifierUpdated.invoke(index, widget)
  }

  override fun detach() {
    // Note that this doesn't update [container], since we don't want to trigger an update to the UI
    // if a detached widget is still on screen.
    for (widget in _widgets) {
      if (widget is ResizableWidget<*>) {
        widget.sizeListener = null // Break a reference cycle.
      }
    }
    _widgets.clear()
  }
}

private fun List<UIView>.removeFromSuperview(index: Int, count: Int) {
  for (i in index until index + count) {
    this[index].removeFromSuperview()
  }
}
