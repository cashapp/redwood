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
package app.cash.redwood.widget

import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import kotlin.native.ObjCName
import kotlinx.coroutines.flow.StateFlow

@ObjCName("RedwoodView", exact = true)
public interface RedwoodView<W : Any> {
  public val children: Widget.Children<W>
  public val onBackPressedDispatcher: OnBackPressedDispatcher
  public val uiConfiguration: StateFlow<UiConfiguration>
  public val savedStateRegistry: SavedStateRegistry?

  /**
   * The insets of the viewport that the composition is responsible for consuming. This value is
   * passed to the composition as [UiConfiguration.windowInsets].
   */
  public var windowInsets: Margin

  /**
   * This should at minimum clear all [children].
   *
   * Invoke when switching the backing composition to prepare the view for an initial load.
   */
  public fun reset()
}
