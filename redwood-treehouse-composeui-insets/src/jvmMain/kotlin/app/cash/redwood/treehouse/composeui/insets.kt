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
@file:JvmName("insetsJvm")

package app.cash.redwood.treehouse.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import app.cash.redwood.layout.api.Margin
import java.awt.GraphicsEnvironment
import java.awt.Insets
import java.awt.Toolkit

@Composable
public actual fun safeAreaInsets(): Margin {
  val configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .defaultScreenDevice.defaultConfiguration
  val rawSpacing = Toolkit.getDefaultToolkit().getScreenInsets(configuration).toMargin()
  return rawSpacing / LocalDensity.current.density.toDouble()
}

private fun Insets.toMargin() = Margin(
  start = left.toDouble(),
  end = right.toDouble(),
  top = top.toDouble(),
  bottom = bottom.toDouble(),
)
