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

import app.cash.redwood.protocol.Change
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.core.api.FocusRequester
import app.cash.zipline.ZiplineApiMismatchException
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.flow.StateFlow

internal class GuestFocusDirectorTest {
  @Test
  fun requestFocus() {
    val host = FakeHost()
    val focusRequester = GuestFocusDirector(host)

    val nameFocusRequester = focusRequester.newFocusRequester()
    val colorFocusRequester = focusRequester.newFocusRequester()

    nameFocusRequester.requestFocus()
    assertThat(host.focused).isEqualTo(nameFocusRequester)

    colorFocusRequester.requestFocus()
    assertThat(host.focused).isEqualTo(colorFocusRequester)
  }

  @Test
  fun requestFocusOnHostThatDoesntImplementFocus() {
    val host = NullHost()
    val focusRequester = GuestFocusDirector(host)

    val nameFocusRequester = focusRequester.newFocusRequester()

    nameFocusRequester.requestFocus() // No crash is all we need!
  }

  @Test
  fun hideSoftwareKeyboard() {
    val host = FakeHost()
    val focusRequester = GuestFocusDirector(host)

    focusRequester.hideSoftwareKeyboard()
    assertThat(host.softwareKeyboardHidden).isTrue()
  }

  @Test
  fun hideSoftwareKeyboardOnHostThatDoesntImplementFocus() {
    val host = NullHost()
    val focusRequester = GuestFocusDirector(host)

    focusRequester.hideSoftwareKeyboard() // No crash is all we need!
  }

  /**
   * This throws [app.cash.zipline.ZiplineApiMismatchException], the same exception Zipline throws when calling a
   * function that the host doesn't have.
   */
  private open class NullHost : ZiplineTreehouseUi.Host {
    override val uiConfigurations: StateFlow<UiConfiguration>
      get() = throw ZiplineApiMismatchException("unexpected call")
    override val stateSnapshot: StateSnapshot?
      get() = throw ZiplineApiMismatchException("unexpected call")

    override fun addOnBackPressedCallback(onBackPressedCallbackService: OnBackPressedCallbackService): CancellableService {
      throw ZiplineApiMismatchException("unexpected call")
    }

    override fun hideSoftwareKeyboard() {
      throw ZiplineApiMismatchException("unexpected call")
    }

    override fun requestFocus(focusRequester: FocusRequester) {
      throw ZiplineApiMismatchException("unexpected call")
    }

    override fun sendChanges(changes: List<Change>) {
      throw ZiplineApiMismatchException("unexpected call")
    }
  }

  private class FakeHost : NullHost() {
    var focused: FocusRequester? = null
    var softwareKeyboardHidden = false

    override fun hideSoftwareKeyboard() {
      softwareKeyboardHidden = true
    }

    override fun requestFocus(focusRequester: FocusRequester) {
      this.focused = focusRequester
    }
  }
}
