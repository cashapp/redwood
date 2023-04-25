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

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.MonotonicFrameClock
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolMismatchHandler
import app.cash.redwood.treehouse.AppLifecycle.Host
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.serialization.json.Json

public class StandardAppLifecycle(
  internal val protocolBridgeFactory: ProtocolBridge.Factory,
  internal val json: Json,
  internal val widgetVersion: UInt,
) : AppLifecycle {
  internal val coroutineScope: CoroutineScope = GlobalScope

  private var broadcastFrameClock: BroadcastFrameClock? = null

  public val frameClock: MonotonicFrameClock
    get() = broadcastFrameClock ?: error("AppLifecycle not started yet")

  private var _mismatchHandler: ProtocolMismatchHandler? = null
  internal val mismatchHandler: ProtocolMismatchHandler
    get() = _mismatchHandler ?: error("AppLifecycle not started yet")

  override fun start(host: Host) {
    broadcastFrameClock = BroadcastFrameClock { host.requestFrame() }
    _mismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag) {
        host.onUnknownEvent(widgetTag, tag)
      }

      override fun onUnknownEventNode(id: Id, tag: EventTag) {
        host.onUnknownEventNode(id, tag)
      }
    }
  }

  override fun sendFrame(timeNanos: Long) {
    val frameClock = broadcastFrameClock ?: error("AppLifecycle not started yet")
    frameClock.sendFrame(timeNanos)
  }
}
