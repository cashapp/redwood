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
package app.cash.redwood.layout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.layout.Color
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.toUIColor
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import kotlinx.cinterop.CValue
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView

class UIViewText : Text<UIView> {
  override val value = UILabel().apply {
    numberOfLines = 0
    textColor = UIColor.blackColor
  }
  override var modifier: Modifier = Modifier

  override fun text(text: String) {
    value.text = text
  }

  override fun bgColor(color: Int) {
    value.backgroundColor = color.toUIColor()
  }
}

class UIViewColor : Color<UIView> {
  override val value: UIView = object : UIView(CGRectZero.readValue()) {
    override fun intrinsicContentSize(): CValue<CGSize> {
      return CGSizeMake(width, height)
    }
  }
  override var modifier: Modifier = Modifier

  private var width = 0.0
  private var height = 0.0

  override fun width(width: Dp) {
    this.width = with(Density.Default) { width.toPx() }
    invalidate()
  }

  override fun height(height: Dp) {
    this.height = with(Density.Default) { height.toPx() }
    invalidate()
  }

  override fun color(color: Int) {
    value.backgroundColor = color.toUIColor()
  }

  private fun invalidate() {
    value.setFrame(CGRectMake(0.0, 0.0, width, height))
    value.invalidateIntrinsicContentSize()
  }
}
