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
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexContainer
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.Size
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.UIViewChildren
import cocoapods.YogaKit.YGAlign
import cocoapods.YogaKit.YGFlexDirection
import cocoapods.YogaKit.YGJustify
import cocoapods.YogaKit.configureLayoutWithBlock
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.UIKit.setFrame
import platform.UIKit.setNeedsLayout
import platform.UIKit.superview
import platform.darwin.NSObject

internal class UIViewFlexContainer(
  private val direction: FlexDirection,
) : Row<UIView>, Column<UIView> {
  override val value = UIView()

  override val children = UIViewChildren(value)

  override var layoutModifiers: LayoutModifier = LayoutModifier

  init {
    value.configureLayoutWithBlock {
      it!!.setFlexDirection(when (direction) {
        FlexDirection.Row -> YGFlexDirection.YGFlexDirectionRow
        FlexDirection.RowReverse -> YGFlexDirection.YGFlexDirectionRowReverse
        FlexDirection.Column -> YGFlexDirection.YGFlexDirectionColumn
        FlexDirection.ColumnReverse -> YGFlexDirection.YGFlexDirectionColumnReverse
        else -> throw AssertionError()
      })
    }
  }

  override fun width(width: Constraint) {
//    container.fillWidth = width == Constraint.Fill
    invalidate()
  }

  override fun height(height: Constraint) {
//    container.fillHeight = height == Constraint.Fill
    invalidate()
  }

  override fun margin(margin: Margin) {
    value.configureLayoutWithBlock {
      it!!.setFlexDirection(when (direction) {
        FlexDirection.Row -> YGFlexDirection.YGFlexDirectionRow
        FlexDirection.RowReverse -> YGFlexDirection.YGFlexDirectionRowReverse
        FlexDirection.Column -> YGFlexDirection.YGFlexDirectionColumn
        FlexDirection.ColumnReverse -> YGFlexDirection.YGFlexDirectionColumnReverse
        else -> throw AssertionError()
      })
    }
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
//    value.setScrollEnabled(overflow == Overflow.Scroll)
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
    value.configureLayoutWithBlock {
      it!!.alignItems = when (alignItems) {
        AlignItems.FlexStart -> YGAlign.YGAlignFlexStart
        AlignItems.FlexEnd -> YGAlign.YGAlignFlexEnd
        AlignItems.Center -> YGAlign.YGAlignCenter
        AlignItems.Baseline -> YGAlign.YGAlignBaseline
        AlignItems.Stretch -> YGAlign.YGAlignStretch
        else -> throw AssertionError()
      }
    }
    invalidate()
  }

  private fun justifyContent(justifyContent: JustifyContent) {
    value.configureLayoutWithBlock {
      it!!.justifyContent = when (justifyContent) {
        JustifyContent.FlexStart -> YGJustify.YGJustifyFlexStart
        JustifyContent.FlexEnd -> YGJustify.YGJustifyFlexEnd
        JustifyContent.Center -> YGJustify.YGJustifyCenter
        JustifyContent.SpaceBetween -> YGJustify.YGJustifySpaceBetween
        JustifyContent.SpaceAround -> YGJustify.YGJustifySpaceAround
        JustifyContent.SpaceEvenly -> YGJustify.YGJustifySpaceEvenly
        else -> throw AssertionError()
      }
    }
    invalidate()
  }

  private fun invalidate() {
    value.setNeedsLayout()
  }

//  private inner class UIViewDelegate : NSObject(), RedwoodScrollViewDelegateProtocol {
//    private var needsLayout = true
//
//    override fun intrinsicContentSize(): CValue<CGSize> = CGSizeMake(noIntrinsicSize.width, noIntrinsicSize.height)
//
//    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> =
//      measure(size.useContents { toUnsafeSize() }).run { CGSizeMake(width, height) }
//
//    override fun setNeedsLayout() {
//      needsLayout = true
//    }
//
//    override fun layoutSubviews() {
//      if (!needsLayout) return
//      needsLayout = false
//
//      val bounds = value.bounds.useContents { size.toUnsafeSize() }
//      measure(bounds)
//
//      value.setContentSize(
//        CGSizeMake(
//          width = container.items.maxOfOrNull { it.right } ?: 0.0,
//          height = container.items.maxOfOrNull { it.top } ?: 0.0,
//        ),
//      )
//      value.superview?.setNeedsLayout()
//
//      container.items.forEachIndexed { index, item ->
//        value.typedSubviews[index].setFrame(
//          CGRectMake(
//            x = item.left,
//            y = item.top,
//            width = item.right - item.left,
//            height = item.bottom - item.top,
//          ),
//        )
//      }
//    }
//
//    private fun measure(size: UnsafeSize): Size {
//      syncItems()
//      val (widthSpec, heightSpec) = size.toMeasureSpecs()
//      return container.measure(widthSpec, heightSpec)
//    }
//
//    private fun syncItems() {
//      container.items.clear()
//      children.widgets.forEach { widget ->
//        container.items += newFlexItem(
//          direction = direction,
//          density = DensityMultiplier,
//          layoutModifiers = widget.layoutModifiers,
//          measurable = UIViewMeasurable(widget.value),
//        )
//      }
//    }
//  }
}

private val noIntrinsicSize = UnsafeSize(UIViewNoIntrinsicMetric, UIViewNoIntrinsicMetric)

internal data class UnsafeSize(
  val width: Double,
  val height: Double,
)
