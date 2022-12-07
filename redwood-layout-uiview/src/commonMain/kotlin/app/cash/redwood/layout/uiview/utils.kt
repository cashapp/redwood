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

import app.cash.redwood.flexbox.Constraints
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import app.cash.redwood.flexbox.constrain
import app.cash.redwood.flexbox.constrainHeight
import app.cash.redwood.flexbox.constrainWidth
import app.cash.redwood.flexbox.hasBoundedHeight
import app.cash.redwood.flexbox.hasBoundedWidth
import app.cash.redwood.flexbox.hasFixedHeight
import app.cash.redwood.flexbox.hasFixedWidth
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
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

internal fun CGSize.toUnsafeSize() = UnsafeSize(width, height)

internal fun Size.toUnsafeSize() = UnsafeSize(width, height)

internal fun UnsafeSize.toConstraints(): Constraints {
  return Constraints(
    maxWidth = if (width == UIViewNoIntrinsicMetric) Constraints.Infinity else width,
    maxHeight = if (height == UIViewNoIntrinsicMetric) Constraints.Infinity else height,
  )
}

internal fun Constraints.toCGSize(): CValue<CGSize> {
  return CGSizeMake(
    width = if (hasBoundedWidth) maxWidth else UIViewNoIntrinsicMetric,
    height = if (hasBoundedHeight) maxHeight else UIViewNoIntrinsicMetric,
  )
}

public fun Constraints.constrain(size: CGSize): Size {
  return Size(constrainWidth(size.width), constrainHeight(size.height))
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

  override fun measure(constraints: Constraints): Size {
    val output = view.sizeThatFits(constraints.toCGSize())
    return output.useContents { constraints.constrain(this) }
  }
}
