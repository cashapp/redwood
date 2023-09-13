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
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.Updater
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

public interface RedwoodComposition {
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun <W : Any> RedwoodComposition(
  scope: CoroutineScope,
  view: RedwoodView<W>,
  provider: Widget.Provider<W>,
  onEndChanges: () -> Unit = {},
): RedwoodComposition {
  view.reset()

  return RedwoodComposition(scope, view.children, view.uiConfiguration, provider, onEndChanges)
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun <W : Any> RedwoodComposition(
  scope: CoroutineScope,
  container: Widget.Children<W>,
  uiConfigurations: StateFlow<UiConfiguration>,
  provider: Widget.Provider<W>,
  onEndChanges: () -> Unit = {},
): RedwoodComposition {
  return WidgetRedwoodComposition(scope, uiConfigurations, NodeApplier(provider, container, onEndChanges))
}

private class WidgetRedwoodComposition<W : Any>(
  private val scope: CoroutineScope,
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
      CompositionLocalProvider(LocalUiConfiguration provides uiConfiguration) {
        content()
      }
    }
  }

  override fun cancel() {
    snapshotHandle.dispose()
    snapshotJob?.cancel()
    recomposeJob.cancel()
    recomposer.cancel()
  }
}

/** @suppress For generated code usage only. */
@RedwoodCodegenApi
public interface RedwoodApplier<W : Any> {
  public val provider: Widget.Provider<W>
  public fun recordChanged(widget: Widget<W>)
}

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the provider type [P] to our factory function so the correct widget can be created.
 *
 * @suppress For generated code usage only.
 */
@Composable
@RedwoodCodegenApi
public inline fun <P : Widget.Provider<*>, W : Widget<*>> RedwoodComposeNode(
  crossinline factory: (P) -> W,
  update: @DisallowComposableCalls Updater<WidgetNode<W, *>>.() -> Unit,
  content: @Composable RedwoodComposeContent<W>.() -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    // Perform an explicit !! on the return value to avoid the Kotlin compiler inserting a huge
    // string into the output as an error message for an otherwise implicit null check.
    @Suppress(
      "UNCHECKED_CAST", // Safe so long as you use generated composition function.
      "UNNECESSARY_NOT_NULL_ASSERTION",
    )
    val applier = currentComposer.applier!! as RedwoodApplier<Any>

    currentComposer.createNode {
      @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
      WidgetNode(applier, factory(applier.provider as P) as Widget<Any>)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<WidgetNode<W, *>>(currentComposer).update()
  RedwoodComposeContent.Instance.content()

  currentComposer.endNode()
}

/**
 * @suppress For generated code usage only.
 */
@RedwoodCodegenApi
public class RedwoodComposeContent<out W : Widget<*>> {
  @Composable
  public fun into(
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

  public companion object {
    public val Instance: RedwoodComposeContent<Nothing> = RedwoodComposeContent()
  }
}
