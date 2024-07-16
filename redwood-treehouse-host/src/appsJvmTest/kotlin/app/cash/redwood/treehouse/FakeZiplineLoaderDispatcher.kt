/*
 * Copyright (C) 2024 Square, Inc.
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

import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class) // CloseableCoroutineDispatcher is experimental.
internal class FakeZiplineLoaderDispatcher(
  testScope: TestScope,
) : CloseableCoroutineDispatcher() {
  private val delegate = testScope.dispatcher()

  var closed = false
    private set

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    delegate.dispatch(context, block)
  }

  override val executor: Executor
    get() = error("unexpected call")

  override fun close() {
    closed = true
  }
}
