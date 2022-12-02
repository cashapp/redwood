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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import app.cash.redwood.treehouse.TreehouseView.CodeListener
import app.cash.redwood.treehouse.TreehouseView.OnStateChangeListener
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget
import kotlin.DeprecationLevel.ERROR
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ViewConstructor")
public class TreehouseWidgetView<A : Any>(
  context: Context,
  override val widgetSystem: TreehouseView.WidgetSystem<A>,
) : FrameLayout(context), TreehouseView<A> {
  @Deprecated(
    message = "TreehouseView no longer owns a TreehouseApp. Instead, call app.renderTo(view).",
    replaceWith = ReplaceWith("TreehouseWidgetView(context, widgetSystem).also(treehouseApp::renderTo)"),
    level = ERROR,
  )
  @Suppress("UNUSED_PARAMETER")
  public constructor(
    context: Context,
    treehouseApp: TreehouseApp<A>,
    widgetSystem: TreehouseView.WidgetSystem<A>,
  ) : this(context, widgetSystem)

  public override var codeListener: CodeListener = CodeListener()
  public override var stateChangeListener: OnStateChangeListener<A>? = null
    set(value) {
      check(value != null) { "Views cannot be unbound from a listener at this time" }
      check(field == null) { "View already bound to a listener" }
      field = value
    }

  private var content: TreehouseView.Content<A>? = null

  override val boundContent: TreehouseView.Content<A>?
    get() {
      return when {
        isAttachedToWindow -> content
        else -> null
      }
    }

  private val _children = ViewGroupChildren(this)
  override val children: Widget.Children<View> get() = _children

  private val mutableHostConfiguration =
    MutableStateFlow(computeHostConfiguration(context.resources.configuration))

  override val hostConfiguration: StateFlow<HostConfiguration>
    get() = mutableHostConfiguration

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    removeAllViews()
  }

  public fun setContent(content: TreehouseView.Content<A>) {
    this.content = content
    stateChangeListener?.onStateChanged(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    stateChangeListener?.onStateChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    stateChangeListener?.onStateChanged(this)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mutableHostConfiguration.value = computeHostConfiguration(newConfig)
  }

  override fun generateDefaultLayoutParams(): LayoutParams =
    LayoutParams(MATCH_PARENT, MATCH_PARENT)
}

private fun computeHostConfiguration(
  config: Configuration,
): HostConfiguration {
  return HostConfiguration(
    darkMode = (config.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES,
  )
}
