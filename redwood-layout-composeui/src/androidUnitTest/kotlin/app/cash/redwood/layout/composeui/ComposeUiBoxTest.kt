/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.layout.composeui

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.Color as ColorWidget
import app.cash.redwood.layout.widget.Box
import com.android.resources.LayoutDirection
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ComposeUiBoxTest(
  @TestParameter layoutDirection: LayoutDirection,
) : AbstractBoxTest<@Composable () -> Unit>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6.copy(layoutDirection = layoutDirection),
    theme = "android:Theme.Material.Light.NoActionBar",
    supportsRtl = true,
  )

  override fun Box(): Box<@Composable () -> Unit> = ComposeUiBox(0x88000000.toInt())

  override fun Color(): ColorWidget<@Composable () -> Unit> = ComposeUiColor()

  override fun verifySnapshot(value: @Composable () -> Unit) {
    paparazzi.snapshot(composable = value)
  }
}
