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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * This class binds downloaded code to on-screen views.
 *
 * It updates the binding when the views change in [onContentChanged], and when new code is
 * available in [onCodeChanged].
 */
public class TreehouseApp<A : Any> internal constructor(
  private val scope: CoroutineScope,
  public val spec: Spec<A>,
  public val dispatchers: TreehouseDispatchers,
  private val eventPublisher: EventPublisher,
) {
  /** All state is confined to [TreehouseDispatchers.zipline]. */
  private var closed = false
  private var ziplineSession: ZiplineSession? = null
  private val viewToBoundContent = mutableMapOf<TreehouseView<A>, TreehouseView.Content<A>>()

  /**
   * Returns the current zipline attached to this host, or null if Zipline hasn't loaded yet. The
   * returned value will be invalid when new code is loaded.
   *
   * It is unwise to use this instance for anything beyond measurement and monitoring, because the
   * instance may be replaced if new code is loaded.
   */
  public val zipline: Zipline?
    get() = ziplineSession?.zipline

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  public fun onContentChanged(view: TreehouseView<A>) {
    dispatchers.checkUi()
    scope.launch(dispatchers.zipline) {
      bind(view)
    }
  }

  /**
   * Refresh the code. Even if no views are currently showing we refresh the code, so we're ready
   * when a view is added.
   */
  public fun onCodeChanged(zipline: Zipline, context: A) {
    dispatchers.checkZipline()
    check(!closed)

    // This job lets us cancel the Android Treehouse job without canceling its sibling jobs.
    val supervisorJob = SupervisorJob(scope.coroutineContext.job)
    val cancelableScope = CoroutineScope(supervisorJob + dispatchers.zipline)
    cancelableScope.launch {
      val previous = ziplineSession

      val next = ZiplineSession(
        scope = cancelableScope,
        zipline = zipline,
        context = context,
        isInitialLaunch = previous == null,
      )

      previous?.cancel()

      ziplineSession = next
      for ((treehouseView, boundContent) in viewToBoundContent) {
        next.bind(treehouseView, boundContent)
      }
    }
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  private fun bind(view: TreehouseView<A>) {
    dispatchers.checkZipline()

    // Make sure we're tracking this view, so we can update it when the code changes.
    val content = view.boundContent
    if (content == viewToBoundContent[view]) {
      return // Nothing has changed.
    } else if (content != null) {
      viewToBoundContent[view] = content
    } else {
      viewToBoundContent.remove(view)
    }

    val ziplineSession = ziplineSession
    if (ziplineSession != null) {
      ziplineSession.bind(view, content)
    } else {
      scope.launch(dispatchers.ui) {
        view.codeListener.onInitialCodeLoading()
      }
    }
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  public fun cancel() {
    dispatchers.checkZipline()
    closed = true
    ziplineSession?.cancel()
    eventPublisher.appCanceled(this)
  }

  /** The host state for a single code load. We get a new session each time we get new code. */
  private inner class ZiplineSession(
    val scope: CoroutineScope,
    val context: A,
    val zipline: Zipline,
    val isInitialLaunch: Boolean,
  ) {
    /** Map of views to the zipline service whose content drives those views. */
    val bindings = mutableMapOf<TreehouseView<A>, WidgetBinding>()

    fun bind(
      view: TreehouseView<A>,
      content: TreehouseView.Content<A>?,
    ) {
      dispatchers.checkZipline()

      val previous = bindings.remove(view)
      previous?.cancel()

      if (content == null) return

      val ziplineTreehouseUi = content.get(context)
      val widgetBinding = WidgetBinding(ziplineTreehouseUi, isInitialLaunch, this, view)
      widgetBinding.start(view)

      bindings[view] = widgetBinding
    }

    fun cancel() {
      scope.cancel()
      zipline.close()
    }
  }

  /**
   * Binds a widget to a single code load.
   *
   * This aggressively manages the lifecycle of the widget, breaking widget reachability when the
   * binding is canceled.
   */
  private inner class WidgetBinding(
    private val content: ZiplineTreehouseUi,
    private val isInitialLaunch: Boolean,
    session: ZiplineSession,
    view: TreehouseView<A>,
  ) : EventSink, DiffSinkService {
    private var firstDiff = true

    /** Only accessed on the UI dispatcher. Null after [cancel]. */
    private var viewOrNull: TreehouseView<A>? = view

    /** Only accessed on the UI dispatcher. Null after [cancel]. */
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

    /** Send an event from the UI to Zipline. */
    override fun sendEvent(event: Event) {
      dispatchers.checkUi()
      if (viewOrNull == null) return

      // Send UI events on the zipline dispatcher.
      scope.launch(dispatchers.zipline) {
        content.sendEvent(event)
      }
    }

    /** Send a diff from Zipline to the UI. */
    override fun sendDiff(diff: Diff) {
      // Receive UI updates on the UI dispatcher.
      scope.launch(dispatchers.ui) {
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

    fun start(view: TreehouseView<A>) {
      content.start(
        diffSink = this,
        hostConfigurations = view.hostConfiguration.toFlowWithInitialValue(),
      )
    }

    fun cancel() {
      dispatchers.checkUi()

      content.close()
      viewOrNull = null
      bridgeOrNull = null
    }
  }

  /**
   * Configuration and code to launch a Treehouse application.
   */
  public abstract class Spec<A : Any> {
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
