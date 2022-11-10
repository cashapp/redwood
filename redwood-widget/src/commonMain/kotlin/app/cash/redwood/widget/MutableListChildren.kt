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

/**
 * A [MutableList] that is also a [Widget.Children].
 *
 * @param list Optional existing [MutableList] instance to wrap.
 */
public class MutableListChildren<T : Any>(
  private val list: MutableList<Widget<T>> = mutableListOf(),
) : Widget.Children<T>, MutableList<Widget<T>> by list {
  override fun insert(index: Int, widget: Widget<T>) {
    list.add(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    list.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    list.remove(index, count)
  }

  override fun onLayoutModifierUpdated(index: Int) {}
}
