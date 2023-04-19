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
import androidx.core.graphics.Insets
import app.cash.redwood.layout.api.Density
import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ViewConstructor")
public class TreehouseWidgetView(
  context: Context,
  override val widgetSystem: WidgetSystem,
) : FrameLayout(context), TreehouseView {
  override var readyForContentChangeListener: ReadyForContentChangeListener? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  /**
   * Like [View.isAttachedToWindow]. We'd prefer that property but it's false until
   * [onAttachedToWindow] returns and true until [onDetachedFromWindow] returns.
   */
  override var readyForContent: Boolean = false
    private set

  private val _children = ViewGroupChildren(this)
  override val children: Widget.Children<View> get() = _children

  private val mutableHostConfiguration = MutableStateFlow(computeHostConfiguration())

  override val hostConfiguration: StateFlow<HostConfiguration>
    get() = mutableHostConfiguration

  init {
    setOnWindowInsetsChangeListener { insets ->
      mutableHostConfiguration.value = computeHostConfiguration(insets = insets.safeDrawing)
    }
  }

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    removeAllViews()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    readyForContent = true
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    readyForContent = false
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mutableHostConfiguration.value = computeHostConfiguration(config = newConfig)
  }

  override fun generateDefaultLayoutParams(): LayoutParams =
    LayoutParams(MATCH_PARENT, MATCH_PARENT)

  private fun computeHostConfiguration(
    config: Configuration = context.resources.configuration,
    insets: Insets = rootWindowInsetsCompat.safeDrawing,
  ): HostConfiguration {
    return HostConfiguration(
      darkMode = (config.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES,
      safeAreaInsets = insets.toMargin(Density(resources)),
    )
  }
}
