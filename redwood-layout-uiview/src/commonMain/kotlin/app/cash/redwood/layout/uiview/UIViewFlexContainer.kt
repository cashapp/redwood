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

import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexContainer
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.MeasureResult
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.uiview.cinterop.CGSize
import app.cash.redwood.layout.uiview.cinterop.UIScrollViewWithInterop
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.UIKit.setFrame
import platform.UIKit.setNeedsLayout
import platform.UIKit.superview

internal class UIViewFlexContainer(
  viewFactory: RedwoodUIScrollViewFactory,
  private val direction: FlexDirection,
) {
  private val container = FlexContainer().apply {
    flexDirection = direction
    roundToInt = false
  }

  private val _view = HostView()
  val view: UIView get() = _view

  private val _children = UIViewChildren(_view)
  val children: Widget.Children<UIView> get() = _children

  init {
    _view.showsHorizontalScrollIndicator = false
    _view.showsVerticalScrollIndicator = false
  }

  fun width(width: Constraint) {
    container.fillWidth = width == Constraint.Fill
    invalidate()
  }

  fun height(height: Constraint) {
    container.fillHeight = height == Constraint.Fill
    invalidate()
  }

  fun padding(padding: Padding) {
    container.padding = padding.toSpacing()
    invalidate()
  }

  fun overflow(overflow: Overflow) {
    _view.setScrollEnabled(overflow == Overflow.Scroll)
    invalidate()
  }

  fun alignItems(alignItems: AlignItems) {
    container.alignItems = alignItems
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    container.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    _view.setNeedsLayout()
  }

  private inner class HostView : UIScrollViewWithInterop(cValue { CGRectZero }) {
    private var needsLayout = true

    init {
      showsHorizontalScrollIndicator = false
      showsVerticalScrollIndicator = false
    }

    override fun intrinsicContentSize(): CValue<CGSize> {
      return cValue { CGSizeMake(UIViewNoIntrinsicMetric, UIViewNoIntrinsicMetric) }
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      val (width, height) = measure(size.useContents { UnsafeSize(width, height) }).containerSize
      return cValue { CGSizeMake(width, height) }
    }

    override fun setNeedsLayout() {
      super.setNeedsLayout()
      needsLayout = true
    }

    override fun layoutSubviews() {
      super.layoutSubviews()

      if (!needsLayout) return
      needsLayout = false

      val bounds = bounds.useContents { size.toUnsafeSize() }
      container.layout(measure(bounds))

      setContentSize(
        CGSizeMake(
          width = container.items.maxOfOrNull { it.right } ?: 0.0,
          height = container.items.maxOfOrNull { it.top } ?: 0.0,
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

    private fun measure(size: UnsafeSize): MeasureResult {
      syncItems()
      val (widthSpec, heightSpec) = size.toMeasureSpecs()
      return container.measure(widthSpec, heightSpec)
    }

    private fun syncItems() {
      container.items.clear()
      _children.widgets.forEach { widget ->
        container.items += newFlexItem(
          direction = direction,
          layoutModifiers = widget.layoutModifiers,
          measurable = UIViewMeasurable(widget.value),
        )
      }
    }
  }
}
