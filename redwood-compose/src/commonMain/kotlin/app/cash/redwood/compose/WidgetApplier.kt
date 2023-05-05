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
import app.cash.redwood.LayoutModifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.Widget

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
 * Similarly, no relationship is maintained from a [ChildrenNode] to their [WidgetNode]s. Instead,
 * the [WidgetNode.widget] is what's added to the parent [ChildrenNode.children].
 *
 * Compose maintains the tree structure internally. All non-insert operations are performed
 * using indexes and counts rather than references which are forwarded to [ChildrenNode.children].
 */
@OptIn(RedwoodCodegenApi::class)
internal class NodeApplier<W : Any>(
  override val provider: Widget.Provider<W>,
  root: Widget.Children<W>,
  private val onEndChanges: () -> Unit,
) : AbstractApplier<Node<W>>(ChildrenNode(root)), RedwoodApplier<W> {
  private var closed = false

  override fun onEndChanges() {
    check(!closed)

    onEndChanges.invoke()
  }

  override fun insertTopDown(index: Int, instance: Node<W>) {
    check(!closed)

    if (instance is ChildrenNode) {
      @Suppress("UNCHECKED_CAST") // Guaranteed by generated code.
      val widgetNode = current as WidgetNode<Widget<W>, W>
      instance.attachTo(widgetNode.widget)
    } else {
      @Suppress("UNCHECKED_CAST") // Guaranteed by generated code.
      val widgetNode = instance as WidgetNode<Widget<W>, W>
      val children = (current as ChildrenNode<W>).children

      widgetNode.container = children
      children.insert(index, widgetNode.widget)
    }
  }

  override fun insertBottomUp(index: Int, instance: Node<W>) {
    // Ignored, we insert top-down.
  }

  override fun remove(index: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenNode
    current.children.remove(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenNode
    current.children.move(from, to, count)
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
public class WidgetNode<W : Widget<V>, V : Any>(
  public val widget: W,
) : Node<W> {
  public var container: Widget.Children<V>? = null

  public companion object {
    public val SetLayoutModifiers: WidgetNode<*, *>.(LayoutModifier) -> Unit = {
      widget.layoutModifiers = it
      container?.onLayoutModifierUpdated()
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
  private var _children: Widget.Children<W>?,
) : Node<W> {
  constructor(accessor: (Widget<W>) -> Widget.Children<W>) : this(accessor, null)
  constructor(children: Widget.Children<W>) : this(null, children)

  val children: Widget.Children<W> get() = _children!!

  fun attachTo(parent: Widget<W>) {
    _children = checkNotNull(accessor).invoke(parent)
    accessor = null
  }
}
