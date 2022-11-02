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
package app.cash.redwood.layout.uiview

import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.Widget
import platform.UIKit.UIView

internal class UIViewRow(viewFactory: RedwoodUIScrollViewFactory) : Row<UIView> {
  private val container = UIViewFlexContainer(viewFactory, FlexDirection.Row)

  override val children: Widget.Children<UIView> get() = container.children

  override val value: UIView get() = container.view

  override var layoutModifiers: LayoutModifier = LayoutModifier

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
