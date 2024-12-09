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
package app.cash.redwood.treehouse

import com.example.redwood.testapp.treehouse.HostApi
import kotlinx.coroutines.channels.Channel

class FakeHostApi : HostApi {
  private val messagesChannel = Channel<String>(capacity = Int.MAX_VALUE)

  override suspend fun httpCall(url: String, headers: Map<String, String>): String {
    error("unexpected call")
  }

  override fun log(message: String) {
    messagesChannel.trySend(message)
  }

  suspend fun takeMessage(): String {
    return messagesChannel.receive()
  }
}
