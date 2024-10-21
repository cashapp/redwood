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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlinx.coroutines.CoroutineScope

public open class DynamicContent {
  public var loadCount: Int by mutableIntStateOf(0)
    private set
  public var attached: Boolean by mutableStateOf(false)
    private set
  public var uncaughtException: Throwable? by mutableStateOf(null)
    private set
  public var restart: (() -> Unit)? by mutableStateOf(null)
    private set

  public open fun contentState(
    scope: CoroutineScope,
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
    this.loadCount = loadCount
    this.attached = attached
    this.uncaughtException = uncaughtException
  }

  public fun restart(restart: (() -> Unit)?) {
    this.restart = restart
  }

  @Composable
  public open fun Render(children: ComposeWidgetChildren) {
    children.Render()
  }
}
