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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.widget.Widget
import kotlin.jvm.JvmOverloads

public class ComposeWidgetChildren @JvmOverloads constructor(
  private val onModifierUpdated: () -> Unit = {},
) : Widget.Children<@Composable () -> Unit> {
  private var modifierTick by mutableStateOf(0)

  private val _widgets = mutableStateListOf<Widget<@Composable () -> Unit>>()
  public val widgets: List<Widget<@Composable () -> Unit>> get() = _widgets

  @Composable
  public fun Render() {
    // Observe the layout modifier count so we recompose if it changes.
    modifierTick

    for (index in _widgets.indices) {
      _widgets[index].value()
    }
  }

  override fun insert(index: Int, widget: Widget<@Composable () -> Unit>) {
    _widgets.add(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)
  }

  override fun onModifierUpdated(index: Int, widget: Widget<@Composable () -> Unit>) {
    modifierTick++
    onModifierUpdated.invoke()
  }
}
