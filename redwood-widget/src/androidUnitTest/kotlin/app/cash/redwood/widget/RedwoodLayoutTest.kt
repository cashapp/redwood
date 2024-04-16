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
package app.cash.redwood.widget

import androidx.activity.ComponentActivity
import app.cash.redwood.ui.OnBackPressedCallback
import assertk.assertThat
import assertk.assertions.hasSize
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class RedwoodLayoutTest {
  private val activity = Robolectric.buildActivity(ComponentActivity::class.java).resume().visible().get()

  @Test
  fun disabledToEnabledOnBackPressedCallback() {
    val layout = RedwoodLayout(activity, activity.onBackPressedDispatcher)
    val onBackPressedCallback = FakeOnBackPressedCallback(enabled = false)
    layout.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    activity.onBackPressedDispatcher.onBackPressed()
    assertThat(onBackPressedCallback.handleOnBackPressedEvents).hasSize(0)
    onBackPressedCallback.isEnabled = true
    activity.onBackPressedDispatcher.onBackPressed()
    assertThat(onBackPressedCallback.handleOnBackPressedEvents).hasSize(1)
  }
}

private class FakeOnBackPressedCallback(enabled: Boolean) : OnBackPressedCallback(enabled) {
  private val _handleOnBackPressedEvents = ArrayDeque<Unit>()
  val handleOnBackPressedEvents: List<Unit> = _handleOnBackPressedEvents

  override fun handleOnBackPressed() {
    _handleOnBackPressedEvents += Unit
  }
}
