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
import kotlinx.serialization.json.Json

/** The host state for a single code load. We get a new session each time we get new code. */
internal interface CodeSession<A : AppService> {
  val eventPublisher: EventPublisher

  val appService: A

  val json: Json

  fun start(sessionScope: CoroutineScope, frameClock: FrameClock)

  fun addListener(listener: Listener<A>)

  fun removeListener(listener: Listener<A>)

  fun newServiceScope(): ServiceScope<A>

  /** Propagates [exception] to all listeners and cancels this session. */
  fun handleUncaughtException(exception: Throwable)

  fun cancel()

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
    fun onUncaughtException(codeSession: CodeSession<A>, exception: Throwable)
    fun onCancel(codeSession: CodeSession<A>)
  }
}

internal val CodeSession<*>.coroutineExceptionHandler: CoroutineExceptionHandler
  get() = object : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
      get() = CoroutineExceptionHandler.Key

    override fun handleException(context: CoroutineContext, exception: Throwable) {
      handleUncaughtException(exception)
    }
  }
