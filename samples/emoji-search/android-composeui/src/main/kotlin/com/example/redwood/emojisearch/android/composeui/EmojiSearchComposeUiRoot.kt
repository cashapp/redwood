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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.redwood.treehouse.composeui.ComposeUiRoot
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class EmojiSearchComposeUiRoot(
  private val scope: CoroutineScope,
) : ComposeUiRoot() {
  private var loadCount by mutableIntStateOf(0)
  private var attached by mutableStateOf(false)
  private var uncaughtException by mutableStateOf<Throwable?>(null)
  private var restart: (() -> Unit)? = null

  override fun contentState(
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
    this.loadCount = loadCount
    this.attached = attached
    this.uncaughtException = uncaughtException

    if (uncaughtException != null) {
      scope.launch {
        delay(2_000.milliseconds)
        restart?.invoke()
      }
    }
  }

  override fun restart(restart: (() -> Unit)?) {
    this.restart = restart
  }

  @Composable
  override fun Render() {
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
  }
}
