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

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.channels.Channel

/** Track side-effects for testing. */
class EventLog {
  private val events = Channel<String>(capacity = Int.MAX_VALUE)

  operator fun plusAssign(event: String) {
    events.trySend(event)
  }

  suspend fun takeEvent(): String {
    return events.receive()
  }

  suspend fun takeEvent(event: String) {
    assertThat(takeEvent()).isEqualTo(event)
  }

  fun assertNoEvents() {
    val received = events.tryReceive()
    check(received.isFailure) {
      "expected no events but was ${received.getOrNull()}"
    }
  }

  /** Discard all events. */
  fun clear() {
    while (true) {
      if (!events.tryReceive().isSuccess) break
    }
  }
}
