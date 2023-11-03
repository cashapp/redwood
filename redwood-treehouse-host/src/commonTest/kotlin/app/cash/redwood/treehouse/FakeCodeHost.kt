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

import app.cash.redwood.treehouse.CodeHost.Listener
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope

internal class FakeCodeHost(
  private val eventLog: EventLog,
  private val eventPublisher: EventPublisher,
) : CodeHost<FakeAppService> {
  override val stateStore = MemoryStateStore()

  private val codeSessionListener = object : CodeSession.Listener<FakeAppService> {
    override fun onUncaughtException(
      codeSession: CodeSession<FakeAppService>,
      exception: Throwable,
    ) {
    }

    override fun onCancel(
      codeSession: CodeSession<FakeAppService>,
    ) {
      check(codeSession == this@FakeCodeHost.session)
      this@FakeCodeHost.session = null
    }
  }

  override var session: CodeSession<FakeAppService>? = null
    set(value) {
      val previous = field
      previous?.removeListener(codeSessionListener)
      previous?.cancel()

      if (value != null) {
        value.start(CoroutineScope(EmptyCoroutineContext), FakeFrameClock())
        for (listener in listeners) {
          listener.codeSessionChanged(value)
        }
      }

      value?.addListener(codeSessionListener)
      field = value
    }

  private val listeners = mutableListOf<Listener<FakeAppService>>()

  fun startCodeSession(name: String): CodeSession<FakeAppService> {
    val result = FakeCodeSession(eventLog, name, eventPublisher)
    session = result
    return result
  }

  override fun addListener(listener: Listener<FakeAppService>) {
    listeners += listener
  }

  override fun removeListener(listener: Listener<FakeAppService>) {
    listeners -= listener
  }
}
