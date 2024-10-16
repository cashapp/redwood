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
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.compose.ComposeWidgetChildren

/**
 * A default base implementation of [RedwoodView.Root].
 *
 * This composition contributes nothing to the view hierarchy. It delegates directly to its child
 * views.
 */
public open class ComposeUiRoot : RedwoodView.Root<@Composable () -> Unit> {
  override val children: ComposeWidgetChildren = ComposeWidgetChildren()

  override var modifier: Modifier = Modifier

  override fun contentState(
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
  }

  override fun restart(restart: (() -> Unit)?) {
  }

  override val value: @Composable () -> Unit = {
    Render()
  }

  @Composable
  public open fun Render() {
    children.Render()
  }
}
