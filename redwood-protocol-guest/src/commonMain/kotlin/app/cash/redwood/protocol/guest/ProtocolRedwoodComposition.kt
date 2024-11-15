/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.protocol.guest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.saveable.SaveableStateRegistry
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
@Suppress("FunctionName")
public fun ProtocolRedwoodComposition(
  scope: CoroutineScope,
  guestAdapter: GuestProtocolAdapter,
  widgetVersion: UInt,
  onBackPressedDispatcher: OnBackPressedDispatcher,
  saveableStateRegistry: SaveableStateRegistry?,
  uiConfigurations: StateFlow<UiConfiguration>,
): RedwoodComposition {
  val composition = RedwoodComposition(
    scope = scope,
    container = guestAdapter.root,
    onBackPressedDispatcher = onBackPressedDispatcher,
    saveableStateRegistry = saveableStateRegistry,
    uiConfigurations = uiConfigurations,
    widgetSystem = guestAdapter.widgetSystem,
  )
  return ProtocolRedwoodComposition(composition, widgetVersion)
}

private class ProtocolRedwoodComposition(
  private val composition: RedwoodComposition,
  private val widgetVersion: UInt,
) : RedwoodComposition by composition {
  override fun setContent(content: @Composable () -> Unit) {
    composition.setContent {
      CompositionLocalProvider(LocalWidgetVersion provides widgetVersion) {
        content()
      }
    }
  }
}
