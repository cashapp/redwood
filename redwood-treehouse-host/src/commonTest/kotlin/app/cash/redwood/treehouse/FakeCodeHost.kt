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

internal class FakeCodeHost : CodeHost<FakeAppService> {
  override val stateStore = MemoryStateStore()

  override var session: CodeSession<FakeAppService>? = null
    set(value) {
      val previous = field
      previous?.cancel()

      if (value != null) {
        value.start()
        for (listener in listeners) {
          listener.codeSessionChanged(value)
        }
      }

      field = value
    }

  private val listeners = mutableListOf<CodeHost.Listener<FakeAppService>>()

  override fun newServiceScope(): CodeHost.ServiceScope<FakeAppService> {
    return object : CodeHost.ServiceScope<FakeAppService> {
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

  fun triggerException(exception: Throwable) {
    for (listener in listeners) {
      listener.uncaughtException(exception)
    }
    session = null
  }

  override fun addListener(listener: CodeHost.Listener<FakeAppService>) {
    listeners += listener
  }

  override fun removeListener(listener: CodeHost.Listener<FakeAppService>) {
    listeners -= listener
  }
}
