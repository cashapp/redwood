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
package app.cash.redwood.widget.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget

public class ComposeWidgetChildren : Widget.Children<@Composable () -> Unit> {
  private val _widgets = MutableListChildren<@Composable () -> Unit>(mutableStateListOf())
  public val widgets: List<Widget<@Composable () -> Unit>> get() = _widgets

  @Composable
  public fun render() {
    for (child in _widgets) {
      child.value()
    }
  }

  override fun insert(index: Int, widget: Widget<@Composable () -> Unit>) {
    _widgets.insert(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)
  }

  override fun clear() {
    _widgets.clear()
  }

  override fun onLayoutModifierUpdated(index: Int) {
    _widgets.onLayoutModifierUpdated(index)
  }
}
