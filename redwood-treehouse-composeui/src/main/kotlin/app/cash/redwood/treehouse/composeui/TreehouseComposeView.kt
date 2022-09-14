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
package app.cash.redwood.treehouse.composeui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlinx.serialization.json.JsonArray

@SuppressLint("ViewConstructor")
public class TreehouseComposeView<T : Any>(
  context: Context,
  private val treehouseApp: TreehouseApp<T>,
  public val widgetFactory: Widget.Factory<@Composable () -> Unit>,
) : FrameLayout(context), TreehouseView<T> {
  public val composeView: ComposeView = ComposeView(context)

  init {
    addView(composeView)
  }

  /** This is always the user-supplied content. */
  private var content: TreehouseView.Content<T>? = null

  /** This is the actual content, or null if not attached to the screen. */
  override val boundContent: TreehouseView.Content<T>?
    get() {
      return when {
        isAttachedToWindow -> content
        else -> null
      }
    }

  override val protocolDisplayRoot: DiffConsumingWidget<*> = ProtocolDisplayRoot(this)

  public fun setContent(content: TreehouseView.Content<T>) {
    treehouseApp.dispatchers.checkUi()
    this.content = content
    treehouseApp.onContentChanged(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    treehouseApp.onContentChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    treehouseApp.onContentChanged(this)
  }

  override fun generateDefaultLayoutParams(): LayoutParams =
    LayoutParams(MATCH_PARENT, MATCH_PARENT)
}

internal class ProtocolDisplayRoot(
  private val treehouseComposeView: TreehouseComposeView<*>,
) : DiffConsumingWidget<@Composable () -> Unit> {

  init {
    treehouseComposeView.composeView.setContent {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        children.render()
      }
    }
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
  private val children = ComposeWidgetChildren()

  override val value: @Composable () -> Unit
    get() {
      error("unexpected call")
    }

  override fun updateLayoutModifier(value: JsonArray) {
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    error("unexpected update on view root: $diff")
  }

  override fun children(tag: Int) = when (tag) {
    RootChildrenTag -> children
    else -> error("unexpected tag: $tag")
  }
}
