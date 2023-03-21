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

import app.cash.redwood.protocol.widget.DiffConsumingNode
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

public interface TreehouseView {
  public val children: Widget.Children<*>
  public val hostConfiguration: StateFlow<HostConfiguration>
  public val widgetSystem: WidgetSystem
  public val readyForContent: Boolean
  public var readyForContentChangeListener: ReadyForContentChangeListener?

  /** Invoked when new code is loaded. This should at minimum clear all [children]. */
  public fun reset()

  public fun interface ReadyForContentChangeListener {
    /** Called when [TreehouseView.readyForContent] has changed. */
    public fun onReadyForContentChanged(view: TreehouseView)
  }

  public fun interface WidgetSystem {
    /** Returns a widget factory for encoding and decoding changes to the contents of [view]. */
    public fun widgetFactory(
      json: Json,
      protocolMismatchHandler: ProtocolMismatchHandler,
    ): DiffConsumingNode.Factory<*>
  }
}
