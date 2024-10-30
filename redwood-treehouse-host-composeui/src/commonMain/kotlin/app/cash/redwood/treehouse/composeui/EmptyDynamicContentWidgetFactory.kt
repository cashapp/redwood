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
import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.Crashed
import app.cash.redwood.treehouse.DynamicContentWidgetFactory
import app.cash.redwood.treehouse.Loading

/** Don't show anything for loading or error screens. */
internal object EmptyDynamicContentWidgetFactory :
  DynamicContentWidgetFactory<@Composable () -> Unit> {
  override fun Loading() = EmptyLoading()

  override fun Crashed() = EmptyCrashed()

  internal class EmptyLoading : Loading<@Composable () -> Unit> {
    override var modifier: Modifier = Modifier
    override val value = @Composable {
    }
  }

  internal class EmptyCrashed : Crashed<@Composable () -> Unit> {
    override var modifier: Modifier = Modifier
    override val value = @Composable {
    }

    override fun uncaughtException(uncaughtException: Throwable) {
    }

    override fun restart(restart: () -> Unit) {
    }
  }
}
