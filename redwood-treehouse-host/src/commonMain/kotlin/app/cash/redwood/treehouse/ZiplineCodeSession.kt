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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal class ZiplineCodeSession<A : AppService>(
  private val dispatchers: TreehouseDispatchers,
  override val eventPublisher: EventPublisher,
  frameClockFactory: FrameClock.Factory,
  override val appService: A,
  val zipline: Zipline,
  val appScope: CoroutineScope,
) : CodeSession<A>, AppLifecycle.Host {
  private val listeners = mutableListOf<Listener<A>>()
  private val ziplineScope = ZiplineScope()

  override val scope = CoroutineScope(
    SupervisorJob(appScope.coroutineContext.job) + coroutineExceptionHandler,
  )

  override val json: Json
    get() = zipline.json

  private val frameClock = frameClockFactory.create(scope, dispatchers)
  private lateinit var appLifecycle: AppLifecycle

  private var canceled = false

  override fun start() {
    dispatchers.checkUi()

    scope.launch(dispatchers.zipline) {
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
    dispatchers.checkUi()

    if (canceled) return
    canceled = true

    val listenersArray = listeners.toTypedArray() // onCancel mutates.
    for (listener in listenersArray) {
      listener.onCancel(this)
    }

    scope.launch(dispatchers.zipline) {
      ziplineScope.close()
      zipline.close()
      scope.cancel()
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
    scope.launch(dispatchers.ui) {
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
