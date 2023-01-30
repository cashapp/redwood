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
package app.cash.redwood.treehouse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import app.cash.redwood.compose.WidgetVersion

/**
 * Provide the configuration of the host display.
 * This value will be bound automatically when Treehouse is used.
 * Custom values should only be provided into a composition for testing purposes!
 *
 * @see WidgetVersion
 */
public val LocalHostConfiguration: ProvidableCompositionLocal<HostConfiguration> =
  compositionLocalOf {
    throw AssertionError("HostConfiguration was not provided!")
  }

/**
 * Expose various configuration properties of the host.
 *
 * @see HostConfiguration
 */
@Suppress("unused") // Emulating a CompositionLocal.
public val HostConfiguration.Companion.current: HostConfiguration
  @Composable
  @ReadOnlyComposable
  get() = LocalHostConfiguration.current
