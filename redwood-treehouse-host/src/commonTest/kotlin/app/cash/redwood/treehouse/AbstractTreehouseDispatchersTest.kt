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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

abstract class AbstractTreehouseDispatchersTest {
  open val ignoreTestsThatExecuteOnUiThread: Boolean get() = false

  abstract fun newTreehouseDispatchers(
    applicationName: String,
  ): TreehouseDispatchers

  @Test
  fun uncaughtExceptionsOnUiDispatcherCancelsJob() = runTest {
    if (ignoreTestsThatExecuteOnUiThread) return@runTest
    val treehouseDispatchers = newTreehouseDispatchers("appName")
    uncaughtExceptionsCancelTheirJob(treehouseDispatchers.ui)
    treehouseDispatchers.close()
  }

  @Test
  fun uncaughtExceptionsOnZiplineDispatcherCancelsJob() = runTest {
    val treehouseDispatchers = newTreehouseDispatchers("appName")
    uncaughtExceptionsCancelTheirJob(treehouseDispatchers.zipline)
    treehouseDispatchers.close()
  }

  private suspend fun uncaughtExceptionsCancelTheirJob(dispatcher: CoroutineContext) {
    val exceptionCollector = ExceptionCollector()

    val failingJob = supervisorScope {
      launch(exceptionCollector + dispatcher) {
        throw Exception("boom!")
      }
    }

    failingJob.join()
    assertThat(failingJob.isCancelled).isTrue()
    assertThat(exceptionCollector.exceptions.removeFirst().message).isEqualTo("boom!")
  }

  @Test
  fun checkThreadFromUiDispatcher() = runTest {
    if (ignoreTestsThatExecuteOnUiThread) return@runTest
    val treehouseDispatchers = newTreehouseDispatchers("appName")
    withContext(treehouseDispatchers.ui) {
      treehouseDispatchers.checkUi()
      assertFailsWith<IllegalStateException> {
        treehouseDispatchers.checkZipline()
      }
    }
    treehouseDispatchers.close()
  }

  @Test
  fun checkThreadFromZiplineDispatcher() = runTest {
    val treehouseDispatchers = newTreehouseDispatchers("appName")
    withContext(treehouseDispatchers.zipline) {
      treehouseDispatchers.checkZipline()
      assertFailsWith<IllegalStateException> {
        treehouseDispatchers.checkUi()
      }
    }
    treehouseDispatchers.close()
  }

  @Test
  fun multipleTreehouseDispatchersHaveIndependentZiplineThreads() = runTest {
    val dispatchersA = newTreehouseDispatchers("app A")
    val dispatchersB = newTreehouseDispatchers("app B")
    withContext(dispatchersA.zipline) {
      dispatchersA.checkZipline()
      assertFailsWith<IllegalStateException> {
        dispatchersB.checkZipline()
      }
    }
    withContext(dispatchersB.zipline) {
      dispatchersB.checkZipline()
      assertFailsWith<IllegalStateException> {
        dispatchersA.checkZipline()
      }
    }
    dispatchersA.close()
    dispatchersB.close()
  }

  @Test
  fun multipleTreehouseDispatchersHaveSharedMainThread() = runTest {
    if (ignoreTestsThatExecuteOnUiThread) return@runTest
    val dispatchersA = newTreehouseDispatchers("app A")
    val dispatchersB = newTreehouseDispatchers("app B")
    withContext(dispatchersA.ui) {
      dispatchersA.checkUi()
      dispatchersB.checkUi()
    }
    withContext(dispatchersB.ui) {
      dispatchersA.checkUi()
      dispatchersB.checkUi()
    }
    dispatchersA.close()
    dispatchersB.close()
  }

  private class ExceptionCollector :
    AbstractCoroutineContextElement(CoroutineExceptionHandler),
    CoroutineExceptionHandler {
    val exceptions = ArrayDeque<Throwable>()

    override fun handleException(context: CoroutineContext, exception: Throwable) {
      exceptions += exception
    }
  }
}
