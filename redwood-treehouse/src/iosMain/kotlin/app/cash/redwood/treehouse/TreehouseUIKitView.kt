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

import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.UIKit.addSubview
import platform.UIKit.removeFromSuperview
import platform.UIKit.setFrame
import platform.UIKit.subviews
import platform.UIKit.superview

public class TreehouseUIKitView<T : Any>(
  private val treehouseApp: TreehouseApp<T>,
) : TreehouseView<T> {
  public val view: UIView = RootUiView(this)
  private var content: TreehouseView.Content<T>? = null

  override val boundContent: TreehouseView.Content<T>?
    get() {
      return when {
        view.superview != null -> content
        else -> null
      }
    }

  override val children: Widget.Children<*> =
    MutableListChildren { newViews ->
      @Suppress("UNCHECKED_CAST") // cinterop loses the generic.
      (view.subviews as List<UIView>).forEach(UIView::removeFromSuperview)
      newViews.forEach(view::addSubview)
    }

  public fun setContent(content: TreehouseView.Content<T>) {
    treehouseApp.dispatchers.checkUi()
    this.content = content
    treehouseApp.onContentChanged(this)
  }

  internal fun superviewChanged() {
    treehouseApp.onContentChanged(this)
  }
}

@Suppress("unused") // cinterop erroneously exposes these as extension functions.
private class RootUiView(
  private val treehouseView: TreehouseUIKitView<*>,
) : UIView(cValue { CGRectZero }) {
  @ObjCAction fun layoutSubviews() {
    subviews.forEach {
      (it as UIView).setFrame(bounds)
    }
  }

  @ObjCAction fun didMoveToSuperview() {
    treehouseView.superviewChanged()
  }
}
