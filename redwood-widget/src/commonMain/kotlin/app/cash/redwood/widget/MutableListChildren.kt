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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.MutableListChildren.Child

/**
 * A [MutableList] that is also a [Widget.Children].
 *
 * @param list Optional existing [MutableList] instance to wrap.
 * @param onUpdate Optional callback invoked when contents change via the [Widget.Children] API.
 */
public class MutableListChildren<T : Any>(
  private val list: MutableList<Child<T>> = mutableListOf(),
  private val onUpdate: (List<Child<T>>) -> Unit = {},
) : Widget.Children<T>, MutableList<Child<T>> by list {
  /** @param onUpdate Callback invoked when contents change via the [Widget.Children] API. */
  public constructor(onUpdate: (List<Child<T>>) -> Unit) : this(mutableListOf(), onUpdate = onUpdate)

  public val children: List<Child<T>> get() = list

  override fun insert(index: Int, widget: T, layoutModifier: LayoutModifier) {
    list.add(index, Child(widget, layoutModifier))
    onUpdate(list)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    list.move(fromIndex, toIndex, count)
    onUpdate(list)
  }

  override fun remove(index: Int, count: Int) {
    list.remove(index, count)
    onUpdate(list)
  }

  override fun clear() {
    list.clear()
    onUpdate(list)
  }

  override fun setLayoutModifier(index: Int, layoutModifier: LayoutModifier) {
    list.set(index, list[index].copy(layoutModifier = layoutModifier))
    onUpdate(list)
  }

  public data class Child<T : Any>(
    val widget: T,
    val layoutModifier: LayoutModifier,
  )
}
