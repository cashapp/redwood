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
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIEdgeInsetsZero
import platform.UIKit.UILabel
import platform.UIKit.UIView

class RedwoodUIViewTest {
  @Test
  fun widgetsAddChildViews() {
    val redwoodUIView = RedwoodUIView()

    val label = UILabel()
    redwoodUIView.children.insert(0, UIViewWidget(label))

    assertThat(redwoodUIView.value.subviews).containsExactly(label)
  }

  /** Confirm we accept and propagates insets through [RedwoodUIView.uiConfiguration]. */
  @Test
  fun viewInsets() {
    val redwoodUIView = RedwoodUIView()
    val insetsContainer = InsetsContainer()
    insetsContainer.addSubview(redwoodUIView.value)

    assertThat(redwoodUIView.uiConfiguration.value.viewInsets)
      .isEqualTo(Margin.Zero)

    insetsContainer.subviewSafeAreaInsets = UIEdgeInsetsMake(10.0, 20.0, 30.0, 40.0)

    assertThat(redwoodUIView.uiConfiguration.value.viewInsets)
      .isEqualTo(Margin(top = 10.0.dp, start = 20.0.dp, bottom = 30.0.dp, end = 40.0.dp))
  }

  class UIViewWidget(
    override val value: UIView,
  ) : Widget<UIView> {
    override var modifier: Modifier = Modifier
  }

  /** Override [safeAreaInsets] to propagate a test value to subviews on the next layout. */
  class InsetsContainer : UIView(cValue { CGRectZero }) {
    var subviewSafeAreaInsets = cValue<UIEdgeInsets> { UIEdgeInsetsZero }
      set(value) {
        field = value
        setNeedsLayout()
        layoutIfNeeded()
      }

    override fun safeAreaInsets() = subviewSafeAreaInsets
  }
}
