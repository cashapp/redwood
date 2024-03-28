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

import kotlin.native.ObjCName

/**
 * A [MutableList] that is also a [Widget.Children].
 *
 * @param container Optional existing [MutableList] instance to wrap.
 */
@ObjCName("MutableListChildren", exact = true)
public class MutableListChildren<W : Any>(
  private val container: MutableList<Widget<W>> = mutableListOf(),
  private val modifierUpdated: () -> Unit = {},
) : Widget.Children<W>, MutableList<Widget<W>> by container {
  override val widgets: MutableList<Widget<W>> get() = container

  override fun insert(index: Int, widget: Widget<W>) {
    container.add(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    container.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    container.remove(index, count)
  }

  override fun onModifierUpdated(index: Int, widget: Widget<W>) {
    modifierUpdated()
  }
}
