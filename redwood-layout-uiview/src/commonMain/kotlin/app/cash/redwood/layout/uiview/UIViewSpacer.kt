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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.widget.Spacer
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView

internal class UIViewSpacer : Spacer<UIView> {
  private var width = 0.0
  private var height = 0.0

  private val view = SpacerHostView()

  override val value: UIView get() = view

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun width(width: Int) {
    require(width >= 0) { "width must be >= 0: $width" }
    this.width = width.toDouble()
    invalidate()
  }

  override fun height(height: Int) {
    require(height >= 0) { "height must be >= 0: $height" }
    this.height = height.toDouble()
    invalidate()
  }

  private fun invalidate() {
    value.invalidateIntrinsicContentSize()
    value.setNeedsLayout()
  }

  private inner class SpacerHostView : UIView() {
    override fun intrinsicContentSize() = CGSizeMake(width, height)
  }
}
