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
package app.cash.redwood.treehouse

import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreGraphics.CGRect
import platform.UIKit.UIApplication
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView

public open class RedwoodUIKitView(
  public val view: UIView,
) : RedwoodView<UIView> {
  private val _children = UIViewChildren(view)
  override val children: Widget.Children<UIView> get() = _children

  private val mutableUiConfiguration =
    MutableStateFlow(computeUiConfiguration(view.traitCollection, view.bounds))

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  internal fun updateUiConfiguration() {
    mutableUiConfiguration.value = computeUiConfiguration(
      traitCollection = view.traitCollection,
      bounds = view.bounds,
    )
  }
}

internal fun computeUiConfiguration(
  traitCollection: UITraitCollection,
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
