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
package com.example.redwood.emojisearch.android.composeui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.redwood.treehouse.composeui.DynamicContent
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class EmojiSearchComposeUiRoot : DynamicContent() {
  override fun contentState(
    scope: CoroutineScope,
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
    super.contentState(scope, loadCount, attached, uncaughtException)

    if (uncaughtException != null) {
      scope.launch {
        delay(2_000.milliseconds)
        restart?.invoke()
      }
    }
  }

  @Composable
  override fun Render(children: ComposeWidgetChildren) {
    val uncaughtException = this.uncaughtException
    if (uncaughtException != null) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        BasicText(uncaughtException.stackTraceToString())
      }
      return
    }

    if (!attached) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        BasicText("loading...")
      }
      return
    }

    super.Render(children)
  }
}
