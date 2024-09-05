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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import platform.Foundation.NSThread

internal class IosTreehouseDispatchersTest : AbstractTreehouseDispatchersTest() {
  /** We haven't set done the work to dispatch to the UI thread on iOS tests. */
  override val ignoreTestsThatExecuteOnUiThread: Boolean
    get() = true

  override fun newTreehouseDispatchers(applicationName: String) =
    IosTreehouseDispatchers(applicationName)

  @Test
  fun closeFinishesZiplineThreadWithoutExecutingSubsequentRunnable() = runBlocking {
    val treehouseDispatchers = newTreehouseDispatchers("appName")
    val ziplineThread = treehouseDispatchers.ziplineThread!!

    val loggedRunnableIds = mutableListOf<Char>()
    fun runnable(id: Char) = Runnable { loggedRunnableIds.add(id) }

    ziplineThread.awaitIsExecuting(isExecuting = true)
    treehouseDispatchers.zipline.dispatch(coroutineContext, runnable('a'))
    treehouseDispatchers.zipline.dispatch(coroutineContext, runnable('b'))
    treehouseDispatchers.close()
    treehouseDispatchers.zipline.dispatch(coroutineContext, runnable('c'))
    ziplineThread.awaitIsExecuting(isExecuting = false)

    assertTrue(ziplineThread.isFinished())
    assertEquals(listOf('a', 'b'), loggedRunnableIds)
  }

  private suspend fun NSThread.awaitIsExecuting(isExecuting: Boolean) {
    withTimeout(1.seconds) {
      while (isExecuting() != isExecuting) {
        delay(5.milliseconds)
      }
    }
  }
}
