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

import app.cash.redwood.treehouse.TreehouseView.CodeListener
import app.cash.redwood.widget.SwiftUIChildren
import app.cash.redwood.widget.SwiftUIView
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class TreehouseSwiftUIView<A : Any>(
  private val treehouseApp: TreehouseApp<A>,
  override val widgetSystem: TreehouseView.WidgetSystem<A>,
) : TreehouseView<A>, SwiftUIView {

  public override var codeListener: CodeListener = CodeListener()
  private var content: TreehouseView.Content<A>? = null

  private val _children = SwiftUIChildren(this)
  override val children: Widget.Children<*> = _children
  public override var stateChangeListener: TreehouseView.OnStateChangeListener<A>? = null

  override fun reset() {
    _children.remove(0, _children.widgets.size)
  }

  override val boundContent: TreehouseView.Content<A>?
    get() = content

  private val mutableHostConfiguration = MutableStateFlow(HostConfiguration())

  override val hostConfiguration: StateFlow<HostConfiguration>
    get() = mutableHostConfiguration

  public fun setContent(content: TreehouseView.Content<A>) {
    treehouseApp.dispatchers.checkUi()
    this.content = content
    stateChangeListener?.onStateChanged(this)
  }
}
