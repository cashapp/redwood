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
package app.cash.redwood.composeui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.cash.redwood.ui.OnBackPressedCallback
import assertk.assertThat
import assertk.assertions.hasSize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class AndroidOnBackPressedDispatcherTest {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun disabledToEnabledOnBackPressedCallback() {
    composeTestRule.setContent {
      val onBackPressedDispatcher = platformOnBackPressedDispatcher()
      val onBackPressedCallback = FakeOnBackPressedCallback(enabled = false)
      onBackPressedDispatcher.addCallback(onBackPressedCallback)
      composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      assertThat(onBackPressedCallback.handleOnBackPressedEvents).hasSize(0)
      onBackPressedCallback.isEnabled = true
      composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
      assertThat(onBackPressedCallback.handleOnBackPressedEvents).hasSize(1)
    }
  }
}

private class FakeOnBackPressedCallback(enabled: Boolean) : OnBackPressedCallback(enabled) {
  private val _handleOnBackPressedEvents = ArrayDeque<Unit>()
  val handleOnBackPressedEvents: List<Unit> = _handleOnBackPressedEvents

  override fun handleOnBackPressed() {
    _handleOnBackPressedEvents += Unit
  }
}
