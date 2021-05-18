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
package app.cash.treehouse.widget

import kotlinx.dom.clear
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

public class HTMLElementChildren(
  private val parent: HTMLElement,
) : Widget.Children<HTMLElement> {
  override fun insert(index: Int, widget: HTMLElement) {
    // Null element returned when index == childCount causes insertion at end.
    val current = parent.children[index]
    parent.insertBefore(widget, current)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    val elements = Array(count) {
      val element = parent.children[fromIndex] as HTMLElement
      parent.removeChild(element)
      element
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    elements.forEachIndexed { offset, element ->
      // Null element returned when newIndex + offset == childCount causes insertion at end.
      val current = parent.children[newIndex + offset]
      parent.insertBefore(element, current)
    }
  }

  override fun remove(index: Int, count: Int) {
    repeat(count) {
      parent.removeChild(parent.children[index]!!)
    }
  }

  override fun clear() {
    parent.clear()
  }
}
