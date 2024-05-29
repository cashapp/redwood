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

import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.SavedStateRegistry
import app.cash.redwood.widget.Widget
import com.example.redwood.testapp.protocol.host.TestSchemaProtocolFactory
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * An in-memory fake.
 *
 * This pretends to be like a real UI by keeping an independent copy of the views. That way
 * [Widget.Children.detach] can clear the widget (adapters) without forcing an update to the UI.
 */
internal class FakeTreehouseView(
  private val name: String,
  override val onBackPressedDispatcher: OnBackPressedDispatcher,
  override val uiConfiguration: StateFlow<UiConfiguration> = MutableStateFlow(UiConfiguration()),
) : TreehouseView<WidgetValue> {
  private val mutableListChildren = MutableListChildren<WidgetValue>()
  private val mutableViews = mutableListOf<WidgetValue>()

  val views: List<WidgetValue>
    get() = mutableViews

  @OptIn(RedwoodCodegenApi::class)
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

  override var readyForContentChangeListener: ReadyForContentChangeListener<WidgetValue>? = null

  override var readyForContent = false
    set(value) {
      field = value
      readyForContentChangeListener?.onReadyForContentChanged(this)
    }

  override var saveCallback: TreehouseView.SaveCallback? = null

  override val stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override val children = object : Widget.Children<WidgetValue> by mutableListChildren {
    override fun insert(index: Int, widget: Widget<WidgetValue>) {
      mutableViews.add(index, widget.value)
      mutableListChildren.insert(index, widget)
    }

    override fun remove(index: Int, count: Int) {
      mutableViews.subList(index, index + count).clear()
      mutableListChildren.remove(index, count)
    }

    override fun onModifierUpdated(index: Int, widget: Widget<WidgetValue>) {
    }
  }

  override val savedStateRegistry: SavedStateRegistry? = null

  override fun reset() {
    mutableViews.clear()
    mutableListChildren.clear()
  }

  override fun toString() = name
}
