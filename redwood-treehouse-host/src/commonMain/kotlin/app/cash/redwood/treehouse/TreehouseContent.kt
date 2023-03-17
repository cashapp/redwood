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

internal class TreehouseContent<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val source: TreehouseContentSource<A>,
) : Content<A> {
  private val dispatchers = treehouseApp.dispatchers

  private var view: TreehouseView<A>? = null
  private var binding: Binding? = null

  override fun bind(view: TreehouseView<A>) {
    treehouseApp.dispatchers.checkUi()

    // Binding the bound view does nothing. This is necessary so that listeners don't need to
    // independently track the bound/unbound state.
    if (this.view == view) return

    require(this.view == null)
    this.view = view

    treehouseApp.boundContents += this
    receiveZiplineSession(treehouseApp.ziplineSession, false)
  }

  override fun unbind() {
    treehouseApp.dispatchers.checkUi()

    if (view == null) return // unbind() is idempotent.

    treehouseApp.boundContents.remove(this)
    binding?.cancel()
    view = null
    binding = null
  }

  /** This function may only be invoked on [TreehouseDispatchers.ui]. */
  internal fun receiveZiplineSession(
    ziplineSession: ZiplineSession<A>?,
    codeChanged: Boolean,
  ) {
    dispatchers.checkUi()

    // Make sure we're tracking this view, so we can update it when the code changes.
    val view = this.view!!
    val previous = binding
    if (!codeChanged && previous is RealBinding<*>) {
      return // Nothing has changed.
    }

    val next = when {
      // We have content and code. Launch the treehouse UI.
      ziplineSession != null -> {
        RealBinding(
          app = treehouseApp,
          appScope = treehouseApp.appScope,
          eventPublisher = treehouseApp.eventPublisher,
          contentSource = source,
          session = ziplineSession,
          view = view,
        ).apply {
          start(ziplineSession, view)
        }
      }

      // We have content but no code. Keep track of it for later.
      else -> {
        LoadingBinding.also {
          if (previous == null) {
            view.codeListener.onInitialCodeLoading()
          }
        }
      }
    }

    // Replace the previous binding, if any.
    binding = next

    previous?.cancel()
  }
}
