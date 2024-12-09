/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ExplicitGroupsComposable
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.Updater
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.structuralEqualityPolicy
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.WidgetFactoryOwner
import app.cash.redwood.widget.WidgetSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

public interface RedwoodComposition {
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 * @param onChanges Invoked when changes are being applied to the widget tree. Multiple rounds of
 * changes can be applied in a single frame or setContent call, resulting in multiple invocations
 * of this callback.
 */
public fun <W : Any> RedwoodComposition(
  scope: CoroutineScope,
  view: RedwoodView<W>,
  widgetSystem: WidgetSystem<W>,
  onChanges: () -> Unit = {},
): RedwoodComposition {
  view.children.remove(0, view.children.widgets.size)

  val saveableStateRegistry = view.savedStateRegistry?.let { viewRegistry ->
    val state = viewRegistry.consumeRestoredState()
    val composeRegistry = SaveableStateRegistry(state) { value ->
      if (value is SnapshotMutableState<*>) {
        if (value.policy === neverEqualPolicy<Any?>() ||
          value.policy === structuralEqualityPolicy<Any?>() ||
          value.policy === referentialEqualityPolicy<Any?>()
        ) {
          val stateValue = value.value
          if (stateValue == null) true else viewRegistry.canBeSaved(stateValue)
        } else {
          false
        }
      } else {
        viewRegistry.canBeSaved(value)
      }
    }

    viewRegistry.registerSavedStateProvider(composeRegistry::performSave)
    scope.coroutineContext.job.invokeOnCompletion {
      viewRegistry.unregisterSavedStateProvider()
    }

    composeRegistry
  }

  return RedwoodComposition(
    scope,
    view.children,
    view.onBackPressedDispatcher,
    saveableStateRegistry,
    view.uiConfiguration,
    widgetSystem,
    onChanges,
  )
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 * @param onChanges Invoked when changes are being applied to the widget tree. Multiple rounds of
 * changes can be applied in a single frame or setContent call, resulting in multiple invocations
 * of this callback.
 */
public fun <W : Any> RedwoodComposition(
  scope: CoroutineScope,
  container: Widget.Children<W>,
  onBackPressedDispatcher: OnBackPressedDispatcher,
  saveableStateRegistry: SaveableStateRegistry?,
  uiConfigurations: StateFlow<UiConfiguration>,
  widgetSystem: WidgetSystem<W>,
  onChanges: () -> Unit = {},
): RedwoodComposition {
  return WidgetRedwoodComposition(
    scope,
    onBackPressedDispatcher,
    saveableStateRegistry,
    uiConfigurations,
    NodeApplier(widgetSystem, container, onChanges),
  )
}

private class WidgetRedwoodComposition<W : Any>(
  private val scope: CoroutineScope,
  private val onBackPressedDispatcher: OnBackPressedDispatcher,
  private val savedStateRegistry: SaveableStateRegistry?,
  private val uiConfigurations: StateFlow<UiConfiguration>,
  applier: NodeApplier<W>,
) : RedwoodComposition {
  private val recomposer = Recomposer(scope.coroutineContext)
  private val composition = Composition(applier, recomposer)

  private var snapshotJob: Job? = null
  private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
    // Set up a trigger to apply changes on the next frame if a global write was observed.
    if (snapshotJob == null) {
      snapshotJob = scope.launch {
        snapshotJob = null
        Snapshot.sendApplyNotifications()
      }
    }
  }

  // These launch undispatched so that they reach their first suspension points before returning
  // control to the caller.
  private val recomposeJob = scope.launch(start = UNDISPATCHED) {
    recomposer.runRecomposeAndApplyChanges()
  }

  override fun setContent(content: @Composable () -> Unit) {
    composition.setContent {
      val uiConfiguration by uiConfigurations.collectAsState()
      CompositionLocalProvider(
        LocalOnBackPressedDispatcher provides onBackPressedDispatcher,
        LocalSaveableStateRegistry provides savedStateRegistry,
        LocalUiConfiguration provides uiConfiguration,
      ) {
        content()
      }
    }
  }

  override fun cancel() {
    composition.dispose()
    snapshotHandle.dispose()
    snapshotJob?.cancel()
    recomposeJob.cancel()
    recomposer.cancel()
  }
}

/** @suppress For generated code usage only. */
@RedwoodCodegenApi
public interface RedwoodApplier<W : Any> {
  public val widgetSystem: WidgetSystem<W>
  public fun recordChanged(widget: Widget<W>)
}

@PublishedApi
@RedwoodCodegenApi
internal expect inline fun <V : Any> Composer.redwoodApplier(): RedwoodApplier<V>

@PublishedApi
@RedwoodCodegenApi
internal expect inline fun <O : WidgetFactoryOwner<V>, V : Any> RedwoodApplier<V>.widgetSystem(): O

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the owner type [O] to our factory function so the correct widget can be created.
 *
 * @suppress For generated code usage only.
 */
@Composable
@RedwoodCodegenApi
@ExplicitGroupsComposable
public inline fun <O : WidgetFactoryOwner<V>, W : Widget<V>, V : Any> RedwoodComposeNode(
  crossinline factory: (O) -> W,
  update: @DisallowComposableCalls Updater<WidgetNode<W, V>>.() -> Unit,
  content: @Composable RedwoodComposeContent<W>.() -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    val applier = currentComposer.redwoodApplier<V>()
    currentComposer.createNode {
      WidgetNode(applier, factory(applier.widgetSystem()))
    }
  } else {
    currentComposer.useNode()
  }

  Updater<WidgetNode<W, V>>(currentComposer).update()
  RedwoodComposeContent.Instance.content()

  currentComposer.endNode()
}

/**
 * @suppress For generated code usage only.
 */
@RedwoodCodegenApi
public class RedwoodComposeContent<out W : Widget<*>> {
  @Composable
  public fun Children(
    accessor: (W) -> Widget.Children<*>,
    content: @Composable () -> Unit,
  ) {
    ComposeNode<ChildrenNode<*>, Applier<*>>(
      factory = {
        @Suppress("UNCHECKED_CAST")
        ChildrenNode(accessor as (Widget<Any>) -> Widget.Children<Any>)
      },
      update = {},
      content = content,
    )
  }

  @RedwoodCodegenApi // https://github.com/Kotlin/binary-compatibility-validator/issues/91
  public companion object {
    public val Instance: RedwoodComposeContent<Nothing> = RedwoodComposeContent()
  }
}
