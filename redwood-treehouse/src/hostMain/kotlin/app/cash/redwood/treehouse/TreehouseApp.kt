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

import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.widget.DiffConsumingNode
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.widget.Widget
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineScope
import app.cash.zipline.withScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
  private val scope: CoroutineScope,
  public val spec: Spec<A>,
  public val dispatchers: TreehouseDispatchers,
  private val eventPublisher: EventPublisher,
) {
  /** Only accessed on [TreehouseDispatchers.zipline]. */
  private var closed = false

  /** Only accessed on [TreehouseDispatchers.ui]. */
  private var ziplineSession: ZiplineSession? = null

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

    val sessionScope = CoroutineScope(
      SupervisorJob(scope.coroutineContext.job) + dispatchers.zipline
    )
    sessionScope.launch(dispatchers.zipline) {
      val clockService = appService.frameClockService
      coroutineContext.job.invokeOnCompletion {
        clockService.close()
      }
      val ticksPerSecond = 60
      var now = 0L
      val delayNanos = 1_000_000_000L / ticksPerSecond
      while (true) {
        clockService.sendFrame(now)
        delay(delayNanos / 1_000_000)
        now += delayNanos
      }
    }

    sessionScope.launch(dispatchers.ui) {
      val previous = ziplineSession

      val next = ZiplineSession(
        sessionScope = sessionScope,
        zipline = zipline,
        appService = appService,
        isInitialLaunch = previous == null,
      )

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
  private fun bind(view: TreehouseView<A>, ziplineSession: ZiplineSession?, codeChanged: Boolean) {
    dispatchers.checkUi()

    // Make sure we're tracking this view, so we can update it when the code changes.
    val content = view.boundContent
    val previous = bindings[view]
    if (!codeChanged && previous is TreehouseApp<*>.RealBinding && content == previous.content) {
      return // Nothing has changed.
    }

    val next = when {
      // We have content and code. Launch the treehouse UI.
      content != null && ziplineSession != null -> {
        RealBinding(
          content,
          ziplineSession.isInitialLaunch,
          ziplineSession,
          view,
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
    scope.launch(dispatchers.ui) {
      val session = ziplineSession ?: return@launch
      session.cancel()
      ziplineSession = null
    }
    eventPublisher.appCanceled(this)
  }

  /** The host state for a single code load. We get a new session each time we get new code. */
  private inner class ZiplineSession(
    val sessionScope: CoroutineScope,
    val appService: A,
    val zipline: Zipline,
    val isInitialLaunch: Boolean,
  ) {
    fun cancel() {
      this@TreehouseApp.scope.launch(dispatchers.zipline) {
        sessionScope.cancel()
        zipline.close()
      }
    }
  }

  private interface Binding {
    fun cancel()
  }

  /** A widget awaiting a [ZiplineSession]. */
  private object LoadingBinding : Binding {
    override fun cancel() {
    }
  }

  /**
   * Connects a widget, its current [TreehouseView.boundContent], and the current [ZiplineSession].
   *
   * Canceled if the code changes, the widget's content changes, or the widget is detached from
   * screen.
   *
   * This aggressively manages the lifecycle of the widget, breaking widget reachability when the
   * binding is canceled. It uses a single [ZiplineScope] for all Zipline services consumed by this
   * binding.
   */
  private inner class RealBinding(
    val content: TreehouseView.Content<A>,
    private val isInitialLaunch: Boolean,
    session: ZiplineSession,
    view: TreehouseView<A>,
  ) : Binding, EventSink, DiffSinkService {
    private val bindingScope = CoroutineScope(
      SupervisorJob(scope.coroutineContext.job) + dispatchers.zipline
    )

    /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
    private var viewOrNull: TreehouseView<A>? = view

    /** Only accessed on [TreehouseDispatchers.ui]. Null after [cancel]. */
    @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
    private var bridgeOrNull: ProtocolBridge<*>? = ProtocolBridge(
      container = view.children as Widget.Children<Any>,
      factory = view.widgetSystem.widgetFactory(
        app = this@TreehouseApp,
        json = session.zipline.json,
        protocolMismatchHandler = eventPublisher.protocolMismatchHandler(this@TreehouseApp),
      ) as DiffConsumingNode.Factory<Any>,
      eventSink = this,
    )

    /** Only accessed on [TreehouseDispatchers.zipline]. */
    private val ziplineScope = ZiplineScope()

    /** Only accessed on [TreehouseDispatchers.zipline]. Null after [cancel]. */
    private var treehouseUiOrNull: ZiplineTreehouseUi? = null

    /** Only accessed on [TreehouseDispatchers.ui]. */
    private var firstDiff = true

    /** Send an event from the UI to Zipline. */
    override fun sendEvent(event: Event) {
      // Send UI events on the zipline dispatcher.
      bindingScope.launch(dispatchers.zipline) {
        val treehouseUi = treehouseUiOrNull ?: return@launch
        treehouseUi.sendEvent(event)
      }
    }

    /** Send a diff from Zipline to the UI. */
    override fun sendDiff(diff: Diff) {
      // Receive UI updates on the UI dispatcher.
      bindingScope.launch(dispatchers.ui) {
        val view = viewOrNull ?: return@launch
        val bridge = bridgeOrNull ?: return@launch

        if (firstDiff) {
          firstDiff = false
          view.reset()
          view.codeListener.onCodeLoaded(isInitialLaunch)
        }

        bridge.sendDiff(diff)
      }
    }

    fun start(
      session: ZiplineSession,
      view: TreehouseView<A>,
    ) {
      bindingScope.launch(dispatchers.zipline) {
        val scopedAppService = session.appService.withScope(ziplineScope)
        val treehouseUi = content.get(scopedAppService)
        treehouseUiOrNull = treehouseUi
        treehouseUi.start(
          diffSink = this@RealBinding,
          hostConfigurations = view.hostConfiguration.toFlowWithInitialValue(),
        )
      }
    }

    override fun cancel() {
      viewOrNull = null
      bridgeOrNull = null
      scope.launch(dispatchers.zipline) {
        treehouseUiOrNull = null
        bindingScope.cancel()
        ziplineScope.close()
      }
    }
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
