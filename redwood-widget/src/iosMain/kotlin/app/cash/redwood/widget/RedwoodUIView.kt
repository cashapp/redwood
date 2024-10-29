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

import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIApplication
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentFill
import platform.UIKit.UIStackViewDistributionFillEqually
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceLayoutDirection
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionLeftToRight
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView

@ObjCName("RedwoodUIView", exact = true)
public open class RedwoodUIView : RedwoodView<UIView> {
  private val valueRootView: RootUIStackView = RootUIStackView()

  override val value: UIView
    get() = valueRootView

  private val _children = UIViewChildren(valueRootView)
  override val children: Widget.Children<UIView>
    get() = _children

  private val mutableUiConfiguration =
    MutableStateFlow(
      computeUiConfiguration(
        traitCollection = valueRootView.traitCollection,
        layoutDirection = valueRootView.effectiveUserInterfaceLayoutDirection,
        bounds = valueRootView.bounds,
      ),
    )

  override val onBackPressedDispatcher: OnBackPressedDispatcher = object : OnBackPressedDispatcher {
    override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
      return object : Cancellable {
        override fun cancel() = Unit
      }
    }
  }

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  override val savedStateRegistry: SavedStateRegistry?
    get() = null

  private fun updateUiConfiguration() {
    mutableUiConfiguration.value = computeUiConfiguration(
      traitCollection = valueRootView.traitCollection,
      layoutDirection = valueRootView.effectiveUserInterfaceLayoutDirection,
      bounds = valueRootView.bounds,
    )
  }

  protected open fun superviewChanged() {
  }

  /**
   * In practice we expect this to contain either zero child subviews (especially when
   * newly-initialized) or one child subview, which will usually be a layout container.
   *
   * This could just as easily be a horizontal stack. A box would be even better, but there's no
   * such built-in component and implementing it manually is difficult if we want to react to
   * content resizes.
   */
  private inner class RootUIStackView : UIStackView(cValue { CGRectZero }) {
    init {
      this.axis = UILayoutConstraintAxisVertical
      this.alignment = UIStackViewAlignmentFill // Fill horizontal.
      this.distribution = UIStackViewDistributionFillEqually // Fill vertical.
    }

    override fun layoutSubviews() {
      super.layoutSubviews()

      // Bounds likely changed. Report new size.
      updateUiConfiguration()
    }

    override fun didMoveToSuperview() {
      super.didMoveToSuperview()
      superviewChanged()
    }

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
      super.traitCollectionDidChange(previousTraitCollection)
      updateUiConfiguration()
    }
  }
}

internal fun computeUiConfiguration(
  traitCollection: UITraitCollection,
  layoutDirection: UIUserInterfaceLayoutDirection,
  bounds: CValue<CGRect>,
): UiConfiguration {
  return UiConfiguration(
    darkMode = traitCollection.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark,
    safeAreaInsets = computeSafeAreaInsets(),
    viewportSize = bounds.useContents {
      with(Density.Default) {
        Size(size.width.toDp(), size.height.toDp())
      }
    },
    density = Density.Default.rawDensity,
    layoutDirection = when (layoutDirection) {
      UIUserInterfaceLayoutDirectionRightToLeft -> LayoutDirection.Rtl
      UIUserInterfaceLayoutDirectionLeftToRight -> LayoutDirection.Ltr
      else -> throw IllegalArgumentException("Layout direction must be RightToLeft or LeftToRight")
    },
  )
}

private fun computeSafeAreaInsets(): Margin {
  val keyWindow = UIApplication.sharedApplication.keyWindow ?: return Margin.Zero
  return keyWindow.safeAreaInsets.useContents {
    with(Density.Default) {
      Margin(left.toDp(), right.toDp(), top.toDp(), bottom.toDp())
    }
  }
}
