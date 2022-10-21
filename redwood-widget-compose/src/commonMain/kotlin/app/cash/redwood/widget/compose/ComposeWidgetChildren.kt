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
import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.Widget

public class ComposeWidgetChildren : Widget.Children<@Composable () -> Unit> {
  private val children = mutableStateListOf<Pair<Widget<@Composable () -> Unit>, LayoutModifier>>()

  @Composable
  public fun render() {
    for ((widget) in children) {
      widget.value()
    }
  }

  override fun insert(index: Int, widget: Widget<@Composable () -> Unit>) {
    children.add(index, Pair(widget, widget.layoutModifiers))
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    children.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    children.removeRange(index, index + count)
  }

  override fun clear() {
    children.clear()
  }

  override fun updateLayoutModifier(widget: Widget<@Composable () -> Unit>) {
    val index = children.indexOfFirst { it.first == widget }
    if (index != -1) {
      children.add(index, Pair(widget, widget.layoutModifiers))
    }
  }
}
