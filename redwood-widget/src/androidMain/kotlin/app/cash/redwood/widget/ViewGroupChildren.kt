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

import android.view.View
import android.view.ViewGroup

public class ViewGroupChildren(
  private val container: ViewGroup,
  private val insert: (index: Int, view: View) -> Unit = { index, view ->
    container.addView(view, index)
  },
  private val remove: (index: Int, count: Int) -> Unit = { index, count ->
    container.removeViews(index, count)
  },
) : Widget.Children<View> {
  private val _widgets = ArrayList<Widget<View>>()
  override val widgets: List<Widget<View>> get() = _widgets

  override fun insert(index: Int, widget: Widget<View>) {
    _widgets.add(index, widget)
    insert(index, widget.value)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)

    val views = Array(count) { offset ->
      container.getChildAt(fromIndex + offset)
    }
    remove.invoke(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    views.forEachIndexed { offset, view ->
      insert(newIndex + offset, view)
    }
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)
    remove.invoke(index, count)
  }

  override fun onModifierUpdated(index: Int, widget: Widget<View>) {
    container.requestLayout()
  }

  override fun detach() {
  }
}
