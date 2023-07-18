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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractSpacerTest
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComposeUiSpacerTest : AbstractSpacerTest<@Composable () -> Unit>() {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    renderingMode = SessionParams.RenderingMode.SHRINK,
  )

  override fun widget(
    width: Int,
    height: Int,
    modifier: Modifier,
  ): Spacer<@Composable () -> Unit> = ComposeUiSpacer().apply {
    this.modifier = modifier
    width(width.dp)
    height(height.dp)
  }

  override fun wrap(widget: Widget<@Composable () -> Unit>, horizontal: Boolean) = @Composable {
    if (horizontal) {
      Row {
        BasicText("Text 1")
        widget.value()
        BasicText("Text 2")
      }
    } else {
      Column {
        BasicText("Text 1")
        widget.value()
        BasicText("Text 2")
      }
    }
  }

  override fun verifySnapshot(value: @Composable () -> Unit) {
    paparazzi.snapshot(composable = value)
  }
}
