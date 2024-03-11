/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView

fun viewWidget(view: UIView): Widget<UIView> = object : Widget<UIView> {
  override val value: UIView get() = view
  override var modifier: Modifier = Modifier
}

val throwingWidgetSystem = WidgetSystem<UIView> { _, _ -> throw UnsupportedOperationException() }

val UIView.frameRectangle: Rectangle
  get() = frame.useContents { Rectangle(origin.x, origin.y, size.width, size.height) }

data class Rectangle(
  val x: Double,
  val y: Double,
  val width: Double,
  val height: Double,
)

/** This view exists only to be participate in layout testing. */
class RectangleUIView(
  width: Double,
  height: Double,
) : UIView(cValue { CGRectZero }) {
  var width = width
    set(value) {
      field = value
      invalidateIntrinsicContentSize()
    }
  var height = height
    set(value) {
      field = value
      invalidateIntrinsicContentSize()
    }

  override fun sizeThatFits(size: CValue<CGSize>) = CGSizeMake(width, height)

  override fun intrinsicContentSize(): CValue<CGSize> = CGSizeMake(width, height)
}
