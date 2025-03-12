/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.basic.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.basic.widget.Button
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIView
import platform.darwin.NSObject

internal class UIViewButton : Button<UIView> {
  override val value = UIButton().apply {
    backgroundColor = UIColor.Companion.grayColor
  }

  override var modifier: Modifier = Modifier

  private var onClick: (() -> Unit)? = null
  private val clickedTarget = ClickedTarget(this)
  private val clickedPointer = NSSelectorFromString(ClickedTarget::clicked.name)

  override fun text(text: String?) {
    value.setTitle(text, UIControlStateNormal)
  }

  override fun enabled(enabled: Boolean) {
    value.enabled = enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    if (this.onClick == null) {
      if (onClick != null) {
        value.addTarget(clickedTarget, clickedPointer, UIControlEventTouchUpInside)
      }
    } else if (onClick == null) {
      value.removeTarget(clickedTarget, clickedPointer, UIControlEventTouchUpInside)
    }
    this.onClick = onClick
  }

  private fun clicked() {
    onClick?.invoke()
  }

  /**
   * Selectors can only target subtypes of [NSObject] which is why this helper exists.
   * We cannot subclass it on [UIViewButton] directly because it implements a Kotlin
   * interface, but we also don't want to leak it into public API. The contained function
   * must be public for the selector to work.
   */
  private class ClickedTarget(private val target: UIViewButton) : NSObject() {
    @ObjCAction
    fun clicked() {
      target.clicked()
    }
  }
}
