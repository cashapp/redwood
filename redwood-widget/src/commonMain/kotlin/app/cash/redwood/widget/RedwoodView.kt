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
package app.cash.redwood.widget

import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import kotlin.native.ObjCName
import kotlinx.coroutines.flow.StateFlow

@ObjCName("RedwoodView", exact = true)
public interface RedwoodView<W : Any> {
  public val root: Root<W>
  public val onBackPressedDispatcher: OnBackPressedDispatcher
  public val uiConfiguration: StateFlow<UiConfiguration>
  public val savedStateRegistry: SavedStateRegistry?

  @ObjCName("Root", exact = true)
  public interface Root<W : Any> : Widget<W> {
    /**
     * The current dynamic content. If the application logic is stopped or crashed this will retain
     * a snapshot of the most-recent content, but the content will ignore user actions.
     */
    public val children: Widget.Children<W>

    /**
     * Called for lifecycle changes to the content choreographing this view.
     *
     * Content may be bound to this view before it has a view tree. When that happens this will
     * be called with `loadCount = 0` and `attached = false`. Content won't be ready if it is busy
     * downloading, launching, or computing an initial view tree.
     *
     * When the content's initial view tree is ready, this is called with `loadCount = 1` and
     * `attached = true`. This signals that the content is both running and interactive.
     *
     * If the content stops, this is called again with `attached = false`. This happens when the
     * content is detached, either gracefully or due to a crash. Call the lambda provided to
     * [restart] to relaunch the content.
     *
     * Each time the content is replaced dynamically (a ‘hot reload’), this is called with an
     * incremented [loadCount].
     *
     * @param loadCount how many different versions of content have been loaded for this view. This
     *     is 1 for the first load, and increments for subsequent reloads. Use this to trigger an
     *     'reloaded' message or animation when business logic is updated. This may skip values if
     *     content stops before it emits its initial view tree.
     * @param attached true if the content is interactive. This is false if it has stopped or
     *     crashed.
     * @param uncaughtException the exception that caused the content to detach.
     */
    public fun contentState(
      loadCount: Int,
      attached: Boolean,
      uncaughtException: Throwable? = null,
    )

    /**
     * Call the provided lambda to restart the business logic that powers this UI.
     */
    public fun restart(restart: (() -> Unit)?)
  }
}
