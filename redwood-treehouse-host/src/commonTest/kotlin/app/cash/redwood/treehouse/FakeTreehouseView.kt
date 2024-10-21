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
  private val eventLog: EventLog,
  override val onBackPressedDispatcher: OnBackPressedDispatcher,
  override val uiConfiguration: StateFlow<UiConfiguration> = MutableStateFlow(UiConfiguration()),
) : TreehouseView<WidgetValue> {
  override val children = FakeChildren()

  val views: List<WidgetValue>
    get() = children.views

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

  override var readyForContentChangeListener: ReadyForContentChangeListener<WidgetValue>? = null

  override var readyForContent = false
    set(value) {
      field = value
      readyForContentChangeListener?.onReadyForContentChanged(this)
    }

  override var saveCallback: TreehouseView.SaveCallback? = null

  override val stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override val savedStateRegistry: SavedStateRegistry? = null

  override fun contentState(loadCount: Int, attached: Boolean, uncaughtException: Throwable?) {
    // Remove all child views in case the previous content state left some behind.
    if (attached) children.views.clear()

    // Canonicalize "java.lang.Exception(boom!)" to "kotlin.Exception(boom!)".
    val exceptionString = uncaughtException?.toString()?.replace("java.lang.", "kotlin.")

    // TODO(jwilson): this is a backwards-compatibility shim. Emit a simpler event.
    eventLog += when {
      loadCount == 0 && !attached -> "codeListener.onInitialCodeLoading()"
      attached -> "codeListener.onCodeLoaded($loadCount)"
      else -> "codeListener.onCodeDetached($exceptionString)"
    }
  }

  override fun restart(restart: (() -> Unit)?) {
  }

  override fun toString() = name

  internal class FakeChildren(
    private val childrenDelegate: MutableListChildren<WidgetValue> = MutableListChildren(),
  ) : Widget.Children<WidgetValue> by childrenDelegate {
    /** A private copy of the child views that isn't cleared by `detach()`. */
    val views = mutableListOf<WidgetValue>()

    override fun insert(index: Int, widget: Widget<WidgetValue>) {
      views.add(index, widget.value)
      childrenDelegate.insert(index, widget)
    }

    override fun remove(index: Int, count: Int) {
      views.subList(index, index + count).clear()
      childrenDelegate.remove(index, count)
    }
  }
}
