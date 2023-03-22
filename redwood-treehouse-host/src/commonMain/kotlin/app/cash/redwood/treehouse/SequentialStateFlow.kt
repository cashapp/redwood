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
package app.cash.redwood.treehouse

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

internal class SequentialStateFlow<T>(
  first: StateFlow<T>,
) : StateFlow<T> {
  public val stateFlowValue: MutableStateFlow<StateFlow<T>> = MutableStateFlow(first)

  override val replayCache: List<T> = listOf()

  override suspend fun collect(collector: FlowCollector<T>): Nothing {
    stateFlowValue.collectLatest { stateFlow ->
      stateFlow.collect {
        collector.emit(it)
      }
    }
    error("stateFlow.collect never returns")
  }

  override val value: T
    get() = stateFlowValue.value.value
}
