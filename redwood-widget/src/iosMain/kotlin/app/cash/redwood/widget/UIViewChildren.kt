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
  private val insert: (UIView, Int) -> Unit = when (container) {
    is UIStackView -> { view, index -> container.insertArrangedSubview(view, index.convert()) }
    else -> { view, index -> container.insertSubview(view, index.convert<NSInteger>()) }
  },
  private val remove: (index: Int, count: Int) -> Array<UIView> = when (container) {
    is UIStackView -> { index, count -> container.typedArrangedSubviews.remove(index, count) }
    else -> { index, count -> container.typedSubviews.remove(index, count) }
  },
) : Widget.Children<UIView> {
  private val _widgets = ArrayList<Widget<UIView>>()
  public val widgets: List<Widget<UIView>> get() = _widgets

  override fun insert(index: Int, widget: Widget<UIView>) {
    _widgets.add(index, widget)
    insert(widget.value, index)
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
      insert(view, newIndex + offset)
    }
    invalidate()
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)

    remove.invoke(index, count)
    invalidate()
  }

  override fun onModifierUpdated(index: Int, widget: Widget<UIView>) {
    invalidate()
  }

  private fun invalidate() {
    (container.superview ?: container).setNeedsLayout()
  }
}

private fun List<UIView>.remove(index: Int, count: Int): Array<UIView> {
  return Array(count) { offset ->
    this[index + offset].also(UIView::removeFromSuperview)
  }
}
