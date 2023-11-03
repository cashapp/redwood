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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

internal class FakeCodeHost(
  private val eventLog: EventLog,
  private val eventPublisher: EventPublisher,
  private val dispatchers: TreehouseDispatchers,
  private val appScope: CoroutineScope,
  frameClockFactory: FrameClock.Factory,
) : CodeHost<FakeAppService>(
  dispatchers = dispatchers,
  appScope = appScope,
  frameClockFactory = frameClockFactory,
  stateStore = MemoryStateStore(),
) {
  private var codeSessions: Channel<CodeSession<FakeAppService>>? = null

  /**
   * Create a new channel every time we subscribe to code updates. The channel will be closed when
   * the superclass is done consuming the flow.
   */
  override fun codeUpdatesFlow(): Flow<CodeSession<FakeAppService>> {
    eventLog += "codeHost.collectCodeUpdates()"
    val channel = Channel<CodeSession<FakeAppService>>(Int.MAX_VALUE)
    codeSessions = channel
    return channel.consumeAsFlow()
  }

  suspend fun startCodeSession(name: String): CodeSession<FakeAppService> {
    val result = FakeCodeSession(dispatchers, eventPublisher, eventLog, name, appScope)
    codeSessions!!.send(result)
    return result
  }
}
