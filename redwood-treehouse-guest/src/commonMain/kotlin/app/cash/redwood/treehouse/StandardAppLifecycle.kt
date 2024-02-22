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
import app.cash.redwood.protocol.guest.ProtocolBridge
import app.cash.redwood.protocol.guest.ProtocolMismatchHandler
import app.cash.redwood.treehouse.AppLifecycle.Host
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

public class StandardAppLifecycle(
  internal val protocolBridgeFactory: ProtocolBridge.Factory,
  internal val json: Json,
  internal val widgetVersion: UInt,
) : AppLifecycle {
  private var started = false
  private lateinit var host: Host

  private val broadcastFrameClock: BroadcastFrameClock = BroadcastFrameClock {
    if (started) {
      host.requestFrame()
    }
  }
  public val frameClock: MonotonicFrameClock get() = broadcastFrameClock

  internal val mismatchHandler: ProtocolMismatchHandler = object : ProtocolMismatchHandler {
    override fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag) {
      host.onUnknownEvent(widgetTag, tag)
    }

    override fun onUnknownEventNode(id: Id, tag: EventTag) {
      host.onUnknownEventNode(id, tag)
    }
  }

  private val coroutineExceptionHandler = object : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
      get() = CoroutineExceptionHandler.Key

    override fun handleException(context: CoroutineContext, exception: Throwable) {
      host.handleUncaughtException(exception)
    }
  }

  internal val coroutineScope = CoroutineScope(coroutineExceptionHandler)

  override fun start(host: Host) {
    check(!started) { "already started" }
    this.started = true
    this.host = host

    prepareEnvironment(coroutineExceptionHandler)
  }

  override fun sendFrame(timeNanos: Long) {
    broadcastFrameClock.sendFrame(timeNanos)
  }
}
