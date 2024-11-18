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

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.Widget
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class TreehouseLayoutTest {
  private val activity = Robolectric.buildActivity(ComponentActivity::class.java).resume().visible().get()

  @Test fun widgetsAddChildViews() {
    val layout = TreehouseLayout(activity, throwingWidgetSystem, activity.onBackPressedDispatcher)
    val rootView = layout.value as ViewGroup

    val view = View(activity)
    layout.children.insert(0, viewWidget(view))
    assertThat(rootView.childCount).isEqualTo(1)
    assertThat(rootView.getChildAt(0)).isSameInstanceAs(view)
  }

  @Test fun attachAndDetachSendsStateChange() {
    val parent = activity.findViewById<ViewGroup>(android.R.id.content)
    val layout = TreehouseLayout(activity, throwingWidgetSystem, activity.onBackPressedDispatcher)
    val listener = CountingReadyForContentChangeListener<View>()

    layout.readyForContentChangeListener = listener
    assertThat(listener.count).isEqualTo(0)

    parent.addView(layout)
    assertThat(listener.count).isEqualTo(1)

    parent.removeView(layout)
    assertThat(listener.count).isEqualTo(2)
  }

  @Test fun uiConfigurationReflectsInitialUiMode() {
    val newConfig = Configuration(activity.resources.configuration)
    newConfig.uiMode = (newConfig.uiMode and UI_MODE_NIGHT_MASK.inv()) or UI_MODE_NIGHT_YES
    val newContext = activity.createConfigurationContext(newConfig) // Needs API 26.
    val layout = TreehouseLayout(newContext, throwingWidgetSystem, activity.onBackPressedDispatcher)
    assertThat(layout.uiConfiguration.value.darkMode).isTrue()
  }

  @Test fun uiConfigurationEmitsUiModeChanges() = runTest {
    val layout = TreehouseLayout(activity, throwingWidgetSystem, activity.onBackPressedDispatcher)
    layout.uiConfiguration.test {
      assertThat(awaitItem().darkMode).isFalse()

      val newConfig = Configuration(activity.resources.configuration)
      newConfig.uiMode = (newConfig.uiMode and UI_MODE_NIGHT_MASK.inv()) or UI_MODE_NIGHT_YES

      layout.dispatchConfigurationChanged(newConfig)
      assertThat(awaitItem().darkMode).isTrue()
    }
  }

  @Test fun uiConfigurationEmitsSystemBarsSafeAreaInsetsChanges() = runTest {
    val layout = TreehouseLayout(activity, throwingWidgetSystem, activity.onBackPressedDispatcher)
    layout.uiConfiguration.test {
      val value1 = awaitItem()
      assertThat(value1.safeAreaInsets).isEqualTo(Margin.Zero)
      assertThat(value1.viewInsets).isEqualTo(Margin.Zero)

      val insets = Insets.of(10, 20, 30, 40)
      val windowInsets = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.systemBars(), insets)
        .build()
      ViewCompat.dispatchApplyWindowInsets(layout, windowInsets)
      val expectedInsets = with(Density(activity.resources)) {
        Margin(
          start = insets.left.toDp(),
          end = insets.right.toDp(),
          top = insets.top.toDp(),
          bottom = insets.bottom.toDp(),
        )
      }
      val value2 = awaitItem()
      assertThat(value2.safeAreaInsets).isEqualTo(Margin.Zero)
      assertThat(value2.viewInsets).isEqualTo(expectedInsets)
    }
  }

  @Test fun uiConfigurationEmitsLayoutDirectionChanges() = runTest {
    val layout = TreehouseLayout(activity, throwingWidgetSystem, activity.onBackPressedDispatcher)
    layout.uiConfiguration.test {
      assertThat(awaitItem().layoutDirection).isEqualTo(LayoutDirection.Ltr)

      val newConfig = Configuration(activity.resources.configuration)
      newConfig.setLayoutDirection(Locale("he")) // Hebrew is RTL

      layout.dispatchConfigurationChanged(newConfig)
      assertThat(awaitItem().layoutDirection).isEqualTo(LayoutDirection.Rtl)
    }
  }

  private fun viewWidget(view: View) = object : Widget<View> {
    override val value: View get() = view
    override var modifier: Modifier = Modifier
  }

  private val throwingWidgetSystem =
    WidgetSystem<View> { _, _ -> throw UnsupportedOperationException() }
}
