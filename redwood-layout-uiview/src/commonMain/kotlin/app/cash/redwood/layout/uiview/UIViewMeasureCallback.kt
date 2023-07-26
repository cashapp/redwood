/*
 * Copyright (C) 2023 Square, Inc.
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

import app.cash.redwood.yoga.MeasureCallback
import app.cash.redwood.yoga.MeasureMode
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class UIViewMeasureCallback(val view: UIView) : MeasureCallback {
  override fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size {
    val constrainedWidth = when (widthMode) {
      MeasureMode.Undefined -> UIViewNoIntrinsicMetric
      else -> width.toDouble()
    }
    val constrainedHeight = when (heightMode) {
      MeasureMode.Undefined -> UIViewNoIntrinsicMetric
      else -> height.toDouble()
    }

    // The default implementation of sizeThatFits: returns the existing size of
    // the view. That means that if we want to layout an empty UIView, which
    // already has a frame set, its measured size should be CGSizeZero, but
    // UIKit returns the existing size. See https://github.com/facebook/yoga/issues/606
    // for more information.
    val sizeThatFits = if (view.isMemberOfClass(UIView.`class`()) && view.typedSubviews.isEmpty()) {
      Size(0f, 0f)
    } else {
      view.sizeThatFits(CGSizeMake(constrainedWidth, constrainedHeight)).toSize()
    }

    return Size(
      width = sanitizeMeasurement(constrainedWidth, sizeThatFits.width, widthMode),
      height = sanitizeMeasurement(constrainedHeight, sizeThatFits.height, heightMode),
    )
  }

  private fun sanitizeMeasurement(
    constrainedSize: Double,
    measuredSize: Float,
    measureMode: MeasureMode,
  ): Float = when (measureMode) {
    MeasureMode.Exactly -> constrainedSize.toFloat()
    MeasureMode.AtMost -> measuredSize
    MeasureMode.Undefined -> measuredSize
    else -> throw AssertionError()
  }
}

private fun CValue<CGSize>.toSize() = useContents {
  Size(width.toFloat(), height.toFloat())
}
