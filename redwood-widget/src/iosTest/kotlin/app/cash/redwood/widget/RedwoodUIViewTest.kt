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
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.additionalSafeAreaInsets

class RedwoodUIViewTest {
  @Test
  fun widgetsAddChildViews() {
    val redwoodUIView = RedwoodUIView()

    val label = UILabel()
    redwoodUIView.children.insert(0, UIViewWidget(label))

    assertThat(redwoodUIView.value.subviews).containsExactly(label)
  }

  /**
   * Confirm we accept and propagates insets through [RedwoodUIView.uiConfiguration].
   *
   * Testing insets is tricky. We need both a [UIWindow] and a [UIViewController] to apply insets to
   * a subject view.
   */
  @Test
  fun viewInsets() {
    val redwoodUIView = RedwoodUIView()
    val viewController = object : UIViewController(null, null) {
      override fun loadView() {
        view = redwoodUIView.value
      }
    }

    val window = UIWindow(
      CGRectMake(0.0, 0.0, 390.0, 844.0), // iPhone 14.
    )
    window.setHidden(false) // Necessary to propagate additionalSafeAreaInsets.
    window.rootViewController = viewController

    assertThat(redwoodUIView.uiConfiguration.value.viewInsets)
      .isEqualTo(Margin.Zero)

    viewController.additionalSafeAreaInsets = UIEdgeInsetsMake(10.0, 20.0, 30.0, 40.0)

    assertThat(redwoodUIView.uiConfiguration.value.viewInsets)
      .isEqualTo(Margin(top = 10.0.dp, start = 20.0.dp, bottom = 30.0.dp, end = 40.0.dp))
  }

  class UIViewWidget(
    override val value: UIView,
  ) : Widget<UIView> {
    override var modifier: Modifier = Modifier
  }
}
