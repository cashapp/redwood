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
@file:JvmName("insetsAndroid")

package app.cash.redwood.treehouse.composeui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.fromPlatformDp

@Composable
internal actual fun safeAreaInsets(): Margin {
  val layoutDirection = LocalLayoutDirection.current
  return WindowInsets.safeDrawing.asPaddingValues().toMargin(layoutDirection)
}

@Composable
private fun PaddingValues.toMargin(layoutDirection: LayoutDirection) = Margin(
  start = Dp.fromPlatformDp(calculateLeftPadding(layoutDirection).value.toDouble()),
  end = Dp.fromPlatformDp(calculateRightPadding(layoutDirection).value.toDouble()),
  top = Dp.fromPlatformDp(calculateTopPadding().value.toDouble()),
  bottom = Dp.fromPlatformDp(calculateBottomPadding().value.toDouble()),
)
