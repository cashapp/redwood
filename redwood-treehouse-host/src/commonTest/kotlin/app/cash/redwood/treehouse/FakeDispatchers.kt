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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope

class FakeDispatchers(
  override val ui: CoroutineDispatcher,
  override val zipline: CoroutineDispatcher,
) : TreehouseDispatchers {

  var isClosed = false
    private set

  constructor(testScope: TestScope) : this(ui = testScope.dispatcher(), zipline = testScope.dispatcher())

  override fun checkUi() {
  }

  override fun checkZipline() {
  }

  override fun close() {
    isClosed = true
  }
}
