/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.ui.basic.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.ui.basic.api.TextFieldState
import app.cash.redwood.ui.basic.widget.TextInput
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UITextAutocapitalizationType.UITextAutocapitalizationTypeNone
import platform.UIKit.UITextBorderStyle.UITextBorderStyleRoundedRect
import platform.UIKit.UITextField
import platform.UIKit.UIView
import platform.darwin.NSObject

internal class UIViewTextInput : TextInput<UIView> {
  override val value = UITextField().apply {
    borderStyle = UITextBorderStyleRoundedRect
    autocapitalizationType = UITextAutocapitalizationTypeNone
  }

  private var updating = false
  private var state = TextFieldState()
  private var onChange: ((TextFieldState) -> Unit)? = null

  private val stateChangedTarget = StateChangedTarget(this)
  private val stateChangedPointer = NSSelectorFromString(StateChangedTarget::stateChanged.name)

  override fun state(state: TextFieldState) {
    if (state.userEditCount > this.state.userEditCount) {
      return
    }
    require(!updating)
    updating = true
    this.state = state
    value.text = state.text
    updating = false
  }

  override fun hint(hint: String) {
    value.placeholder = hint
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    if (this.onChange == null) {
      if (onChange != null) {
        value.addTarget(stateChangedTarget, stateChangedPointer, UIControlEventEditingChanged)
      }
    } else if (onChange == null) {
      value.removeTarget(stateChangedTarget, stateChangedPointer, UIControlEventEditingChanged)
    }
    this.onChange = onChange
  }

  private fun stateChanged() {
    // Ignore this update if it isn't a user edit.
    if (updating) return

    val newState = state.userEdit(value.text ?: "", 0, 0)
    if (state != newState) {
      state = newState
      onChange?.invoke(newState)
    }
  }

  /**
   * Selectors can only target subtypes of [NSObject] which is why this helper exists.
   * We cannot subclass it on [UIViewTextInput] directly because it implements a Kotlin
   * interface, but we also don't want to leak it into public API. The contained function
   * must be public for the selector to work.
   */
  private class StateChangedTarget(private val target: UIViewTextInput) : NSObject() {
    @ObjCAction
    fun stateChanged() {
      target.stateChanged()
    }
  }

  override var modifier: Modifier = Modifier
}
