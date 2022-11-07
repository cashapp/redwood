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
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.AlignSelf
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.FlexItem
import app.cash.redwood.flexcontainer.FlexItem.Companion.DefaultFlexGrow
import app.cash.redwood.flexcontainer.FlexItem.Companion.DefaultFlexShrink
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.flexcontainer.Measurable
import app.cash.redwood.flexcontainer.MeasureSpec
import app.cash.redwood.flexcontainer.MeasureSpecMode
import app.cash.redwood.flexcontainer.Size
import app.cash.redwood.flexcontainer.Spacing
import app.cash.redwood.flexcontainer.isHorizontal
import app.cash.redwood.flexcontainer.isVertical
import app.cash.redwood.layout.Grow
import app.cash.redwood.layout.HorizontalAlignment
import app.cash.redwood.layout.Padding as PaddingModifier
import app.cash.redwood.layout.Shrink
import app.cash.redwood.layout.VerticalAlignment
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Padding
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.UIKit.intrinsicContentSize
import platform.UIKit.sizeThatFits
import platform.UIKit.subviews

internal fun MainAxisAlignment.toJustifyContent() = when (this) {
  MainAxisAlignment.Start -> JustifyContent.FlexStart
  MainAxisAlignment.Center -> JustifyContent.Center
  MainAxisAlignment.End -> JustifyContent.FlexEnd
  MainAxisAlignment.SpaceBetween -> JustifyContent.SpaceBetween
  MainAxisAlignment.SpaceAround -> JustifyContent.SpaceAround
  MainAxisAlignment.SpaceEvenly -> JustifyContent.SpaceEvenly
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignItems() = when (this) {
  CrossAxisAlignment.Start -> AlignItems.FlexStart
  CrossAxisAlignment.Center -> AlignItems.Center
  CrossAxisAlignment.End -> AlignItems.FlexEnd
  CrossAxisAlignment.Stretch -> AlignItems.Stretch
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignSelf() = when (this) {
  CrossAxisAlignment.Start -> AlignSelf.FlexStart
  CrossAxisAlignment.Center -> AlignSelf.Center
  CrossAxisAlignment.End -> AlignSelf.FlexEnd
  CrossAxisAlignment.Stretch -> AlignSelf.Stretch
  else -> throw AssertionError()
}

internal fun Padding.toSpacing() = Spacing(start, end, top, bottom)

internal fun Size.toDoubleSize() = DoubleSize(width.toDouble(), height.toDouble())

internal fun CGSize.toDoubleSize() = DoubleSize(width, height)

internal fun CValue<CGSize>.toSize() = useContents { Size(width.roundToInt(), height.roundToInt()) }

internal fun DoubleSize.toMeasureSpecs(): Pair<MeasureSpec, MeasureSpec> {
  val widthSpec = when (width) {
    UIViewNoIntrinsicMetric -> MeasureSpec.from(MeasureSpec.MaxSize, MeasureSpecMode.Unspecified)
    else -> MeasureSpec.from(width.roundToInt(), MeasureSpecMode.AtMost)
  }
  val heightSpec = when (height) {
    UIViewNoIntrinsicMetric -> MeasureSpec.from(MeasureSpec.MaxSize, MeasureSpecMode.Unspecified)
    else -> MeasureSpec.from(height.roundToInt(), MeasureSpecMode.AtMost)
  }
  return widthSpec to heightSpec
}

internal fun measureSpecsToCGSize(widthSpec: MeasureSpec, heightSpec: MeasureSpec): CValue<CGSize> {
  val width = when (widthSpec.mode) {
    MeasureSpecMode.Unspecified -> UIViewNoIntrinsicMetric
    else -> widthSpec.size.toDouble()
  }
  val height = when (heightSpec.mode) {
    MeasureSpecMode.Unspecified -> UIViewNoIntrinsicMetric
    else -> heightSpec.size.toDouble()
  }
  return CGSizeMake(width, height)
}

@Suppress("UNCHECKED_CAST")
internal val UIView.typedSubviews: List<UIView>
  get() = subviews as List<UIView>

internal fun UIView.asItem(layoutModifiers: LayoutModifier, direction: FlexDirection): FlexItem {
  var flexGrow = DefaultFlexGrow
  var flexShrink = DefaultFlexShrink
  var padding = Padding.Zero
  var crossAxisAlignment = CrossAxisAlignment.Start
  var isCrossAxisAlignmentSet = false
  layoutModifiers.forEach { modifier ->
    when (modifier) {
      is Grow -> flexGrow = modifier.value
      is Shrink -> flexShrink = modifier.value
      is PaddingModifier -> padding = modifier.padding
      is HorizontalAlignment -> if (direction.isVertical) {
        crossAxisAlignment = modifier.alignment
        isCrossAxisAlignmentSet = true
      }
      is VerticalAlignment -> if (direction.isHorizontal) {
        crossAxisAlignment = modifier.alignment
        isCrossAxisAlignmentSet = true
      }
    }
  }
  return FlexItem(
    flexGrow = flexGrow,
    flexShrink = flexShrink,
    margin = padding.toSpacing(),
    alignSelf = if (isCrossAxisAlignmentSet) {
      crossAxisAlignment.toAlignSelf()
    } else {
      AlignSelf.Auto
    },
    measurable = UIViewMeasurable(this),
  )
}

internal class UIViewMeasurable(val view: UIView) : Measurable() {
  override val minWidth: Int
    get() = view.intrinsicContentSize.useContents {
      if (width == UIViewNoIntrinsicMetric) 0 else ceil(width).toInt()
    }
  override val minHeight: Int
    get() = view.intrinsicContentSize.useContents {
      if (height == UIViewNoIntrinsicMetric) 0 else ceil(height).toInt()
    }

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    var output = view.sizeThatFits(measureSpecsToCGSize(widthSpec, heightSpec)).toSize()
    if (widthSpec.mode == MeasureSpecMode.Exactly) {
      output = output.copy(width = widthSpec.size)
    }
    if (heightSpec.mode == MeasureSpecMode.Exactly) {
      output = output.copy(height = heightSpec.size)
    }
    return output
  }
}
