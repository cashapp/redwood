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

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composition
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.Updater
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public interface RedwoodComposition {
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun <T : Any> RedwoodComposition(
  scope: CoroutineScope,
  container: Widget.Children<T>,
  factory: Widget.Factory<T>,
): RedwoodComposition {
  return RedwoodComposition(scope, WidgetApplier(factory, container))
}

public fun <T : Any> RedwoodComposition(
  scope: CoroutineScope,
  applier: WidgetApplier<T>,
) : RedwoodComposition {
  return WidgetRedwoodComposition(scope, applier)
}

private class WidgetRedwoodComposition(
  private val scope: CoroutineScope,
  applier: WidgetApplier<*>,
) : RedwoodComposition {
  private val recomposer = Recomposer(scope.coroutineContext)

  private var applyScheduled = false
  private var snapshotJob: Job? = null

  // Set up a trigger to apply changes on the next frame if a global write was observed.
  private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
    if (!applyScheduled) {
      applyScheduled = true
      snapshotJob = scope.launch {
        applyScheduled = false
        Snapshot.sendApplyNotifications()
      }
    }
  }

  // Launch undispatched so we reach the first suspension point before returning control.
  private val recomposeJob = scope.launch(start = UNDISPATCHED) {
    recomposer.runRecomposeAndApplyChanges()
  }

  private val composition = Composition(applier, recomposer)

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

/**
 * Nodes in the tree are required to alternate between [ChildrenWidget] instances and
 * regular [Widget] subtypes starting from the root. This invariant is maintained by
 * virtue of the fact that all of the input `@Composables` should be generated code.
 *
 * For example, a node tree may look like this:
 * ```
 *                    Children(tag=1)
 *                     /          \
 *                    /            \
 *            ToolbarNode        ListNode
 *             ·     ·                 ·
 *            ·       ·                 ·
 * Children(tag=1)  Children(tag=2)   Children(tag=1)
 *        |              |               /       \
 *        |              |              /         \
 *   ButtonNode     ButtonNode     TextNode     TextNode
 * ```
 * The tree produced by this applier is not a real tree. We do not maintain any relationship from
 * the user nodes to the synthetic children nodes as they can never be individually moved/removed.
 * The hierarchy is maintained by Compose's slot table and is visualized above by dotted lines.
 */
public class WidgetApplier<T : Any>(
  public val factory: Widget.Factory<T>,
  rootChildren: Widget.Children<T>,
  private val onEndChanges: () -> Unit = {},
) : AbstractApplier<Widget<T>>(ChildrenWidget(rootChildren)) {
  private var closed = false

  override fun onEndChanges() {
    onEndChanges.invoke()
  }

  override fun insertTopDown(index: Int, instance: Widget<T>) {
    check(!closed)

    if (instance is ChildrenWidget) {
      instance.children = instance.accessor!!.invoke(current)
      instance.accessor = null
    } else {
      val current = current as ChildrenWidget
      current.children!!.insert(index, instance)
    }
  }

  override fun insertBottomUp(index: Int, instance: Widget<T>) {
    // Ignored, we insert top-down for now.
  }

  override fun remove(index: Int, count: Int) {
    check(!closed)

    // Children instances are never removed from their parents.
    val current = current as ChildrenWidget
    current.children!!.remove(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    check(!closed)

    // Children instances are never moved within their parents.
    val current = current as ChildrenWidget
    current.children!!.move(from, to, count)
  }

  override fun onClear() {
    check(!closed)
    closed = true
  }
}

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the factory type [F] to our factory function so the correct widget can be created.
 *
 * @suppress For generated code usage only.
 */
@Composable
@Suppress("FunctionName") // Hiding from auto-complete.
public inline fun <F : Widget.Factory<*>, W : Widget<*>> _RedwoodComposeNode(
  crossinline factory: (F) -> W,
  update: @DisallowComposableCalls Updater<W>.() -> Unit,
  content: @Composable _RedwoodComposeContent<W>.() -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
    val applier = currentComposer.applier as WidgetApplier<F>
    currentComposer.createNode {
      @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
      factory(applier.factory as F)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<W>(currentComposer).update()
  _RedwoodComposeContent.Instance.content()

  currentComposer.endNode()
}

/**
 * @suppress For generated code usage only.
 */
@Suppress("ClassName") // Hiding from auto-complete.
public class _RedwoodComposeContent<out W : Widget<*>> {
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
    public val Instance: _RedwoodComposeContent<Nothing> = _RedwoodComposeContent()
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
 *
 * @suppress For generated code usage only.
 */
private class ChildrenWidget<T : Any> private constructor(
  var accessor: ((Widget<T>) -> Widget.Children<T>)?,
  var children: Widget.Children<T>?,
) : Widget<T> {
  constructor(accessor: (Widget<T>) -> Widget.Children<T>) : this(accessor, null)
  constructor(children: Widget.Children<T>) : this(null, children)

  override val value: Nothing get() = throw AssertionError()
  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(_) { throw AssertionError() }
}
