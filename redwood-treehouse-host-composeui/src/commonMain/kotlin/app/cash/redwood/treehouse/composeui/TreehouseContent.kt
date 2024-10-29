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
package app.cash.redwood.treehouse.composeui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.cash.redwood.composeui.safeAreaInsets
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.DynamicContentWidgetFactory
import app.cash.redwood.treehouse.StateSnapshot
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection as RedwoodLayoutDirection
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp as redwoodDp
import app.cash.redwood.widget.SavedStateRegistry
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
public fun <A : AppService> TreehouseContent(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem<@Composable () -> Unit>,
  contentSource: TreehouseContentSource<A>,
  modifier: Modifier = Modifier,
  dynamicContentWidgetFactory: DynamicContentWidgetFactory<@Composable () -> Unit> =
    EmptyDynamicContentWidgetFactory,
) {
  val onBackPressedDispatcher = platformOnBackPressedDispatcher()

  var viewportSize: Size? by remember { mutableStateOf(null) }
  val density = LocalDensity.current
  val uiConfiguration = UiConfiguration(
    darkMode = isSystemInDarkTheme(),
    safeAreaInsets = safeAreaInsets(),
    viewportSize = viewportSize,
    density = density.density.toDouble(),
    layoutDirection = when (LocalLayoutDirection.current) {
      LayoutDirection.Ltr -> RedwoodLayoutDirection.Ltr
      LayoutDirection.Rtl -> RedwoodLayoutDirection.Rtl
    },
  )
  val treehouseView = remember(widgetSystem) {
    object : TreehouseView<@Composable () -> Unit> {
      override val children: ComposeWidgetChildren = ComposeWidgetChildren()

      override val value: @Composable () -> Unit = {
        children.Render()
      }

      override val dynamicContentWidgetFactory = dynamicContentWidgetFactory
      override val onBackPressedDispatcher = onBackPressedDispatcher
      override val uiConfiguration = MutableStateFlow(uiConfiguration)

      // TODO TreehouseView is a weird type and shouldn't extend from RedwoodView. The concept
      //  of this registry shouldn't exist for Treehouse / should be auto-wired via RedwoodContent.
      override val savedStateRegistry: SavedStateRegistry? get() = null
      override val widgetSystem = widgetSystem
      override val readyForContent = true
      override var readyForContentChangeListener: ReadyForContentChangeListener<@Composable () -> Unit>? = null
      override var saveCallback: TreehouseView.SaveCallback? = null
      override val stateSnapshotId = StateSnapshot.Id(null)
    }
  }
  LaunchedEffect(treehouseView, uiConfiguration) {
    treehouseView.uiConfiguration.value = uiConfiguration
  }
  DisposableEffect(treehouseView, contentSource) {
    val closeable = contentSource.bindWhenReady(treehouseView, treehouseApp)
    onDispose {
      closeable.close()
    }
  }

  Box(
    modifier = modifier.onSizeChanged { size ->
      viewportSize = with(Density(density.density.toDouble())) {
        Size(size.width.toDp().value.redwoodDp, size.height.toDp().value.redwoodDp)
      }
    },
  ) {
    treehouseView.value()
  }
}

@Composable
internal expect fun platformOnBackPressedDispatcher(): OnBackPressedDispatcher
