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

import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.UIKit.intrinsicContentSize
import platform.UIKit.sizeThatFits
import platform.UIKit.subviews

// The cross platform density multiples use iOS as an anchor.
internal const val DensityMultiplier = 1.0

internal fun CGSize.toSize() = Size(width, height)

internal fun CGSize.toUnsafeSize() = UnsafeSize(width, height)

internal fun Size.toUnsafeSize() = UnsafeSize(width, height)

internal fun UnsafeSize.toMeasureSpecs(): Pair<MeasureSpec, MeasureSpec> {
  val widthSpec = when (width) {
    UIViewNoIntrinsicMetric -> MeasureSpec.from(Double.MAX_VALUE, MeasureSpecMode.Unspecified)
    else -> MeasureSpec.from(width, MeasureSpecMode.AtMost)
  }
  val heightSpec = when (height) {
    UIViewNoIntrinsicMetric -> MeasureSpec.from(Double.MAX_VALUE, MeasureSpecMode.Unspecified)
    else -> MeasureSpec.from(height, MeasureSpecMode.AtMost)
  }
  return widthSpec to heightSpec
}

internal fun measureSpecsToCGSize(widthSpec: MeasureSpec, heightSpec: MeasureSpec): CValue<CGSize> {
  val width = when (widthSpec.mode) {
    MeasureSpecMode.Unspecified -> UIViewNoIntrinsicMetric
    else -> widthSpec.size
  }
  val height = when (heightSpec.mode) {
    MeasureSpecMode.Unspecified -> UIViewNoIntrinsicMetric
    else -> heightSpec.size
  }
  return CGSizeMake(width, height)
}

@Suppress("UNCHECKED_CAST")
internal val UIView.typedSubviews: List<UIView>
  get() = subviews as List<UIView>

internal class UIViewMeasurable(val view: UIView) : Measurable() {
  override val minWidth: Double
    get() = view.intrinsicContentSize.useContents {
      if (width == UIViewNoIntrinsicMetric) 0.0 else width
    }
  override val minHeight: Double
    get() = view.intrinsicContentSize.useContents {
      if (height == UIViewNoIntrinsicMetric) 0.0 else height
    }

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    var output = view.sizeThatFits(measureSpecsToCGSize(widthSpec, heightSpec)).useContents { toSize() }
    if (widthSpec.mode == MeasureSpecMode.Exactly) {
      output = output.copy(width = widthSpec.size)
    }
    if (heightSpec.mode == MeasureSpecMode.Exactly) {
      output = output.copy(height = heightSpec.size)
    }
    return output
  }
}
