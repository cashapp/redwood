/*
 * Copyright (C) 2024 Square, Inc.
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

import app.cash.redwood.Modifier
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentFill
import platform.UIKit.UIStackViewDistributionFillEqually
import platform.UIKit.UITraitCollection
import platform.UIKit.UIView

/**
 * A default base implementation of [RedwoodView.Root] suitable for subclassing.
 *
 * The [value] view contributes nothing to the view hierarchy. It forwards all measurement and
 * layout calls from its own parent view to its child views.
 */
@ObjCName("UIViewRoot", exact = true)
public open class UIViewRoot : RedwoodView.Root<UIView> {
  internal val valueRootView: RootUIStackView = RootUIStackView()

  override val value: UIView
    get() = valueRootView

  private val _children = UIViewChildren(valueRootView)
  override val children: Widget.Children<UIView>
    get() = _children

  override var modifier: Modifier = Modifier

  override fun contentState(
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
  }

  override fun restart(restart: (() -> Unit)?) {
  }

  /**
   * In practice we expect this to contain either zero child subviews (especially when
   * newly-initialized) or one child subview, which will usually be a layout container.
   *
   * This could just as easily be a horizontal stack. A box would be even better, but there's no
   * such built-in component and implementing it manually is difficult if we want to react to
   * content resizes.
   */
  internal class RootUIStackView : UIStackView(cValue { CGRectZero }) {
    var redwoodUIView: RedwoodUIView? = null

    init {
      this.axis = UILayoutConstraintAxisVertical
      this.alignment = UIStackViewAlignmentFill // Fill horizontal.
      this.distribution = UIStackViewDistributionFillEqually // Fill vertical.
    }

    override fun layoutSubviews() {
      super.layoutSubviews()

      // Bounds likely changed. Report new size.
      redwoodUIView?.updateUiConfiguration()
    }

    override fun didMoveToSuperview() {
      super.didMoveToSuperview()
      redwoodUIView?.superviewChanged()
    }

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
      super.traitCollectionDidChange(previousTraitCollection)
      redwoodUIView?.updateUiConfiguration()
    }
  }
}
