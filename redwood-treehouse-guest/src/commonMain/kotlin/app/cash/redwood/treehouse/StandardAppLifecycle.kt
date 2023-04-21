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

import androidx.compose.runtime.MonotonicFrameClock
import app.cash.redwood.protocol.compose.ProtocolBridge
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

  private var ziplineFrameClock: ZiplineFrameClock? = null

  public val frameClock: MonotonicFrameClock
    get() = ziplineFrameClock ?: error("AppLifecycle not started yet")

  override fun start(host: Host) {
    ziplineFrameClock = ZiplineFrameClock(host)
  }

  override fun sendFrame(timeNanos: Long) {
    val frameClock = ziplineFrameClock ?: error("AppLifecycle not started yet")
    frameClock.sendFrame(timeNanos)
  }
}
