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

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable

/** Extremely basic dispatcher that requires [executeQueuedJobs] to be invoked manually. */
internal class ManualCoroutineDispatcher : CoroutineDispatcher() {
  private val queue = ArrayDeque<Runnable>()

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    queue += block
  }

  fun executeQueuedJobs() {
    while (true) {
      val runnable = queue.removeFirstOrNull() ?: return
      runnable.run()
    }
  }
}
