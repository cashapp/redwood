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
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIView

internal class UIViewSpacer : Spacer<UIView> {
  private var width = 0.0
  private var height = 0.0

  private val view = SpacerHostView()

  override val value: UIView get() = view

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun width(width: Dp) {
    this.width = with(Density.Default) { width.toPx() }
    invalidate()
  }

  override fun height(height: Dp) {
    this.height = with(Density.Default) { height.toPx() }
    invalidate()
  }

  private fun invalidate() {
    value.invalidateIntrinsicContentSize()
    value.setNeedsLayout()
  }

  private inner class SpacerHostView : UIView(cValue { CGRectZero }) {
    override fun intrinsicContentSize() = CGSizeMake(width, height)
  }
}
