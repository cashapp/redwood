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
package app.cash.redwood.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import app.cash.redwood.ui.UiConfiguration

/**
 * Provide various configurations of the UI.
 * This value will be bound automatically.
 * Custom values should only be provided into a composition for testing purposes!
 *
 * @see UiConfiguration.Companion.current
 */
public val LocalUiConfiguration: ProvidableCompositionLocal<UiConfiguration> =
  compositionLocalOf {
    throw AssertionError("UiConfiguration was not provided!")
  }

/**
 * Expose various configuration properties of the host.
 *
 * @see UiConfiguration
 */
@Suppress("unused") // Emulating a CompositionLocal.
public val UiConfiguration.Companion.current: UiConfiguration
  @Composable
  @ReadOnlyComposable
  get() = LocalUiConfiguration.current
