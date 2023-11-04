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

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


/** The host state for a single code load. We get a new session each time we get new code. */
internal abstract class CodeSession<A : AppService>(
  val dispatchers: TreehouseDispatchers,
  val eventPublisher: EventPublisher,
  val appScope: CoroutineScope,
  val appService: A,
) {
  private val listeners = mutableListOf<Listener<A>>()

  private var stopped = false

  /** This scope is canceled when this session stops. */
  val scope: CoroutineScope = run {
    val coroutineExceptionHandler = object : CoroutineExceptionHandler {
      override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler.Key

      override fun handleException(context: CoroutineContext, exception: Throwable) {
        handleUncaughtException(exception)
      }
    }

    return@run CoroutineScope(
      SupervisorJob(appScope.coroutineContext.job) + coroutineExceptionHandler
    )
  }

  abstract val json: Json

  fun start() {
    dispatchers.checkUi()
    scope.launch(dispatchers.zipline) {
      ziplineStart()
    }
  }

  /** Invoked on [TreehouseDispatchers.zipline]. */
  protected abstract fun ziplineStart()

  fun stop() {
    dispatchers.checkUi()

    if (stopped) return
    stopped = true

    val listenersArray = listeners.toTypedArray() // onStop mutates.
    for (listener in listenersArray) {
      listener.onStop(this)
    }

    scope.launch(dispatchers.zipline) {
      ziplineStop()
      scope.cancel()
    }
  }

  /** Invoked on [TreehouseDispatchers.zipline]. */
  protected abstract fun ziplineStop()

  /** Propagates [exception] to all listeners and cancels this session. */
  fun handleUncaughtException(exception: Throwable) {
    scope.launch(dispatchers.ui) {
      val listenersArray = listeners.toTypedArray() // onUncaughtException mutates.
      for (listener in listenersArray) {
        listener.onUncaughtException(this@CodeSession, exception)
      }
      stop()
    }

    eventPublisher.onUncaughtException(exception)
  }

  abstract fun newServiceScope(): ServiceScope<A>

  fun addListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners += listener
  }

  fun removeListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners -= listener
  }

  /**
   * Tracks all of the services created to produce a UI, and offers a single mechanism to close
   * them all. Note that closing this does not close the app services it was applied to.
   */
  interface ServiceScope<A : AppService> {
    /**
     * Returns a new instance that forwards calls to [appService] and keeps track of returned
     * instances so they may be closed.
     */
    fun apply(appService: A): A
    fun close()
  }

  interface Listener<A : AppService> {
    /** Called when a code session crashed with [exception].*/
    fun onUncaughtException(codeSession: CodeSession<A>, exception: Throwable)

    /** Called when a code session will stop.*/
    fun onStop(codeSession: CodeSession<A>)
  }
}
