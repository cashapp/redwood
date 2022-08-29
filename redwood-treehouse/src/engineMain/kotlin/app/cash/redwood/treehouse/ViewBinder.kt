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

import app.cash.redwood.protocol.widget.DiffConsumingWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

/**
 * Typically this is always [RealViewBinder]; in tests we have fake implementations that don't
 * require all the compose machinery.
 */
public interface ViewBinder {
  /** Show a spinner when a view is waiting for the code to load. */
  public fun codeLoading(view: TreehouseView<*>)

  /**
   * Binds [content] to [view].
   *
   * @param isInitialCode true if this is the first code loaded into [view]. This is false for
   *     development-mode code updates, and potentially production code reloading.
   */
  public fun bind(
    scope: CoroutineScope,
    content: ZiplineTreehouseUi,
    view: TreehouseView<*>,
    json: Json,
    isInitialCode: Boolean,
  ): ViewBinding

  public interface Adapter {
    /** Show a spinner when a view is waiting for the code to load. */
    public fun codeLoading(view: TreehouseView<*>) {}

    /** Clear the loading indicator when the first code is loaded. */
    public fun beforeInitialCode(view: TreehouseView<*>) {}

    /** Clear the previous UI and show a quick animation for subsequent code updates. */
    public fun beforeUpdatedCode(view: TreehouseView<*>) {}

    /** Returns a widget factory for encoding and decoding changes to the contents of [view]. */
    public fun widgetFactory(
      view: TreehouseView<*>,
      json: Json,
    ): DiffConsumingWidget.Factory<*>
  }
}
