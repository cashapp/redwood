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
package app.cash.redwood.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback as AndroidOnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher as AndroidOnBackPressedDispatcher
import androidx.core.view.children as viewGroupChildren
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.OnBackPressedCallback as RedwoodOnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher as RedwoodOnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ViewConstructor")
public open class RedwoodLayout(
  context: Context,
  androidOnBackPressedDispatcher: AndroidOnBackPressedDispatcher,
) : ViewGroup(context),
  RedwoodView<View> {

  final override val children: Widget.Children<View> = ViewGroupChildren(this)
  final override val value: View
    get() = this

  init {
    // The view needs to have an ID to participate in instance state saving.
    id = R.id.redwood_layout
  }

  private val mutableUiConfiguration = MutableStateFlow(
    computeUiConfiguration(
      viewInsets = Margin.Zero,
    ),
  )

  override val onBackPressedDispatcher: RedwoodOnBackPressedDispatcher =
    object : RedwoodOnBackPressedDispatcher {
      override fun addCallback(onBackPressedCallback: RedwoodOnBackPressedCallback): Cancellable {
        val androidOnBackPressedCallback = onBackPressedCallback.toAndroid()
        onBackPressedCallback.enabledChangedCallback = {
          androidOnBackPressedCallback.isEnabled = onBackPressedCallback.isEnabled
        }
        androidOnBackPressedDispatcher.addCallback(androidOnBackPressedCallback)
        return object : Cancellable {
          override fun cancel() {
            onBackPressedCallback.enabledChangedCallback = null
            androidOnBackPressedCallback.remove()
          }
        }
      }
    }

  override val savedStateRegistry: SavedStateRegistry? get() {
    // Resolve this lazily so that the view has a chance to get attached first.
    val owner = findViewTreeSavedStateRegistryOwner() ?: return null
    val key = "${SavedStateRegistry::class.java.simpleName}:$id"
    return AndroidSavedStateRegistry(key, owner.savedStateRegistry)
  }

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  init {
    setOnWindowInsetsChangeListener { insets ->
      mutableUiConfiguration.value = computeUiConfiguration(
        viewInsets = insets.safeDrawing.toMargin(Density(resources)),
      )
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var maxWidth = 0
    var maxHeight = 0
    for (child in viewGroupChildren) {
      child.measure(widthMeasureSpec, heightMeasureSpec)
      maxWidth = maxOf(maxWidth, child.measuredWidth)
      maxHeight = maxOf(maxHeight, child.measuredHeight)
    }
    setMeasuredDimension(maxWidth, maxHeight)
  }

  @SuppressLint("DrawAllocation") // It's only on layout.
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    mutableUiConfiguration.value = computeUiConfiguration()
    for (child in viewGroupChildren) {
      child.layout(0, 0, right - left, bottom - top)
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mutableUiConfiguration.value = computeUiConfiguration(config = newConfig)
  }

  private fun computeUiConfiguration(
    config: Configuration = context.resources.configuration,
    viewInsets: Margin = uiConfiguration.value.viewInsets,
  ): UiConfiguration {
    val viewportSize: Size
    val density: Double
    with(Density(resources)) {
      density = rawDensity
      viewportSize = Size(width.toDp(), height.toDp())
    }
    return UiConfiguration(
      darkMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES,
      safeAreaInsets = Margin.Zero,
      viewInsets = viewInsets,
      viewportSize = viewportSize,
      density = density,
      layoutDirection = when (config.layoutDirection) {
        View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> throw IllegalArgumentException("layoutDirection must be LTR or RTL")
      },
    )
  }
}

private fun RedwoodOnBackPressedCallback.toAndroid(): AndroidOnBackPressedCallback =
  object : AndroidOnBackPressedCallback(this@toAndroid.isEnabled) {
    override fun handleOnBackPressed() {
      this@toAndroid.handleOnBackPressed()
    }
  }
