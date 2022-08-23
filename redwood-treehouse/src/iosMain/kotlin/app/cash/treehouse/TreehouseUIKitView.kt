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
package app.cash.treehouse

import app.cash.redwood.protocol.widget.DiffConsumingWidget
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIStackView
import platform.UIKit.UIView

public class TreehouseUIKitView<T : Any>(
  private val content: TreehouseView.Content<T>,
) : TreehouseView<T> {
  public val view: UIView = UIStackView(frame = cValue { CGRectZero })
  private var treehouseApp: TreehouseApp<T>? = null

  // TODO(jwilson): track when this view is detached from screen
  override val boundContent: TreehouseView.Content<T>? = content

  override val protocolDisplayRoot: DiffConsumingWidget<*> =
    ProtocolDisplayRoot(view)

  public fun register(treehouseApp: TreehouseApp<T>?) {
    this.treehouseApp = treehouseApp
    treehouseApp?.onContentChanged(this)
  }
}
