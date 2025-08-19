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
package app.cash.redwood.widget

import app.cash.redwood.ui.core.api.FocusDirector
import app.cash.redwood.ui.core.api.FocusRequester
import org.w3c.dom.HTMLElement

// TODO(jwilson): complete this.
internal class HTMLFocusDirector(
  private val element: HTMLElement,
) : FocusDirector {
  override fun hideSoftwareKeyboard() {
  }

  override fun newFocusRequester(): FocusRequester {
    return object : FocusRequester {
      override fun requestFocus() {
      }
    }
  }
}
