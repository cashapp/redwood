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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ViewConstructor")
public class TreehouseWidgetView<T : Any>(
  context: Context,
  private val treehouseApp: TreehouseApp<T>,
) : FrameLayout(context), TreehouseView<T> {
  private var content: TreehouseView.Content<T>? = null

  override val boundContent: TreehouseView.Content<T>?
    get() {
      return when {
        isAttachedToWindow -> content
        else -> null
      }
    }

  override val children: Widget.Children<*> = ViewGroupChildren(this)

  private val mutableHostConfiguration =
    MutableStateFlow(computeHostConfiguration(context.resources.configuration))

  override val hostConfiguration: StateFlow<HostConfiguration>
    get() = mutableHostConfiguration

  override fun reset() {
    children.remove(0, childCount)
  }

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
