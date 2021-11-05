/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

suspend fun BroadcastFrameClock.awaitFrame() {
  // TODO Remove the need for two frames to happen!
  //  I think this is because of the diff-sender is a hot loop that immediately reschedules
  //  itself on the clock. This schedules it ahead of the coroutine which applies changes and
  //  so we need to trigger an additional frame to actually emit the change's diffs.
  repeat(2) {
    coroutineScope {
      launch(start = UNDISPATCHED) {
        withFrameMillis { }
      }
      sendFrame(0L)
    }
  }
}

val String.json: JsonElement get() = Json.encodeToJsonElement(String.serializer(), this)
val Boolean.json: JsonElement get() = Json.encodeToJsonElement(Boolean.serializer(), this)
