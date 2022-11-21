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
package app.cash.redwood.protocol.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import app.cash.redwood.compose._ChildrenWidget
import app.cash.redwood.compose._RedwoodApplier
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget

/**
 * An [Applier] which records operations on the tree as models which can then be separately applied
 * by the display layer. Additionally, it has special handling for emulating nodes which contain
 * multiple children.
 *
 * Nodes in the tree are required to alternate between [_ChildrenWidget] instances and
 * [AbstractDiffProducingWidget] subtypes starting from the root. This invariant is maintained by
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
 * But the protocol diff output would only record [AbstractDiffProducingWidget] nodes using
 * their [AbstractDiffProducingWidget.id] and [AbstractDiffProducingWidget.type] value:
 * ```
 * Insert(id=<root-id>, tag=1, type=<toolbar-type>, childId=<toolbar-id>)
 * Insert(id=<toolbar-id>, tag=1, type=<button-type>, childId=..)
 * Insert(id=<toolbar-id>, tag=2, type=<button-type>, childId=..)
 * Insert(id=<root-id>, tag=1, type=<list-type>, childId=<list-id>)
 * Insert(id=<list-id>, tag=1, type=<text-type>, childId=..)
 * Insert(id=<list-id>, tag=1, type=<text-type>, childId=..)
 * ```
 * The tree produced by this applier is not a real tree. We do not maintain any relationship from
 * the user nodes to the synthetic children nodes as they can never be individually moved/removed.
 * The hierarchy is maintained by Compose's slot table and is represented by dotted lines.
 */
internal class ProtocolApplier(
  override val factory: DiffProducingWidget.Factory,
  private val diffAppender: DiffAppender,
) : _RedwoodApplier<DiffProducingWidget.Factory>, AbstractApplier<Widget<Nothing>>(
  root = _ChildrenWidget(
    DiffProducingWidgetChildren(Id.Root, RootChildrenTag, diffAppender),
  ),
) {
  internal val nodes = mutableMapOf(Id.Root to root)
  private var closed = false

  override fun onEndChanges() {
    check(!closed)

    diffAppender.trySend()
  }

  override fun insertTopDown(index: Int, instance: Widget<Nothing>) {
    check(!closed)

    if (instance is _ChildrenWidget) {
      instance.children = instance.accessor!!.invoke(current)
      instance.accessor = null
    } else {
      instance as AbstractDiffProducingWidget
      instance._diffAppender = diffAppender

      nodes[instance.id] = instance

      val current = current as _ChildrenWidget
      current.children!!.insert(index, instance)
    }
  }

  override fun insertBottomUp(index: Int, instance: Widget<Nothing>) {
    // Ignored, we insert top-down for now.
  }

  override fun remove(index: Int, count: Int) {
    check(!closed)

    // Children instances are never removed from their parents.
    val current = current as _ChildrenWidget
    val children = current.children as DiffProducingWidgetChildren

    for (i in index until index + count) {
      nodes.remove(children.ids[i])
    }
    children.remove(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    check(!closed)

    // Children instances are never moved within their parents.
    val current = current as _ChildrenWidget
    current.children!!.move(from, to, count)
  }

  override fun onClear() {
    closed = true
  }
}
