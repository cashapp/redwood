/*
 * Copyright (C) 2022 Square, Inc.
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
@file:JvmName("Main")

package com.example.redwood.counter.desktop

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.cash.redwood.composeui.RedwoodContent
import app.cash.redwood.ui.basic.composeui.ComposeUiRedwoodUiBasicWidgetSystem
import app.cash.redwood.ui.basic.composeui.RedwoodUiBasicTheme
import coil3.ImageLoader
import coil3.PlatformContext
import com.example.redwood.counter.presenter.Counter

fun main() {
  val widgetSystem = ComposeUiRedwoodUiBasicWidgetSystem(ImageLoader(PlatformContext.INSTANCE))
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "Counter",
      state = rememberWindowState(width = 300.dp, height = 300.dp),
    ) {
      RedwoodUiBasicTheme {
        RedwoodContent(widgetSystem, modifier = Modifier.padding(16.dp)) {
          Counter()
        }
      }
    }
  }
}
