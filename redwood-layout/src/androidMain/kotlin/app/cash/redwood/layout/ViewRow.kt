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

import android.content.Context
import android.view.View
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.widget.Widget

public class ViewRow(context: Context) : RowWidget<View> {
  private val layout = ViewLayout(context, FlexDirection.Row)

  override val children: Widget.Children<View> get() = layout.children

  override val value: View get() = layout.view

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun padding(padding: Padding) {
    layout.padding(padding)
  }

  override fun overflow(overflow: Overflow) {
    layout.overflow(overflow)
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    layout.justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    layout.alignItems(verticalAlignment.toAlignItems())
  }
}
