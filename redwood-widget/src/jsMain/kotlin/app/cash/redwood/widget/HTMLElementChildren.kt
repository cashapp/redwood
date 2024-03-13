/*
 * Copyright (C) 2021 Square, Inc.
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

import org.w3c.dom.HTMLElement
import org.w3c.dom.get

public class HTMLElementChildren(
  private val container: HTMLElement,
) : Widget.Children<HTMLElement> {
  private val _widgets = ArrayList<Widget<HTMLElement>>()
  public val widgets: List<Widget<HTMLElement>> get() = _widgets

  override fun insert(index: Int, widget: Widget<HTMLElement>) {
    _widgets.add(index, widget)

    // Null element returned when index == childCount causes insertion at end.
    val current = container.children[index]
    container.insertBefore(widget.value, current)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    val elements = Array(count) {
      val element = container.children[fromIndex] as HTMLElement
      container.removeChild(element)
      element
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    elements.forEachIndexed { offset, element ->
      // Null element returned when newIndex + offset == childCount causes insertion at end.
      val current = container.children[newIndex + offset]
      container.insertBefore(element, current)
    }
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)

    repeat(count) {
      container.removeChild(container.children[index]!!)
    }
  }

  override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
    // If this function is being invoked we are guaranteed to have at least one child.

    val element = container.children[0] as HTMLElement
    container.removeChild(element)
    container.insertBefore(element, container.children[0])
  }
}
