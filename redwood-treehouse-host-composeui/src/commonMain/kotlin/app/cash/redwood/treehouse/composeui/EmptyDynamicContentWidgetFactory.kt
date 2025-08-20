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
package app.cash.redwood.treehouse.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.treehouse.Crashed
import app.cash.redwood.treehouse.DynamicContentWidgetFactory
import app.cash.redwood.treehouse.Loading
import app.cash.redwood.widget.Widget

/** Don't show anything for loading or error screens. */
internal object EmptyDynamicContentWidgetFactory :
  DynamicContentWidgetFactory<@Composable (Modifier) -> Unit> {
  override fun Loading() = EmptyLoading()

  override fun Crashed() = EmptyCrashed()

  internal class EmptyLoading : Loading<@Composable (Modifier) -> Unit> {
    override var modifier: RedwoodModifier = RedwoodModifier
    override val allChildren: List<Widget.Children<@Composable ((Modifier) -> Unit)>>
      get() = listOf()
    override val value: @Composable (Modifier) -> Unit = {
    }
  }

  internal class EmptyCrashed : Crashed<@Composable (Modifier) -> Unit> {
    override var modifier: RedwoodModifier = RedwoodModifier
    override val allChildren: List<Widget.Children<@Composable ((Modifier) -> Unit)>>
      get() = listOf()
    override val value: @Composable (Modifier) -> Unit = {
    }

    override fun uncaughtException(uncaughtException: Throwable) {
    }

    override fun restart(restart: () -> Unit) {
    }
  }
}
