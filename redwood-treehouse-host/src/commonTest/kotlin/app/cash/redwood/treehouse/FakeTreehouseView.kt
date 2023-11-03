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

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.SavedStateRegistry
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeTreehouseView(
  override val onBackPressedDispatcher: FakeOnBackPressedDispatcher,
  private val name: String,
) : TreehouseView<FakeWidget> {
  override val widgetSystem = FakeWidgetSystem()

  override var readyForContentChangeListener: ReadyForContentChangeListener<FakeWidget>? = null

  override var readyForContent = false
    set(value) {
      field = value
      readyForContentChangeListener?.onReadyForContentChanged(this)
    }

  override var saveCallback: TreehouseView.SaveCallback? = null

  override val stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override val children = MutableListChildren<FakeWidget>()

  override val uiConfiguration = MutableStateFlow(UiConfiguration())

  override val savedStateRegistry: SavedStateRegistry? = null

  override fun reset() {
    children.clear()
  }

  override fun toString() = name
}
