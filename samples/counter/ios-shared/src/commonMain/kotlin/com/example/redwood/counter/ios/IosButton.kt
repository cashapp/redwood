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
package com.example.redwood.counter.ios

import app.cash.redwood.LayoutModifier
import com.example.redwood.counter.widget.Button
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIButton
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIView
import platform.objc.sel_registerName

class IosButton : Button<UIView> {
  override val value = UIButton()

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun text(text: String?) {
    value.setTitle(text, UIControlStateNormal)
  }

  override fun enabled(enabled: Boolean) {
    value.enabled = enabled
  }

  private val clickedPointer = sel_registerName("clicked")

  @ObjCAction
  fun clicked() {
    onClick!!.invoke()
  }

  private var onClick: (() -> Unit)? = null
  override fun onClick(onClick: (() -> Unit)?) {
    this.onClick = onClick
    if (onClick != null) {
      value.addTarget(this, clickedPointer, UIControlEventTouchUpInside)
    } else {
      value.removeTarget(this, clickedPointer, UIControlEventTouchUpInside)
    }
  }
}
