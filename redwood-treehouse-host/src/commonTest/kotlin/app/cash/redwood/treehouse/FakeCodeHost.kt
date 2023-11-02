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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

internal class FakeCodeHost(
  private val eventLog: EventLog,
  private val eventPublisher: EventPublisher,
  dispatchers: TreehouseDispatchers,
  appScope: CoroutineScope,
  frameClockFactory: FrameClock.Factory,
) : CodeHost<FakeAppService>(
  dispatchers = dispatchers,
  appScope = appScope,
  frameClockFactory = frameClockFactory,
  stateStore = MemoryStateStore(),
) {
  private val codeSessions = MutableStateFlow<CodeSession<FakeAppService>?>(null)

  override fun codeUpdatesFlow(): Flow<CodeSession<FakeAppService>> {
    return codeSessions.filterNotNull()
  }

  fun startCodeSession(name: String): CodeSession<FakeAppService> {
    val result = FakeCodeSession(eventLog, name, eventPublisher)
    codeSessions.value = result
    return result
  }
}
