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

import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.protocol.widget.DiffConsumingWidget.Factory
import app.cash.redwood.protocol.widget.ProtocolDisplay
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * This class binds downloaded code to on-screen views.
 *
 * It updates the binding when the views change in [onContentChanged], and when new code is
 * available in [onCodeChanged].
 */
public class TreehouseApp<T : Any>(
  private val scope: CoroutineScope,
  public val dispatchers: TreehouseDispatchers,
  public val viewBinder: ViewBinder,
) {
  /** All state is confined to [TreehouseDispatchers.zipline]. */
  private var closed = false
  private var ziplineSession: ZiplineSession<T>? = null
  private val viewToBoundContent = mutableMapOf<TreehouseView<T>, TreehouseView.Content<T>>()

  /**
   * Returns the current zipline attached to this host, or null if Zipline hasn't loaded yet. The
   * returned value will be invalid when new code is loaded.
   *
   * It is unwise to use this instance for anything beyond measurement and monitoring, because the
   * instance may be replaced if new code is loaded.
   */
  public val zipline: Zipline?
    get() = ziplineSession?.zipline

  /** This function may only be invoked on [TreehouseDispatchers.main]. */
  public fun onContentChanged(view: TreehouseView<T>) {
    dispatchers.checkMain()
    scope.launch(dispatchers.zipline) {
      bind(view)
    }
  }

  /**
   * Refresh the code. Even if no views are currently showing we refresh the code so we're ready
   * when a view is added.
   */
  public fun onCodeChanged(zipline: Zipline, context: T) {
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
  private suspend fun bind(view: TreehouseView<T>) {
    dispatchers.checkZipline()

    // Make sure we're tracking this view so we can update it when the code changes.
    val content = view.boundContent
    if (content == viewToBoundContent[view]) {
      return // Nothing has changed.
    } else if (content != null) {
      viewToBoundContent[view] = content
    } else {
      viewToBoundContent.remove(view)
    }

    val ziplineSession = this.ziplineSession
    if (ziplineSession != null) {
      ziplineSession.bind(view, content)
    } else {
      // If we're waiting for code to load, show a loading indicator until it's ready.
      withContext(dispatchers.main) {
        viewBinder.codeLoading(view)
      }
    }
  }

  /** This function may only be invoked on [TreehouseDispatchers.zipline]. */
  public fun cancel() {
    dispatchers.checkZipline()
    closed = true
    ziplineSession?.cancel()
  }

  /** The host state for a single code load. We get a new session each time we get new code. */
  private inner class ZiplineSession<T : Any>(
    val scope: CoroutineScope,
    val context: T,
    val zipline: Zipline,
    val isInitialLaunch: Boolean,
  ) {
    /** Map of views to the zipline service whose content drives those views. */
    val bindings = mutableMapOf<TreehouseView<T>, ZiplineService>()

    fun bind(
      view: TreehouseView<T>,
      content: TreehouseView.Content<T>?,
    ) {
      dispatchers.checkZipline()

      val previous = bindings.remove(view)
      previous?.close()

      if (content == null) return

      val ziplineTreehouseUi = content.get(context)
      bindSinks(ziplineTreehouseUi, view, isInitialLaunch)

      bindings[view] = ziplineTreehouseUi
    }

    private fun bindSinks(
      content: ZiplineTreehouseUi,
      view: TreehouseView<T>,
      isInitialLaunch: Boolean,
    ) {
      val eventSink = EventSink { event ->
        // Send UI events on the zipline dispatcher.
        scope.launch(dispatchers.zipline) {
          content.sendEvent(event)
        }
      }

      val widgetFactory = viewBinder.widgetFactory(view, zipline.json)

      @Suppress("UNCHECKED_CAST") // We don't have a type parameter for the widget type.
      val display = ProtocolDisplay(
        root = view.protocolDisplayRoot as DiffConsumingWidget<Any>,
        factory = widgetFactory as Factory<Any>,
        eventSink = eventSink,
      )

      val diffSinkService = object : DiffSinkService {
        private var firstDiff = true

        override fun sendDiff(diff: Diff) {
          // Receive UI updates on the main dispatcher.
          scope.launch(dispatchers.main) {
            if (firstDiff) {
              firstDiff = false
              view.protocolDisplayRoot.children(ChildrenDiff.RootChildrenTag)!!.clear()

              when {
                isInitialLaunch -> viewBinder.beforeInitialCode(view)
                else -> viewBinder.beforeUpdatedCode(view)
              }
            }

            display.sendDiff(diff)
          }
        }
      }

      content.start(diffSinkService)
    }

    fun cancel() {
      scope.cancel()
      zipline.close()
    }
  }

  /**
   * Configuration and code to launch a Treehouse application.
   */
  public abstract class Spec<T : Any> {
    public abstract val name: String
    public abstract val manifestUrl: Flow<String>

    public open val serializersModule: SerializersModule
      get() = EmptySerializersModule()

    public open val freshCodePolicy: FreshCodePolicy
      get() = FreshCodePolicy.ALWAYS_REFRESH_IMMEDIATELY

    public abstract val viewBinder: ViewBinder

    public abstract fun bindServices(zipline: Zipline)
    public abstract fun create(zipline: Zipline): T
  }
}
