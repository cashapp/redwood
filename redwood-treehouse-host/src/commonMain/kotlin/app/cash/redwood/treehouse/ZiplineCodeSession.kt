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
import app.cash.redwood.treehouse.CodeSession.Listener
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineScope
import app.cash.zipline.withScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ZiplineCodeSession<A : AppService>(
  private val dispatchers: TreehouseDispatchers,
  private val eventPublisher: EventPublisher,
  private val appScope: CoroutineScope,
  private val frameClock: FrameClock,
  override val appService: A,
  val zipline: Zipline,
) : CodeSession<A>, AppLifecycle.Host {
  private val listeners = mutableListOf<Listener<A>>()
  private val ziplineScope = ZiplineScope()

  override val json: Json
    get() = zipline.json

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private lateinit var sessionScope: CoroutineScope

  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private lateinit var appLifecycle: AppLifecycle

  private var canceled = false

  override fun start(sessionScope: CoroutineScope) {
    dispatchers.checkUi()
    sessionScope.launch(dispatchers.zipline) {
      this@ZiplineCodeSession.sessionScope = sessionScope

      frameClock.start(sessionScope, dispatchers)

      val service = appService.withScope(ziplineScope).appLifecycle
      appLifecycle = service
      service.start(this@ZiplineCodeSession)
    }
  }

  override fun addListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners += listener
  }

  override fun removeListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners -= listener
  }

  override fun cancel() {
    if (canceled) return
    canceled = true

    dispatchers.checkUi()

    val listenersArray = listeners.toTypedArray() // onCancel mutates.
    for (listener in listenersArray) {
      listener.onCancel(this)
    }

    appScope.launch(dispatchers.zipline) {
      sessionScope.cancel()
      ziplineScope.close()
      zipline.close()
    }
  }

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
    appScope.launch(dispatchers.ui) {
      val listenersArray = listeners.toTypedArray() // onUncaughtException mutates.
      for (listener in listenersArray) {
        listener.onUncaughtException(this@ZiplineCodeSession, exception)
      }
      this@ZiplineCodeSession.cancel()
    }

    eventPublisher.onUncaughtException(exception)
  }

  override fun newServiceScope(): CodeSession.ServiceScope<A> {
    val ziplineScope = ZiplineScope()

    return object : CodeSession.ServiceScope<A> {
      override fun apply(appService: A): A {
        return appService.withScope(ziplineScope)
      }

      override fun close() {
        ziplineScope.close()
      }
    }
  }
}
