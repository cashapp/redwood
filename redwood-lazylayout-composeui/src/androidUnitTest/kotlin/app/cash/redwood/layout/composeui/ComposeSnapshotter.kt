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
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.layout.Snapshotter

class ComposeSnapshotter(
  private val paparazzi: Paparazzi,
  private val widget: @Composable () -> Unit,
) : Snapshotter {
  override fun snapshot(name: String?) {
    paparazzi.snapshot(name, widget)
  }
}
