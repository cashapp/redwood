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

import app.cash.zipline.Zipline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * Manages loading and hot-reloading a series of code sessions.
 *
 * The code host has 4 states:
 *
 *  * `Idle`
 *  * `Starting`: collect code updates, and wait for an `Zipline` to load.
 *  * `Running`: collect code updates, and a `Zipline` is running.
 *  * `Crashed`: collect code updates, but the most recent `Zipline` failed.
 *
 * Transitions between states always occur on the UI dispatcher. These functions initiate state
 * transitions:
 *
 *  * `start()` - transition to `Starting` unless it’s `Starting` or `Running`.
 *  * `stop()` - transition to `Idle` immediately
 *  * `restart()` - transition to `Starting` unless it’s already `Starting`.
 *
 * Other state transitions also occur:
 *
 *  * From `Starting` to `Running` when a `Zipline` finishes loading.
 *  * From `Running` to `Crashed` when a `Zipline` fails.
 *  * From `Running` to `Running` when the `Zipline` is replaced by a hot-reload.
 */
internal abstract class CodeHost<A : AppService>(
  private val dispatchers: TreehouseDispatchers,
  private val appScope: CoroutineScope,
  private val frameClockFactory: FrameClock.Factory,
  val stateStore: StateStore,
) {
  /** Contents that this app is currently responsible for. */
  private val listeners = mutableListOf<Listener<A>>()

  private var state: State<A> = State.Idle()

  /** Updated with [State]. */
  private val mutableZipline = MutableStateFlow<Zipline?>(null)

  val zipline: StateFlow<Zipline?>
    get() = mutableZipline

  private val codeSessionListener = object : CodeSession.Listener<A> {
    override fun onUncaughtException(codeSession: CodeSession<A>, exception: Throwable) {
    }

    override fun onStop(codeSession: CodeSession<A>) {
      dispatchers.checkUi()

      codeSession.removeListener(this)

      // If a code session stops while we're still listening to it, it must have crashed.
      val previous = state
      if (previous is State.Running) {
        state = State.Crashed(previous.codeUpdatesScope)
        mutableZipline.value = null
      }
    }
  }

  val codeSession: CodeSession<A>?
    get() = state.codeSession

  /** Returns a flow that emits a new [CodeSession] each time we should load fresh code. */
  abstract fun codeUpdatesFlow(
    eventListenerFactory: EventListener.Factory,
  ): Flow<CodeSession<A>>

  fun start(eventListenerFactory: EventListener.Factory) {
    dispatchers.checkUi()

    val previous = state

    if (previous is State.Starting || previous is State.Running) return // Nothing to do.

    // Force a restart if we're crashed.
    previous.codeUpdatesScope?.cancel()

    val codeUpdatesScope = newCodeUpdatesScope()
    state = State.Starting(codeUpdatesScope)
    codeUpdatesScope.collectCodeUpdates(eventListenerFactory)
  }

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  fun stop() {
    dispatchers.checkUi()

    val previous = state
    previous.codeUpdatesScope?.cancel()
    previous.codeSession?.removeListener(codeSessionListener)
    previous.codeSession?.stop()

    state = State.Idle()
    mutableZipline.value = null
  }

  fun restart(eventListenerFactory: EventListener.Factory) {
    dispatchers.checkUi()

    val previous = state
    if (previous is State.Starting) return // Nothing to restart.

    previous.codeUpdatesScope?.cancel()
    previous.codeSession?.removeListener(codeSessionListener)
    previous.codeSession?.stop()

    val codeUpdatesScope = newCodeUpdatesScope()
    state = State.Starting(codeUpdatesScope)
    mutableZipline.value = null
    codeUpdatesScope.collectCodeUpdates(eventListenerFactory)
  }

  fun addListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners += listener
  }

  fun removeListener(listener: Listener<A>) {
    dispatchers.checkUi()
    listeners -= listener
  }

  private fun newCodeUpdatesScope() =
    CoroutineScope(SupervisorJob(appScope.coroutineContext.job))

  private fun CoroutineScope.collectCodeUpdates(eventListenerFactory: EventListener.Factory) {
    launch(dispatchers.zipline) {
      codeUpdatesFlow(eventListenerFactory).collect {
        codeSessionLoaded(it)
      }
    }
  }

  private fun codeSessionLoaded(next: CodeSession<A>) {
    dispatchers.checkZipline()

    next.scope.launch(dispatchers.ui) {
      // Clean up the previous session.
      val previous = state
      previous.codeSession?.removeListener(codeSessionListener)
      previous.codeSession?.stop()

      // If the codeUpdatesScope is null, we're stopped. Discard the newly-loaded code.
      val codeUpdatesScope = previous.codeUpdatesScope
      if (codeUpdatesScope == null) {
        next.stop()
        return@launch
      }

      // Boot up the new code.
      state = State.Running(codeUpdatesScope, next)
      mutableZipline.value = (next as? ZiplineCodeSession)?.zipline
      next.addListener(codeSessionListener)
      next.start()

      for (listener in listeners) {
        listener.codeSessionChanged(next)
      }
    }
  }

  private sealed class State<A : AppService> {
    /** Non-null if we're prepared for code updates and restarts. */
    open val codeUpdatesScope: CoroutineScope?
      get() = null

    /** Non-null if we're running code. */
    open val codeSession: CodeSession<A>?
      get() = null

    class Idle<A : AppService> : State<A>()

    class Running<A : AppService>(
      override val codeUpdatesScope: CoroutineScope,
      override val codeSession: CodeSession<A>,
    ) : State<A>()

    class Starting<A : AppService>(
      override val codeUpdatesScope: CoroutineScope,
    ) : State<A>()

    class Crashed<A : AppService>(
      override val codeUpdatesScope: CoroutineScope,
    ) : State<A>()
  }

  interface Listener<A : AppService> {
    fun codeSessionChanged(next: CodeSession<A>)
  }
}
