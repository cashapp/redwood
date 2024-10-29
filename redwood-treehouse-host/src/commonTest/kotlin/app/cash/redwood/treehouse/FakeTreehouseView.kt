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

import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.SavedStateRegistry
import com.example.redwood.testapp.protocol.host.TestSchemaProtocolFactory
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * An in-memory fake.
 */
internal class FakeTreehouseView(
  private val name: String,
  override val onBackPressedDispatcher: OnBackPressedDispatcher,
  override val uiConfiguration: StateFlow<UiConfiguration> = MutableStateFlow(UiConfiguration()),
) : TreehouseView<WidgetValue> {
  override val children = MutableListChildren<WidgetValue>()

  val views: List<WidgetValue>
    get() = children.map { it.value }

  override val value: WidgetValue
    get() = error("unexpected call")

  override val widgetSystem = TreehouseView.WidgetSystem { json, protocolMismatchHandler ->
    TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      mismatchHandler = protocolMismatchHandler,
    )
  }

  override val dynamicContentWidgetFactory = FakeDynamicContentWidgetFactory()

  override var readyForContentChangeListener: ReadyForContentChangeListener<WidgetValue>? = null

  override var readyForContent = false
    set(value) {
      field = value
      readyForContentChangeListener?.onReadyForContentChanged(this)
    }

  override var saveCallback: TreehouseView.SaveCallback? = null

  override val stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override val savedStateRegistry: SavedStateRegistry? = null

  override fun toString() = name
}
