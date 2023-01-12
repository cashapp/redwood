/*
 * Copyright (C) 2022 Square, Inc.
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * This class binds downloaded code to on-screen views.
 *
 * It updates the binding when the views change via [TreehouseView.OnStateChangeListener], and when
 * new code is available in [onCodeChanged].
 */
public class TreehouseApp<A : AppService> internal constructor(
  private val appScope: CoroutineScope,
  public val spec: Spec<A>,
  public val dispatchers: TreehouseDispatchers,
  private val eventPublisher: EventPublisher,
) {
  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private var closed = false

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var ziplineSession: ZiplineSession<A>? = null

  /**
   * Keys are views currently attached on-screen with non-null contents.
   * Only accessed on [TreehouseDispatchers.ui].
   */
  private val bindings = mutableMapOf<TreehouseView<A>, Binding>()

  /**
   * Returns the current zipline attached to this host, or null if Zipline hasn't loaded yet. The
   * returned value will be invalid when new code is loaded.
   *
   * It is unwise to use this instance for anything beyond measurement and monitoring, because the
   * instance may be replaced if new code is loaded.
   */
  public val zipline: Zipline?
    get() = ziplineSession?.zipline

  private val stateChangeListener = TreehouseView.OnStateChangeListener { view ->
    bind(view, ziplineSession, codeChanged = false)
  }

  public fun renderTo(view: TreehouseView<A>) {
    view.stateChangeListener = stateChangeListener
    stateChangeListener.onStateChanged(view)
  }

  /**
   * Refresh the code. Even if no views are currently showing we refresh the code, so we're ready
   * when a view is added.
   *
   * This function may only be invoked on [TreehouseDispatchers.zipline].
   */
  public fun onCodeChanged(zipline: Zipline, appService: A) {
    dispatchers.checkZipline()
    check(!closed)

    val sessionScope = CoroutineScope(SupervisorJob(appScope.coroutineContext.job))
    sessionScope.launch(dispatchers.ui) {
      val previous = ziplineSession

      val next = ZiplineSession(
        app = this@TreehouseApp,
        appScope = appScope,
        sessionScope = sessionScope,
        zipline = zipline,
        appService = appService,
        isInitialLaunch = previous == null,
      )

      next.startFrameClock()

      val viewsToRebind = bindings.keys.toTypedArray() // Defensive copy 'cause bind() mutates.
      for (treehouseView in viewsToRebind) {
        bind(treehouseView, next, codeChanged = true)
      }

      if (previous != null) {
        sessionScope.launch(dispatchers.zipline) {
          previous.cancel()
        }
      }

      ziplineSession = next
    }
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  private fun bind(
    view: TreehouseView<A>,
    ziplineSession: ZiplineSession<A>?,
    codeChanged: Boolean,
  ) {
    dispatchers.checkUi()

    // Make sure we're tracking this view, so we can update it when the code changes.
    val content = view.boundContent
    val previous = bindings[view]
    if (!codeChanged && previous is RealBinding<*> && content == previous.content) {
      return // Nothing has changed.
    }

    val next = when {
      // We have content and code. Launch the treehouse UI.
      content != null && ziplineSession != null -> {
        RealBinding(
          app = this@TreehouseApp,
          appScope = appScope,
          eventPublisher = eventPublisher,
          content = content,
          session = ziplineSession,
          view = view,
        ).apply {
          start(ziplineSession, view)
        }
      }

      // We have content but no code. Keep track of it for later.
      content != null -> {
        LoadingBinding.also {
          if (previous == null) {
            view.codeListener.onInitialCodeLoading()
          }
        }
      }

      // No content.
      else -> null
    }

    // Replace the previous binding, if any.
    when {
      next != null -> bindings[view] = next
      else -> bindings.remove(view)
    }

    previous?.cancel()
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  public fun cancel() {
    dispatchers.checkZipline()
    closed = true
    appScope.launch(dispatchers.ui) {
      val session = ziplineSession ?: return@launch
      session.cancel()
      ziplineSession = null
    }
    eventPublisher.appCanceled(this)
  }

  /**
   * Configuration and code to launch a Treehouse application.
   */
  public abstract class Spec<A : AppService> {
    public abstract val name: String
    public abstract val manifestUrl: Flow<String>

    public open val serializersModule: SerializersModule
      get() = EmptySerializersModule()

    public open val freshCodePolicy: FreshCodePolicy
      get() = FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY

    public abstract fun bindServices(zipline: Zipline)
    public abstract fun create(zipline: Zipline): A
  }
}
