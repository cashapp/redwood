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

/**
 * Provide the version of widgets in use.
 * This value will be bound automatically when a protocol-based display is used.
 * Custom values should only be provided into a composition for testing purposes!
 *
 * @see WidgetVersion
 */
public val LocalWidgetVersion: ProvidableCompositionLocal<UInt> = compositionLocalOf {
  // A real value is always provided to a protocol-based composition.
  // When connected directly (i.e., without the protocol indirection) always assume latest.
  UInt.MAX_VALUE
}

/**
 * Exposes the version of the widgets for conditional logic (such as progressive enhancement).
 *
 * If you have `Button` introduced in version 1 and `FancyButton` introduced in version 3 but your
 * minimum-supported version is 2, users of `FancyButton` will need to use a conditional around
 * `FancyButton` to fallback to `Button`.
 * ```kotlin
 * if (WidgetVersion >= 3) {
 *   FancyButton("text")
 * } else {
 *   Button("text")
 * }
 * ```
 */
public val WidgetVersion: UInt
  @Composable
  @ReadOnlyComposable
  get() = LocalWidgetVersion.current
