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
import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.WidgetSystem
import kotlin.math.max
import kotlin.math.min

/**
 * An [Applier] for Redwood's tree of nodes.
 *
 * This applier has special handling for emulating widgets which contain multiple children.
 * Nodes in the tree are required to alternate between [WidgetNode] and [ChildrenNode] starting
 * from the root. This invariant is maintained by virtue of the fact that all of the input
 * `@Composable`s should bottom out in generated Redwood code.
 *
 * For example, a widget tree may look like this:
 * ```
 *                        ChildrenNode(root)
 *                          /           \
 *                         /             \
 *          WidgetNode<Toolbar>       WidgetNode<List>
 *             /          \                      \
 *            /            \                      \
 * ChildrenNode(tag=1)  ChildrenNode(tag=2)     ChildrenNode(tag=1)
 *        |                   |                      /       \
 *        |                   |                     /         \
 * WidgetNode<Button>  WidgetNode<Button>  WidgetNode<Text>  WidgetNode<Text>
 * ```
 * The node tree produced by this applier is not actually a tree. We do not maintain a relationship
 * from each [WidgetNode] to their [ChildrenNode]s as they can never be individually moved/removed.
 * Compose maintains that relationship internally to support positioning the applier.
 */
@OptIn(RedwoodCodegenApi::class)
internal class NodeApplier<W : Any>(
  override val widgetSystem: WidgetSystem<W>,
  root: Widget.Children<W>,
  private val onChanges: () -> Unit,
) : AbstractApplier<Node<W>>(ChildrenNode(root)),
  RedwoodApplier<W> {
  private var closed = false
  private val changedWidgets = platformSetOf<ChangeListener>()

  override fun recordChanged(widget: Widget<W>) {
    if (widget is ChangeListener) {
      changedWidgets += widget
    }
  }

  override fun onBeginChanges() {
    super.onBeginChanges()
    // We invoke this here rather than in the end change callback to try and ensure
    // no one relies on it to signal the end of changes.
    onChanges.invoke()
  }

  override fun onEndChanges() {
    check(!closed)

    changedWidgets.let { changedWidgets ->
      changedWidgets.forEach { changedWidget ->
        changedWidget.onEndChanges()
      }
      changedWidgets.clear()
    }
  }

  override fun insertTopDown(index: Int, instance: Node<W>) {
    check(!closed)

    // If this is a synthetic children node, immediately attach it to its widget node parent when
    // traversing down the tree. This ensures we can add widget nodes to children nodes in
    // bottom-up order.
    if (instance is ChildrenNode) {
      val widgetNode = current as WidgetNode<Widget<W>, W>
      instance.attachTo(widgetNode.widget)
    }
  }

  override fun insertBottomUp(index: Int, instance: Node<W>) {
    check(!closed)

    // We only attach widgets to their parent node's children when traversing back up the tree.
    // This ensures that the initial properties of the widget have been set before it is attached.
    if (instance is WidgetNode<*, *>) {
      val widgetNode = instance as WidgetNode<Widget<W>, W>
      val current = current as ChildrenNode<W>

      current.insert(index, widgetNode)
      current.parent?.let(::recordChanged)
    }
  }

  override fun remove(index: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenNode
    current.remove(index, count)
    current.parent?.let(::recordChanged)
  }

  override fun move(from: Int, to: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenNode
    current.move(from, to, count)
    current.parent?.let(::recordChanged)
  }

  override fun onClear() {
    closed = true
  }
}

/** @suppress For generated code usage only. */
@RedwoodCodegenApi
public sealed interface Node<W : Any>

/**
 * A node which wraps a [Widget] and also holds onto the [Widget.Children] in which the widget
 * is placed. The generics of this class are a little funky in order to avoid casts of the widget
 * in the generated composables.
 *
 * @suppress For generated code usage only.
 */
@RedwoodCodegenApi
public class WidgetNode<out W : Widget<V>, V : Any>(
  private val applier: RedwoodApplier<V>,
  public val widget: W,
) : Node<V> {
  public fun recordChanged() {
    applier.recordChanged(widget)
  }

  public var container: Widget.Children<V>? = null

  /** The index of [widget] within its parent [container] when attached. */
  public var index: Int = -1

  @RedwoodCodegenApi // https://github.com/Kotlin/binary-compatibility-validator/issues/91
  public companion object {
    public val SetModifiers: WidgetNode<Widget<Any>, Any>.(Modifier) -> Unit = {
      recordChanged()

      it.forEachUnscoped { element ->
        applier.widgetSystem.apply(widget.value, element)
      }

      widget.modifier = it
      container?.onModifierUpdated(index, widget)
    }
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
 * This node has two valid states:
 * 1. Non-null accessor and null children. This is the state when created but not inserted in
 *    the node tree.
 * 2. Null accessor and non-null children. Once inserted into the tree, the accessor is used to
 *    fetch the appropriate children reference from the parent widget.
 *
 * Transition from 1 to 2 by calling [attachTo]. This may only be done once, and you
 * cannot transition back to 1 afterwards.
 */
@RedwoodCodegenApi
internal class ChildrenNode<W : Any> private constructor(
  private var accessor: ((Widget<W>) -> Widget.Children<W>)?,
  parent: Widget<W>?,
  private var _children: Widget.Children<W>?,
) : Node<W> {
  constructor(accessor: (Widget<W>) -> Widget.Children<W>) : this(accessor, null, null)
  constructor(children: Widget.Children<W>) : this(null, null, children)

  private val nodes = platformListOf<WidgetNode<Widget<W>, W>>()

  fun insert(index: Int, node: WidgetNode<Widget<W>, W>) {
    nodes.let { nodes ->
      // Bump the index of any nodes which will be shifted.
      for (i in index until nodes.size) {
        nodes[i].index++
      }

      node.index = index
      nodes.add(index, node)
    }

    children.let { children ->
      node.container = children
      children.insert(index, node.widget)
    }
  }

  fun remove(index: Int, count: Int) {
    nodes.let { nodes ->
      nodes.remove(index, count)

      // Drop the index of any nodes shifted after the removal.
      for (i in index until nodes.size) {
        nodes[i].index -= count
      }
    }

    children.remove(index, count)
  }

  fun move(from: Int, to: Int, count: Int) {
    nodes.let { nodes ->
      nodes.move(from, to, count)

      // If moving up, lower bound is from. If moving down, lower bound is to.
      val lowerBound = min(from, to)
      // If moving up, upper bound is to, If moving down, upper bound is from + count.
      val upperBound = max(to, from + count)
      for (i in lowerBound until upperBound) {
        nodes[i].index = i
      }
    }

    children.move(from, to, count)
  }

  /** The parent of this children group. Null when the root children instance. */
  var parent: Widget<W>? = parent
    get() {
      checkNotNull(_children) { "Not attached" }
      return field
    }
    private set

  private val children: Widget.Children<W> get() = checkNotNull(_children) { "Not attached" }

  fun attachTo(parent: Widget<W>) {
    _children = checkNotNull(accessor).invoke(parent)
    this.parent = parent
    accessor = null
  }
}
