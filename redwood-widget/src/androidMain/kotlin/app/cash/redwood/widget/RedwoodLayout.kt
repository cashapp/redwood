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
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback as AndroidOnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher as AndroidOnBackPressedDispatcher
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
) : FrameLayout(context),
  RedwoodView<View> {
  init {
    // The view needs to have an ID to participate in instance state saving.
    id = R.id.redwood_layout
  }

  private val _children = ViewGroupChildren(this)
  override val children: Widget.Children<View> get() = _children

  private val mutableUiConfiguration = MutableStateFlow(
    computeUiConfiguration(
      windowInsets = Margin.Zero,
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

  override var windowInsets: Margin
    get() = uiConfiguration.value.windowInsets
    set(value) {
      val old = uiConfiguration.value
      mutableUiConfiguration.value = UiConfiguration(
        darkMode = old.darkMode,
        safeAreaInsets = old.safeAreaInsets,
        windowInsets = value,
        viewportSize = old.viewportSize,
        density = old.density,
        layoutDirection = old.layoutDirection,
      )
    }

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    removeAllViews()
  }

  init {
    setOnWindowInsetsChangeListener { insets ->
      val old = mutableUiConfiguration.value
      mutableUiConfiguration.value = computeUiConfiguration(
        safeAreaInsets = insets.safeDrawing,
        windowInsets = old.windowInsets,
      )
    }
  }

  @SuppressLint("DrawAllocation") // It's only on layout.
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    val old = mutableUiConfiguration.value
    mutableUiConfiguration.value = computeUiConfiguration(
      windowInsets = old.windowInsets,
    )
    super.onLayout(changed, left, top, right, bottom)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val old = mutableUiConfiguration.value
    mutableUiConfiguration.value = computeUiConfiguration(
      config = newConfig,
      windowInsets = old.windowInsets,
    )
  }

  private fun computeUiConfiguration(
    config: Configuration = context.resources.configuration,
    safeAreaInsets: Insets = rootWindowInsetsCompat.safeDrawing,
    windowInsets: Margin,
  ): UiConfiguration {
    val viewportSize: Size
    val density: Double
    with(Density(resources)) {
      density = rawDensity
      viewportSize = Size(width.toDp(), height.toDp())
    }
    return UiConfiguration(
      darkMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES,
      safeAreaInsets = safeAreaInsets.toMargin(Density(resources)),
      windowInsets = windowInsets,
      viewportSize = viewportSize,
      density = density,
      layoutDirection = when (config.layoutDirection) {
        View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> throw IllegalArgumentException("layoutDirection must be LTR or RTL")
      },
    )
  }

  /**
   * Consume the identified insets from the enclosing UI in the Redwood composition.
   *
   * This installs a `OnApplyWindowInsetsListener` on this view that updates [windowInsets] each
   * time the window insets are updated.
   */
  public fun consumeWindowInsets(
    mask: Int = (WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()),
  ) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
      val insetsForMask = insets.getInsets(mask)
      windowInsets = insetsForMask.toMargin(Density(resources))
      WindowInsetsCompat.CONSUMED
    }
  }
}

private fun RedwoodOnBackPressedCallback.toAndroid(): AndroidOnBackPressedCallback =
  object : AndroidOnBackPressedCallback(this@toAndroid.isEnabled) {
    override fun handleOnBackPressed() {
      this@toAndroid.handleOnBackPressed()
    }
  }
