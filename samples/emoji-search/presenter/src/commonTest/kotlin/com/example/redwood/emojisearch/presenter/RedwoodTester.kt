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

package com.example.redwood.emojisearch.presenter

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.CoroutineScope

/**
 * Performs Redwood composition strictly for testing.
 *
 * Create an instance with a generated `AppNameTester()` function.
 */
@OptIn(RedwoodCodegenApi::class)
class RedwoodTester @RedwoodCodegenApi constructor(
  provider: Widget.Provider<MutableWidget>,
) {
  /** Run enqueued jobs synchronously by manually kicking this. */
  private val coroutineDispatcher = ManualCoroutineDispatcher()

  /** Emit frames manually. */
  private val clock = BroadcastFrameClock()
  private val timeNanos = 0L

  private val scope = CoroutineScope(coroutineDispatcher + clock)

  /** Top-level children of the composition. */
  private val mutableChildren = mutableListOf<Widget<MutableWidget>>()

  private val composition = RedwoodComposition(
    scope = scope,
    container = MutableListChildren(mutableChildren),
    provider = provider,
  )

  fun setContent(content: @Composable () -> Unit) {
    composition.setContent(content)
  }

  fun snapshot(): List<WidgetValue> {
    clock.sendFrame(timeNanos)
    coroutineDispatcher.executeQueuedJobs()
    return mutableChildren.map { it.value.snapshot() }
  }
}
