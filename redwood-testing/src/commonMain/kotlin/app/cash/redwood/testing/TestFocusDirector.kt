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
package app.cash.redwood.testing

import app.cash.redwood.ui.core.api.FocusDirector
import app.cash.redwood.ui.core.api.FocusRequester
import app.cash.redwood.ui.core.modifier.FocusRequester as FocusRequesterElement

/**
 * A [FocusDirector] for use with [TestRedwoodComposition].
 *
 * Instead of tracking a focus state on each widget, focus state is held by this instance.
 */
public class TestFocusDirector : FocusDirector {
  /** The currently focused _requester_ which we can use to find the focused widget. */
  private var focused: TestFocusRequester? = null

  override fun hideSoftwareKeyboard() {
  }

  override fun newFocusRequester(): FocusRequester = TestFocusRequester()

  public fun getFocused(widgets: List<WidgetValue>): WidgetValue? {
    val focused = this.focused ?: return null // Nothing focused.
    return widgets.flatten().firstOrNull { it.focusRequester == focused }
  }

  /** Digs through this widget's modifier elements to find its focus requester. */
  private val WidgetValue.focusRequester: FocusRequester?
    get() {
      var result: FocusRequester? = null
      modifier.forEachUnscoped { element ->
        if (result != null) return@forEachUnscoped // Already found it.
        val matchingElement = (element as? FocusRequesterElement) ?: return@forEachUnscoped
        result = matchingElement.value
      }
      return result
    }

  private inner class TestFocusRequester : FocusRequester {
    override fun requestFocus() {
      focused = this
    }
  }
}
