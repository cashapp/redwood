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

import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexContainer
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.flexcontainer.MeasureResult
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
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

  private val _view: UIScrollView = viewFactory.create(UIViewDelegate())
  val view: UIView get() = _view

  private val _children: UIViewChildren = UIViewChildren(_view)
  val children: Widget.Children<UIView> get() = _children

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

  private inner class UIViewDelegate : RedwoodUIScrollViewDelegate {
    private var needsLayout = true

    override val intrinsicContentSize get() = noIntrinsicSize

    override fun sizeThatFits(size: UnsafeSize): UnsafeSize {
      return measure(size).containerSize.toUnsafeSize()
    }

    override fun setNeedsLayout() {
      needsLayout = true
    }

    override fun layoutSubviews() {
      if (!needsLayout) return
      needsLayout = false

      val bounds = _view.bounds.useContents { size.toUnsafeSize() }
      container.layout(measure(bounds))

      _view.setContentSize(
        CGSizeMake(
          width = container.items.maxOfOrNull { it.right } ?: 0.0,
          height = container.items.maxOfOrNull { it.top } ?: 0.0,
        ),
      )
      _view.superview?.setNeedsLayout()

      container.items.forEachIndexed { index, item ->
        _view.typedSubviews[index].setFrame(
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

private val noIntrinsicSize = UnsafeSize(UIViewNoIntrinsicMetric, UIViewNoIntrinsicMetric)
