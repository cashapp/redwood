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

import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.runUntilDate
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionLeftToRight
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIView
import platform.UIKit.UIWindow

class TreehouseUIViewTest {
  @Test fun widgetsAddChildViews() {
    val layout = TreehouseUIView(throwingWidgetSystem)

    val view = UIView()
    layout.root.children.insert(0, viewWidget(view))
    assertThat(layout.root.value.subviews).hasSize(1)
    // For some reason `assertSame` fails on these references.
    assertThat(layout.root.value.subviews[0].objcPtr()).isEqualTo(view.objcPtr())
  }

  @Test fun attachAndDetachSendsStateChange() {
    val parent = UIView()
    val layout = TreehouseUIView(throwingWidgetSystem)
    val listener = CountingReadyForContentChangeListener<UIView>()

    layout.readyForContentChangeListener = listener
    assertThat(listener.count).isEqualTo(0)

    parent.addSubview(layout.root.value)
    assertThat(listener.count).isEqualTo(1)

    layout.root.value.removeFromSuperview()
    assertThat(listener.count).isEqualTo(2)
  }

  @Test
  fun uiConfigurationReflectsInitialUiMode() {
    val parent = UIWindow()
    parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.root.value)

    assertThat(layout.uiConfiguration.value.darkMode).isTrue()
  }

  @Test fun uiConfigurationEmitsUiModeChanges() = runTest {
    val parent = UIWindow()

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.root.value)

    layout.uiConfiguration.test {
      assertThat(awaitItem().darkMode).isFalse()

      parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark
      // Style propagation through hierarchy is async so yield to run loop for any posted work.
      NSRunLoop.currentRunLoop.runUntilDate(NSDate())

      assertThat(awaitItem().darkMode).isTrue()
    }
  }

  @Test fun uiConfigurationReflectsInitialSafeAreaInsets() = runTest {
    val parent = UIWindow()

    // We can't override this value so test that they match.
    val expectedInsets = parent.safeAreaInsets.useContents {
      with(Density.Default) {
        Margin(left.toDp(), right.toDp(), top.toDp(), bottom.toDp())
      }
    }

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.root.value)

    assertThat(layout.uiConfiguration.value.safeAreaInsets).isEqualTo(expectedInsets)
  }

  @Test
  fun uiConfigurationReflectsInitialLayoutDirection() {
    val parent = UIWindow()

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.root.value)

    val expectedLayoutDirection = when (val direction = parent.effectiveUserInterfaceLayoutDirection) {
      UIUserInterfaceLayoutDirectionLeftToRight -> LayoutDirection.Ltr
      UIUserInterfaceLayoutDirectionRightToLeft -> LayoutDirection.Rtl
      else -> throw IllegalStateException("Unknown layout direction $direction")
    }
    assertThat(layout.uiConfiguration.value.layoutDirection).isEqualTo(expectedLayoutDirection)
  }
}
