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

import app.cash.redwood.Modifier
import kotlinx.cinterop.convert
import platform.UIKit.UIStackView
import platform.UIKit.UIView
import platform.darwin.NSInteger

@ObjCName("UIViewChildren", exact = true)
public class UIViewChildren(
  private val container: UIView,
  private val insert: (UIView, Modifier, Int) -> Unit = when (container) {
    is UIStackView -> { view, _, index -> container.insertArrangedSubview(view, index.convert()) }
    else -> { view, _, index -> container.insertSubview(view, index.convert<NSInteger>()) }
  },
  private val remove: (index: Int, count: Int) -> Array<UIView> = when (container) {
    is UIStackView -> { index, count -> container.typedArrangedSubviews.remove(index, count) }
    else -> { index, count -> container.typedSubviews.remove(index, count) }
  },
  private val updateModifier: (Modifier, Int) -> Unit = { _, _ -> },
) : Widget.Children<UIView> {
  private val _widgets = ArrayList<Widget<UIView>>()
  override val widgets: List<Widget<UIView>> get() = _widgets

  override fun insert(index: Int, widget: Widget<UIView>) {
    _widgets.add(index, widget)
    insert(widget.value, widget.modifier, index)
    invalidate()
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    val subviews = remove.invoke(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    subviews.forEachIndexed { offset, view ->
      val subviewIndex = newIndex + offset
      insert(view, widgets[subviewIndex].modifier, subviewIndex)
    }
    invalidate()
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)

    remove.invoke(index, count)
    invalidate()
  }

  override fun onModifierUpdated(index: Int, widget: Widget<UIView>) {
    updateModifier(widget.modifier, index)
    invalidate()
  }

  private fun invalidate() {
    (container.superview ?: container).setNeedsLayout()
  }

  override fun detach() {
    // Note that this doesn't update [container], since we don't want to trigger an update to the UI
    // if a detached widget is still on screen.
    _widgets.clear()
  }
}

private fun List<UIView>.remove(index: Int, count: Int): Array<UIView> {
  return Array(count) { offset ->
    this[index + offset].also(UIView::removeFromSuperview)
  }
}
