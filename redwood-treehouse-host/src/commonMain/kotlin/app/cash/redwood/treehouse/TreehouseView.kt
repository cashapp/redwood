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

import app.cash.redwood.protocol.host.ProtocolFactory
import app.cash.redwood.protocol.host.ProtocolMismatchHandler
import app.cash.redwood.widget.RedwoodView
import kotlin.native.ObjCName
import kotlinx.serialization.json.Json

@ObjCName("TreehouseView", exact = true)
public interface TreehouseView<W : Any> : RedwoodView<W> {
  public val widgetSystem: WidgetSystem<W>
  public val dynamicContentWidgetFactory: DynamicContentWidgetFactory<W>
  public val readyForContent: Boolean
  public var readyForContentChangeListener: ReadyForContentChangeListener<W>?
  public var saveCallback: SaveCallback?
  public val stateSnapshotId: StateSnapshot.Id

  @ObjCName("TreehouseViewReadyForContentChangeListener", exact = true)
  public fun interface ReadyForContentChangeListener<W : Any> {
    /** Called when [TreehouseView.readyForContent] has changed. */
    public fun onReadyForContentChanged(view: TreehouseView<W>)
  }

  @ObjCName("TreehouseViewSaveCallback", exact = true)
  public interface SaveCallback {
    /** Called on the UI dispatcher to save the state for the current content. */
    public fun performSave(id: String)
  }

  // TODO Rename this type to something else, along with ProtocolFactory which needs a rewrite
  //  to behave more like the guest-side GuestProtocolAdapter and its companion.
  @ObjCName("TreehouseViewWidgetSystem", exact = true)
  public fun interface WidgetSystem<W : Any> {
    /** Returns a widget factory for encoding and decoding changes to the contents of [view]. */
    public fun widgetFactory(
      json: Json,
      protocolMismatchHandler: ProtocolMismatchHandler,
    ): ProtocolFactory<W>
  }
}
