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

import app.cash.redwood.treehouse.CodeSession.Listener
import app.cash.redwood.treehouse.CodeSession.ServiceScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

internal class FakeCodeSession(
  private val eventLog: EventLog,
  private val name: String,
) : CodeSession<FakeAppService> {
  private val listeners = mutableListOf<Listener<FakeAppService>>()

  override val json = Json

  override val appService = FakeAppService("$name.app", eventLog)

  private var canceled = false

  override fun start(sessionScope: CoroutineScope) {
    eventLog += "$name.start()"
  }

  override fun addListener(listener: Listener<FakeAppService>) {
    listeners += listener
  }

  override fun removeListener(listener: Listener<FakeAppService>) {
    listeners -= listener
  }

  override fun handleUncaughtException(exception: Throwable) {
    val listenersArray = listeners.toTypedArray() // onUncaughtException mutates.
    for (listener in listenersArray) {
      listener.onUncaughtException(this, exception)
    }
    cancel()
  }

  override fun newServiceScope(): ServiceScope<FakeAppService> {
    return object : ServiceScope<FakeAppService> {
      val uisToClose = mutableListOf<ZiplineTreehouseUi>()

      override fun apply(appService: FakeAppService): FakeAppService {
        return appService.withListener(object : FakeAppService.Listener {
          override fun onNewUi(ui: ZiplineTreehouseUi) {
            uisToClose += ui
          }
        })
      }

      override fun close() {
        for (ui in uisToClose) {
          ui.close()
        }
      }
    }
  }

  override fun cancel() {
    if (canceled) return
    canceled = true

    val listenersArray = listeners.toTypedArray() // onCancel mutates.
    for (listener in listenersArray) {
      listener.onCancel(this)
    }

    eventLog += "$name.cancel()"
  }
}
