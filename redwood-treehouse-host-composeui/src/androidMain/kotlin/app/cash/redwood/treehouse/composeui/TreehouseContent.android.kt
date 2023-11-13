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
package app.cash.redwood.treehouse.composeui

import androidx.activity.OnBackPressedCallback as AndroidOnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback as RedwoodOnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher as RedwoodOnBackPressedDispatcher

@Composable
internal actual fun platformOnBackPressedDispatcher(): RedwoodOnBackPressedDispatcher {
  val delegate = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
  return remember(delegate) {
    object : RedwoodOnBackPressedDispatcher {
      override fun addCallback(onBackPressedCallback: RedwoodOnBackPressedCallback): Cancellable {
        val androidOnBackPressedCallback = onBackPressedCallback.toAndroid()
        onBackPressedCallback.enabledChangedCallback = {
          androidOnBackPressedCallback.isEnabled = onBackPressedCallback.isEnabled
        }
        delegate.addCallback(androidOnBackPressedCallback)
        return object : Cancellable {
          override fun cancel() {
            onBackPressedCallback.enabledChangedCallback = null
            androidOnBackPressedCallback.remove()
          }
        }
      }
    }
  }
}

private fun RedwoodOnBackPressedCallback.toAndroid(): AndroidOnBackPressedCallback =
  object : AndroidOnBackPressedCallback(this@toAndroid.isEnabled) {
    override fun handleOnBackPressed() {
      this@toAndroid.handleOnBackPressed()
    }
  }
