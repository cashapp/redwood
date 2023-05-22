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
import app.cash.redwood.flexbox.FlexContainer
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.Size
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
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class UIViewFlexContainer(
  private val direction: FlexDirection,
) : Row<UIView>, Column<UIView> {
  private val container = FlexContainer().apply {
    flexDirection = direction
    roundToInt = false
  }
  private val view = FlexContainerHostView().apply {
    showsHorizontalScrollIndicator = false
    showsVerticalScrollIndicator = false
  }

  override val value: UIView get() = view

  override val children = UIViewChildren(value)

  override var modifiers: Modifier = Modifier

  override fun width(width: Constraint) {
    container.fillWidth = width == Constraint.Fill
    invalidate()
  }

  override fun height(height: Constraint) {
    container.fillHeight = height == Constraint.Fill
    invalidate()
  }

  override fun margin(margin: Margin) {
    container.margin = margin.toSpacing(Density.Default)
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    view.setScrollEnabled(overflow == Overflow.Scroll)
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
    container.alignItems = alignItems
    invalidate()
  }

  private fun justifyContent(justifyContent: JustifyContent) {
    container.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    value.setNeedsLayout()
  }

  private inner class FlexContainerHostView : UIScrollView(cValue { CGRectZero }) {
    private var needsLayout = true

    override fun intrinsicContentSize(): CValue<CGSize> {
      return CGSizeMake(UIViewNoIntrinsicMetric, UIViewNoIntrinsicMetric)
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      return measure(size.useContents { toUnsafeSize() }).run { CGSizeMake(width, height) }
    }

    override fun setNeedsLayout() {
      needsLayout = true
    }

    override fun layoutSubviews() {
      if (!needsLayout) return
      needsLayout = false

      val bounds = bounds.useContents { size.toUnsafeSize() }
      measure(bounds)

      setContentSize(
        CGSizeMake(
          width = container.items.maxOfOrNull { it.right } ?: 0.0,
          height = container.items.maxOfOrNull { it.bottom } ?: 0.0,
        ),
      )
      superview?.setNeedsLayout()

      container.items.forEachIndexed { index, item ->
        typedSubviews[index].setFrame(
          CGRectMake(
            x = item.left,
            y = item.top,
            width = item.right - item.left,
            height = item.bottom - item.top,
          ),
        )
      }
    }

    private fun measure(size: UnsafeSize): Size {
      syncItems()
      val (widthSpec, heightSpec) = size.toMeasureSpecs()
      return container.measure(widthSpec, heightSpec)
    }

    private fun syncItems() {
      container.items.clear()
      children.widgets.forEach { widget ->
        container.items += newFlexItem(
          direction = direction,
          density = Density.Default,
          modifiers = widget.modifiers,
          measurable = UIViewMeasurable(widget.value),
        )
      }
    }
  }
}

internal data class UnsafeSize(
  val width: Double,
  val height: Double,
)
