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

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.RedwoodUIView
import app.cash.redwood.widget.UIViewRoot
import platform.UIKit.UIView

@ObjCName("TreehouseUIView", exact = true)
public class TreehouseUIView(
  override val widgetSystem: WidgetSystem<UIView>,
  root: UIViewRoot,
) : RedwoodUIView(root),
  TreehouseView<UIView> {
  override var saveCallback: TreehouseView.SaveCallback? = null
  override var stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override var readyForContentChangeListener: ReadyForContentChangeListener<UIView>? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  override val readyForContent: Boolean
    get() = root.value.superview != null

  public constructor(widgetSystem: WidgetSystem<UIView>) : this(widgetSystem, UIViewRoot())

  override fun superviewChanged() {
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }
}
