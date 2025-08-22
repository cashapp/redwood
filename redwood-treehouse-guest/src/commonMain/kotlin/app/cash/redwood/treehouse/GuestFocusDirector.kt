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
package app.cash.redwood.treehouse

import app.cash.redwood.ui.core.api.FocusDirector
import app.cash.redwood.ui.core.api.FocusRequester
import app.cash.zipline.ZiplineApiMismatchException

/** This forwards focus requests to the host. */
internal class GuestFocusDirector(
  private val host: ZiplineTreehouseUi.Host,
) : FocusDirector {
  private var nextFocusRequesterId = 3000

  override fun hideSoftwareKeyboard() {
    try {
      host.hideSoftwareKeyboard()
    } catch (_: ZiplineApiMismatchException) {
      // Silently drop hideSoftwareKeyboard() calls if the host doesn't have that API.
    }
  }

  override fun newFocusRequester(): FocusRequester = GuestFocusRequester()

  @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // For SerializableFocusRequester.
  private inner class GuestFocusRequester : app.cash.redwood.ui.core.api.SerializableFocusRequester {
    override val id = nextFocusRequesterId++
    override fun requestFocus() {
      try {
        host.requestFocus(this)
      } catch (_: ZiplineApiMismatchException) {
        // Silently drop requestFocus() calls if the host doesn't have that API.
      }
    }
  }
}
