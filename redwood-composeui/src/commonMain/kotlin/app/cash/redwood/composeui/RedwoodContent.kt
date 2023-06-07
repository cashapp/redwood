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
package app.cash.redwood.composeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.compose.ComposeWidgetChildren

/** Render a Redwood composition inside of Compose UI. */
@Composable
public fun RedwoodContent(
  provider: Widget.Provider<@Composable () -> Unit>,
  content: @Composable () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val children = remember(provider, content) { ComposeWidgetChildren() }
  LaunchedEffect(provider, content) {
    val composition = RedwoodComposition(
      scope = scope,
      container = children,
      provider = provider,
    )
    composition.setContent(content)
  }
  children.render()
}
