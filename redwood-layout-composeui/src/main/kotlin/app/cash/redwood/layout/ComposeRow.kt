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
package app.cash.redwood.layout

import androidx.compose.runtime.Composable
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.Widget

class ComposeRow : Row<@Composable () -> Unit> {
  private val container = ComposeFlexContainer(FlexDirection.Column)

  override val children: Widget.Children<@Composable () -> Unit> get() = container.children

  override val value: @Composable () -> Unit get() = container.composable

  override var layoutModifiers: LayoutModifier
    get() = container.layoutModifiers
    set(value) { container.layoutModifiers = value }

  override fun padding(padding: Padding) {
    container.padding(padding)
  }

  override fun overflow(overflow: Overflow) {
    container.overflow(overflow)
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    container.justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    container.alignItems(verticalAlignment.toAlignItems())
  }
}
