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

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIApplication
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIView

@ObjCName("TreehouseUIKitView", exact = true)
public class TreehouseUIKitView(
  override val widgetSystem: WidgetSystem,
) : TreehouseView {
  public val view: UIView = RootUiView(this)
  override var saveCallback: TreehouseView.SaveCallback? = null
  override var stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override var readyForContentChangeListener: ReadyForContentChangeListener? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  override val readyForContent: Boolean
    get() = view.superview != null

  private val _children = UIViewChildren(view)
  override val children: Widget.Children<UIView> get() = _children

  private val mutableUiConfiguration =
    MutableStateFlow(computeUiConfiguration(view.traitCollection, view.bounds))

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    @Suppress("UNCHECKED_CAST") // Correct generic lost by cinterop.
    (view.subviews as List<UIView>).forEach(UIView::removeFromSuperview)
  }

  internal fun superviewChanged() {
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  internal fun updateUiConfiguration() {
    mutableUiConfiguration.value = computeUiConfiguration(
      traitCollection = view.traitCollection,
      bounds = view.bounds,
    )
  }
}

private fun computeUiConfiguration(
  traitCollection: UITraitCollection,
  bounds: CValue<CGRect>,
): UiConfiguration {
  return UiConfiguration(
    darkMode = traitCollection.userInterfaceStyle == UIUserInterfaceStyleDark,
    safeAreaInsets = computeSafeAreaInsets(),
    viewportSize = bounds.useContents {
      with(Density.Default) {
        Size(size.width.toDp(), size.height.toDp())
      }
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

private class RootUiView(
  private val treehouseView: TreehouseUIKitView,
) : UIView(cValue { CGRectZero }) {
  override fun layoutSubviews() {
    // Bounds likely changed. Report new size.
    treehouseView.updateUiConfiguration()

    subviews.forEach {
      (it as UIView).setFrame(bounds)
    }
  }

  override fun didMoveToSuperview() {
    treehouseView.superviewChanged()
  }

  override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
    super.traitCollectionDidChange(previousTraitCollection)
    treehouseView.updateUiConfiguration()
  }
}
