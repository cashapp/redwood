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

import app.cash.redwood.Modifier
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.JustifyContent
import app.cash.redwood.yoga.isHorizontal
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.UIViewChildren
import platform.UIKit.UIColor
import platform.UIKit.UIView

internal class UIViewFlexContainer(
  private val direction: FlexDirection,
) : Row<UIView>, Column<UIView> {
  private val yogaView = YogaUIView()
  private val density = Density.Default

  override val value get() = yogaView.apply {
    backgroundColor = if (direction.isHorizontal) UIColor.blueColor else UIColor.redColor
  }
  override val children = UIViewChildren(value)
  override var modifier: Modifier = Modifier

  init {
    yogaView.rootNode.flexDirection = direction
    yogaView.density = density
    yogaView.getModifier = { children.widgets[it].modifier }
  }

  override fun width(width: Constraint) {
    yogaView.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    yogaView.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    yogaView.rootNode.apply {
      marginStart = with(density) { margin.start.toPx() }.toFloat()
      marginEnd = with(density) { margin.end.toPx() }.toFloat()
      marginTop = with(density) { margin.top.toPx() }.toFloat()
      marginBottom = with(density) { margin.bottom.toPx() }.toFloat()
    }
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    invalidate()
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignItems(horizontalAlignment.toAlignItems())
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    justifyContent(verticalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignItems(verticalAlignment.toAlignItems())
  }

  private fun alignItems(alignItems: AlignItems) {
    yogaView.rootNode.alignItems = alignItems
    invalidate()
  }

  private fun justifyContent(justifyContent: JustifyContent) {
    yogaView.rootNode.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    value.setNeedsLayout()
  }
}
