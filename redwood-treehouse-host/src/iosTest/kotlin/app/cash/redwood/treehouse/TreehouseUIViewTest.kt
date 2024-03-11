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

import app.cash.redwood.treehouse.LayoutTester.Constraint
import app.cash.redwood.treehouse.LayoutTester.Subject
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.UIViewChildren
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.runUntilDate
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIView
import platform.UIKit.UIWindow

class TreehouseUIViewTest {
  @Test fun widgetsAddChildViews() {
    val layout = TreehouseUIView(throwingWidgetSystem)

    val view = UIView()
    layout.children.insert(0, viewWidget(view))
    assertThat(layout.view.subviews).hasSize(1)
    // For some reason `assertSame` fails on these references.
    assertThat(layout.view.subviews[0].objcPtr()).isEqualTo(view.objcPtr())
  }

  @Test fun attachAndDetachSendsStateChange() {
    val parent = UIView()
    val layout = TreehouseUIView(throwingWidgetSystem)
    val listener = CountingReadyForContentChangeListener<UIView>()

    layout.readyForContentChangeListener = listener
    assertThat(listener.count).isEqualTo(0)

    parent.addSubview(layout.view)
    assertThat(listener.count).isEqualTo(1)

    layout.view.removeFromSuperview()
    assertThat(listener.count).isEqualTo(2)
  }

  @Test fun resetClearsUntrackedChildren() {
    val layout = TreehouseUIView(throwingWidgetSystem)

    layout.view.addSubview(UIView())
    assertThat(layout.view.subviews).hasSize(1)

    layout.reset()
    assertThat(layout.view.subviews).hasSize(0)
  }

  @Test fun resetClearsTrackedWidgets() {
    val layout = TreehouseUIView(throwingWidgetSystem)

    // Needed to access internal state which cannot be reasonably observed through the public API.
    val children = layout.children as UIViewChildren

    children.insert(0, viewWidget(UIView()))
    assertThat(children.widgets).hasSize(1)

    layout.reset()
    assertThat(children.widgets).hasSize(0)
  }

  @Test
  fun uiConfigurationReflectsInitialUiMode() {
    val parent = UIWindow()
    parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.view)

    assertThat(layout.uiConfiguration.value).isEqualTo(UiConfiguration(darkMode = true))
  }

  @Test fun uiConfigurationEmitsUiModeChanges() = runTest {
    val parent = UIWindow()

    val layout = TreehouseUIView(throwingWidgetSystem)
    parent.addSubview(layout.view)

    layout.uiConfiguration.test {
      assertThat(awaitItem()).isEqualTo(UiConfiguration(darkMode = false))

      parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark
      // Style propagation through hierarchy is async so yield to run loop for any posted work.
      NSRunLoop.currentRunLoop.runUntilDate(NSDate())

      assertThat(awaitItem()).isEqualTo(UiConfiguration(darkMode = true))
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
    parent.addSubview(layout.view)

    assertThat(layout.uiConfiguration.value)
      .isEqualTo(UiConfiguration(safeAreaInsets = expectedInsets))
  }

  /** Confirm that the layout with the treehouse view is the same as the layout without it. */
  @Test fun layoutIsPassThrough() {
    for (horizontal in Constraint.entries) {
      for (vertical in Constraint.entries) {
        assertThat(LayoutTester(Subject.Reference, horizontal, vertical).subjectFrame())
          .isEqualTo(
            Rectangle(
              x = horizontal.initialX,
              y = vertical.initialY,
              width = horizontal.initialWidth,
              height = vertical.initialHeight,
            ),
          )

        assertThat(LayoutTester(Subject.TreehouseView, horizontal, vertical).subjectFrame())
          .isEqualTo(
            Rectangle(
              x = horizontal.initialX,
              y = vertical.initialY,
              width = horizontal.initialWidth,
              height = vertical.initialHeight,
            ),
          )
      }
    }
  }
}
