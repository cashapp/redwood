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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.widget.Widget

@SuppressLint("ViewConstructor")
public class TreehouseWidgetView<T : Any>(
  context: Context,
  private val treehouseHost: TreehouseHost<T>,
  public val widgetFactory: Widget.Factory<View>,
) : FrameLayout(context), TreehouseView<T> {
  /** This is always the user-supplied content. */
  private var content: TreehouseContent<T>? = null

  /** This is the actual content, or null if not attached to the screen. */
  override val boundContent: TreehouseContent<T>?
    get() {
      return when {
        isAttachedToWindow -> content
        else -> null
      }
    }

  override val protocolDisplayRoot: DiffConsumingWidget<*> = ProtocolDisplayRoot(this)

  public fun setContent(content: TreehouseContent<T>) {
    treehouseHost.dispatchers.checkMain()
    this.content = content
    treehouseHost.onContentChanged(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    treehouseHost.onContentChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    treehouseHost.onContentChanged(this)
  }
}
