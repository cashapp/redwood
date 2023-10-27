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
package app.cash.redwood.treehouse

import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher

internal class FakeOnBackPressedDispatcher : OnBackPressedDispatcher {
  private val mutableCallbacks = mutableListOf<OnBackPressedCallback>()

  val callbacks: List<OnBackPressedCallback>
    get() = mutableCallbacks.toList()

  override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
    mutableCallbacks += onBackPressedCallback

    return object : Cancellable {
      override fun cancel() {
        mutableCallbacks -= onBackPressedCallback
      }
    }
  }

  fun onBack() {
    // Only one callback should handle each back press.
    val callbackToNotify = mutableCallbacks.lastOrNull {
      it.isEnabled
    }
    callbackToNotify?.handleOnBackPressed()
  }
}
