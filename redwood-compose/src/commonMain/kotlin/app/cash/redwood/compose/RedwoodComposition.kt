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
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.Updater
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.redwood.LayoutModifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Render a Redwood composition inside of a different composition such as Compose UI.
 */
@Composable
public fun RedwoodContent(
  provider: Widget.Provider<@Composable () -> Unit>,
  content: @Composable () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val children = remember(provider, content) { ComposeWidgetChildren() }
  LaunchedEffect(provider, content) {
    val composition = RedwoodComposition(
      scope = scope,
      container = children,
      provider = provider,
    )
    composition.setContent(content)
  }
  children.render()
}

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
  container: Widget.Children<W>,
  provider: Widget.Provider<W>,
  onEndChanges: () -> Unit = {},
): RedwoodComposition {
  return WidgetRedwoodComposition(scope, WidgetApplier(provider, container, onEndChanges))
}

private class WidgetRedwoodComposition(
  private val scope: CoroutineScope,
  applier: WidgetApplier<*>,
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
    composition.setContent(content)
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
  update: @DisallowComposableCalls Updater<W>.() -> Unit,
  content: @Composable RedwoodComposeContent<W>.() -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
    val applier = currentComposer.applier as RedwoodApplier<P>
    currentComposer.createNode {
      @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
      factory(applier.provider as P)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<W>(currentComposer).update()
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
    ComposeNode<ChildrenWidget<*>, Applier<*>>(
      factory = {
        @Suppress("UNCHECKED_CAST")
        ChildrenWidget(accessor as (Widget<Any>) -> Widget.Children<Any>)
      },
      update = {},
      content = content,
    )
  }

  public companion object {
    public val Instance: RedwoodComposeContent<Nothing> = RedwoodComposeContent()
  }
}

/**
 * A synthetic widget which allows the applier to differentiate between multiple groups of children.
 *
 * Compose's tree assumes each node only has single list of children. Or, put another way, even if
 * you apply multiple children Compose treats them as a single list of child nodes. In order to
 * differentiate between these children lists we introduce synthetic nodes. Every real node which
 * supports one or more groups of children will have one or more of these synthetic nodes as its
 * direct descendants. The nodes which are produced by each group of children will then become the
 * descendants of those synthetic nodes.
 */
internal class ChildrenWidget<W : Any> private constructor(
  var accessor: ((Widget<W>) -> Widget.Children<W>)?,
  var children: Widget.Children<W>?,
) : Widget<W> {
  constructor(accessor: (Widget<W>) -> Widget.Children<W>) : this(accessor, null)
  constructor(children: Widget.Children<W>) : this(null, children)

  override val value: Nothing get() = throw AssertionError()
  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(_) { throw AssertionError() }
}
