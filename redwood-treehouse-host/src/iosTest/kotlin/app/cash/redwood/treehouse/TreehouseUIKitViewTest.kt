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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.objcPtr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.runUntilDate
import platform.UIKit.UIUserInterfaceStyle.UIUserInterfaceStyleDark
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.addSubview
import platform.UIKit.overrideUserInterfaceStyle
import platform.UIKit.removeFromSuperview
import platform.UIKit.subviews

@OptIn(ExperimentalCoroutinesApi::class)
class TreehouseUIKitViewTest {
  @Test fun widgetsAddChildViews() {
    val layout = TreehouseUIKitView()

    val view = UIView()
    layout.children.insert(0, viewWidget(view))
    assertEquals(1, layout.view.subviews.size)
    // For some reason `assertSame` fails on these references.
    assertEquals(view.objcPtr(), layout.view.subviews[0].objcPtr())
  }

  @Test fun attachAndDetachSendsStateChange() {
    val parent = UIView()
    val layout = TreehouseUIKitView()
    val listener = CountingReadyForContentChangeListener()

    layout.readyForContentChangeListener = listener
    assertEquals(0, listener.count)

    parent.addSubview(layout.view)
    assertEquals(1, listener.count)

    layout.view.removeFromSuperview()
    assertEquals(2, listener.count)
  }

  @Test fun resetClearsUntrackedChildren() {
    val layout = TreehouseUIKitView()

    layout.view.addSubview(UIView())
    assertEquals(1, layout.view.subviews.size)

    layout.reset()
    assertEquals(0, layout.view.subviews.size)
  }

  @Test fun resetClearsTrackedWidgets() {
    val layout = TreehouseUIKitView()

    // Needed to access internal state which cannot be reasonably observed through the public API.
    val children = layout.children as UIViewChildren

    children.insert(0, viewWidget(UIView()))
    assertEquals(1, children.widgets.size)

    layout.reset()
    assertEquals(0, children.widgets.size)
  }

  @Test
  fun hostConfigurationReflectsInitialUiMode() {
    val parent = UIWindow()
    parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark

    val layout = TreehouseUIKitView()
    parent.addSubview(layout.view)

    assertEquals(HostConfiguration(darkMode = true), layout.hostConfiguration.value)
  }

  @Test fun hostConfigurationEmitsUiModeChanges() = runTest {
    val parent = UIWindow()

    val layout = TreehouseUIKitView()
    parent.addSubview(layout.view)

    layout.hostConfiguration.test {
      assertEquals(HostConfiguration(darkMode = false), awaitItem())

      parent.overrideUserInterfaceStyle = UIUserInterfaceStyleDark
      // Style propagation through hierarchy is async so yield to run loop for any posted work.
      NSRunLoop.currentRunLoop.runUntilDate(NSDate())

      assertEquals(HostConfiguration(darkMode = true), awaitItem())
    }
  }

  private fun viewWidget(view: UIView) = object : Widget<UIView> {
    override val value: UIView get() = view
    override var layoutModifiers: LayoutModifier = LayoutModifier
  }
}
