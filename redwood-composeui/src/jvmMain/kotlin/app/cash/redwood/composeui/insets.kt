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

package app.cash.redwood.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import java.awt.GraphicsEnvironment
import java.awt.Insets
import java.awt.Toolkit

@Composable
public actual fun safeAreaInsets(): Margin {
  val configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .defaultScreenDevice.defaultConfiguration
  val density = Density(LocalDensity.current.density.toDouble())
  return Toolkit.getDefaultToolkit().getScreenInsets(configuration).toMargin(density)
}

private fun Insets.toMargin(density: Density) = with(density) {
  Margin(left.toDp(), right.toDp(), top.toDp(), bottom.toDp())
}
