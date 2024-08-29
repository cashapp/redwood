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
package app.cash.redwood.lazylayout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.lazylayout.Text
import app.cash.redwood.lazylayout.toUIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView

class UIViewText : Text<UIView> {
  override val value = UILabel().apply {
    numberOfLines = 0
    textColor = platform.UIKit.UIColor.blackColor
  }
  override var modifier: Modifier = Modifier

  override fun text(text: String) {
    value.text = text
  }

  override fun bgColor(color: Int) {
    value.backgroundColor = color.toUIColor()
  }
}
