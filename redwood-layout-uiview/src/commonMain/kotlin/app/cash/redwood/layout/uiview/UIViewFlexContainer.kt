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
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
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
import app.cash.redwood.yoga.Yoga
import app.cash.redwood.yoga.enums.YGEdge
import platform.UIKit.UIColor
import platform.UIKit.UIView

internal class UIViewFlexContainer(
  direction: FlexDirection,
) : Row<UIView>, Column<UIView> {
  private val yogaLayout = YogaLayout()

  override val value get() = yogaLayout.view.apply {
    backgroundColor = UIColor.redColor
  }
  override val children = UIViewChildren(value)
  override var modifier: Modifier = Modifier

  init {
    Yoga.YGNodeStyleSetFlexDirection(yogaLayout.rootNode, direction.toYoga())
  }

  override fun width(width: Constraint) {
    yogaLayout.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    yogaLayout.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeLeft,
      points = with(Density.Default) { margin.start.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeRight,
      points = with(Density.Default) { margin.end.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeTop,
      points = with(Density.Default) { margin.top.toPx() }.toFloat(),
    )
    Yoga.YGNodeStyleSetPadding(
      node = yogaLayout.rootNode,
      edge = YGEdge.YGEdgeBottom,
      points = with(Density.Default) { margin.bottom.toPx() }.toFloat(),
    )
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
    Yoga.YGNodeStyleSetAlignItems(yogaLayout.rootNode, alignItems.toYoga())
    invalidate()
  }

  private fun justifyContent(justifyContent: JustifyContent) {
    Yoga.YGNodeStyleSetJustifyContent(yogaLayout.rootNode, justifyContent.toYoga())
    invalidate()
  }

  private fun invalidate() {
    value.setNeedsLayout()
  }
}
