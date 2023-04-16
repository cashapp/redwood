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

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget
import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TreehouseWidgetViewTest {
  private val context = RuntimeEnvironment.getApplication()!!

  @Test fun widgetsAddChildViews() {
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)

    val view = View(context)
    layout.children.insert(0, viewWidget(view))
    assertEquals(1, layout.childCount)
    assertSame(view, layout.getChildAt(0))
  }

  @Test fun attachAndDetachSendsStateChange() {
    val activity = Robolectric.buildActivity(Activity::class.java).resume().visible().get()
    val parent = activity.findViewById<ViewGroup>(android.R.id.content)
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)
    val listener = CountingReadyForContentChangeListener()

    layout.readyForContentChangeListener = listener
    assertEquals(0, listener.count)

    parent.addView(layout)
    assertEquals(1, listener.count)

    parent.removeView(layout)
    assertEquals(2, listener.count)
  }

  @Test fun resetClearsUntrackedChildren() {
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)

    layout.addView(View(context))
    assertEquals(1, layout.childCount)

    layout.reset()
    assertEquals(0, layout.childCount)
  }

  @Test fun resetClearsTrackedWidgets() {
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)

    // Needed to access internal state which cannot be reasonably observed through the public API.
    val children = layout.children as ViewGroupChildren

    children.insert(0, viewWidget(View(context)))
    assertEquals(1, children.widgets.size)

    layout.reset()
    assertEquals(0, children.widgets.size)
  }

  @Test
  @Config(sdk = [26])
  fun hostConfigurationReflectsInitialUiMode() {
    val newConfig = Configuration(context.resources.configuration)
    newConfig.uiMode = (newConfig.uiMode and UI_MODE_NIGHT_MASK.inv()) or UI_MODE_NIGHT_YES
    val newContext = context.createConfigurationContext(newConfig) // Needs API 26.
    val layout = TreehouseWidgetView(newContext, throwingWidgetSystem)
    assertEquals(HostConfiguration(darkMode = true), layout.hostConfiguration.value)
  }

  @Test fun hostConfigurationEmitsUiModeChanges() = runTest {
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)
    layout.hostConfiguration.test {
      assertEquals(HostConfiguration(darkMode = false), awaitItem())

      val newConfig = Configuration(context.resources.configuration)
      newConfig.uiMode = (newConfig.uiMode and UI_MODE_NIGHT_MASK.inv()) or UI_MODE_NIGHT_YES

      layout.dispatchConfigurationChanged(newConfig)
      assertEquals(HostConfiguration(darkMode = true), awaitItem())
    }
  }

  @Test fun hostConfigurationEmitsSafeAreaInsetsChanges() = runTest {
    val layout = TreehouseWidgetView(context, throwingWidgetSystem)
    layout.hostConfiguration.test {
      assertEquals(HostConfiguration(safeAreaInsets = Margin.Zero), awaitItem())
      val insets = Insets.of(10, 20, 30, 40)
      val windowInsets = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.systemBars(), insets)
        .build()
      ViewCompat.dispatchApplyWindowInsets(layout, windowInsets)
      val density = context.resources.displayMetrics.density.toDouble()
      val expectedInsets = Margin(
        left = density * insets.left,
        right = density * insets.right,
        top = density * insets.top,
        bottom = density * insets.bottom,
      )
      assertEquals(HostConfiguration(safeAreaInsets = expectedInsets), awaitItem())
    }
  }

  private fun viewWidget(view: View) = object : Widget<View> {
    override val value: View get() = view
    override var layoutModifiers: LayoutModifier = LayoutModifier
  }

  private val throwingWidgetSystem =
    WidgetSystem { _, _ -> throw UnsupportedOperationException() }
}
