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

import app.cash.zipline.ZiplineScope

internal class FakeCodeHost<A : AppService> : CodeHost<A> {
  override val stateStore = MemoryStateStore()

  override var session: CodeSession<A>? = null
    set(value) {
      require(value != null)

      val previous = field
      previous?.cancel()

      value.start()
      for (listener in listeners) {
        listener.codeSessionChanged(value)
      }
      field = value
    }

  private val listeners = mutableListOf<CodeHost.Listener<A>>()

  override fun applyZiplineScope(appService: A, ziplineScope: ZiplineScope) = appService

  override fun addListener(listener: CodeHost.Listener<A>) {
    listeners += listener
  }

  override fun removeListener(listener: CodeHost.Listener<A>) {
    listeners -= listener
  }
}
