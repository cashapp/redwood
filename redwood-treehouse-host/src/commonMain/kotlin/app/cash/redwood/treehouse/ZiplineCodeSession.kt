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

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineScope
import app.cash.zipline.withScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ZiplineCodeSession<A : AppService>(
  private val codeHost: CodeHost<*>,
  private val dispatchers: TreehouseDispatchers,
  private val eventPublisher: EventPublisher,
  private val appScope: CoroutineScope,
  private val frameClock: FrameClock,
  private val sessionScope: CoroutineScope,
  override val appService: A,
  val zipline: Zipline,
) : CodeSession<A> {
  private val ziplineScope = ZiplineScope()

  override val json: Json
    get() = zipline.json

  override fun start() {
    frameClock.start(sessionScope, dispatchers)
    sessionScope.launch(dispatchers.zipline) {
      val appLifecycle = appService.withScope(ziplineScope).appLifecycle
      val host = RealAppLifecycleHost(codeHost, appLifecycle, eventPublisher, frameClock)
      appLifecycle.start(host)
    }
  }

  override fun cancel() {
    appScope.launch(dispatchers.zipline) {
      sessionScope.cancel()
      ziplineScope.close()
      zipline.close()
    }
  }
}

/** Platform features to the guest application. */
private class RealAppLifecycleHost(
  val codeHost: CodeHost<*>,
  val appLifecycle: AppLifecycle,
  val eventPublisher: EventPublisher,
  val frameClock: FrameClock,
) : AppLifecycle.Host {
  override fun requestFrame() {
    frameClock.requestFrame(appLifecycle)
  }

  override fun onUnknownEvent(
    widgetTag: WidgetTag,
    tag: EventTag,
  ) {
    eventPublisher.onUnknownEvent(widgetTag, tag)
  }

  override fun onUnknownEventNode(
    id: Id,
    tag: EventTag,
  ) {
    eventPublisher.onUnknownEventNode(id, tag)
  }

  override fun handleUncaughtException(exception: Throwable) {
    codeHost.handleUncaughtException(exception)
  }
}
