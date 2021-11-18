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
package app.cash.treehouse.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Updater
import androidx.compose.runtime.currentComposer
import app.cash.treehouse.protocol.ChildrenDiff
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootId
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the factory type [F] to our factory function so the correct widget can be created.
 */
@Composable
public inline fun <F, W> TreehouseComposeNode(
  crossinline factory: (F) -> W,
  update: @DisallowComposableCalls Updater<W>.() -> Unit,
  content: @Composable () -> Unit = {},
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
    val applier = currentComposer.applier as TreehouseApplier<F, W>
    currentComposer.createNode {
      factory(applier.factory)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<W>(currentComposer).update()
  content()

  currentComposer.endNode()
}

public interface TreehouseApplier<F, N> : Applier<N> {
  public val factory: F
}

/**
 * A synthetic node which allows the applier to differentiate between multiple groups of children.
 *
 * Compose's tree assumes each node only has single list of children. Or, put another way, even if
 * you apply multiple children Compose treats them as a single list of child nodes. In order to
 * differentiate between these children lists we introduce synthetic nodes. Every real node which
 * supports one or more groups of children will have one or more of these synthetic nodes as its
 * direct descendants. The nodes which are produced by each group of children will then become the
 * descendants of those synthetic nodes.
 *
 * This function is named weirdly to prevent normal usage since bad things will happen.
 *
 * @see ProtocolApplier
 * @suppress
 */
@Composable
public fun `$SyntheticChildren`(tag: Int, content: @Composable () -> Unit) {
  ComposeNode<ProtocolChildrenNode.Intermediate, Applier<ProtocolNode>>(
    factory = {
      ProtocolChildrenNode.Intermediate(tag)
    },
    update = {
    },
    content = content,
  )
}

/**
 * A node which exists in the tree to emulate supporting multiple children sets but which does not
 * appear directly in the protocol. The ID of these nodes mirrors that of its parent to simplify
 * creation of the protocol diffs. This is safe because these types never appears in the node map.
 */
private sealed class ProtocolChildrenNode(
  val tag: Int,
) : ProtocolNode(-1) {
  class Intermediate(tag: Int) : ProtocolChildrenNode(tag)
  class Root : ProtocolChildrenNode(RootChildrenTag) {
    init {
      id = RootId
    }
  }
}

/**
 * @suppress
 */
public open class ProtocolNode(
  public val type: Int,
) {
  public var id: Long = -1
    internal set

  @Suppress("PropertyName") // Avoiding potential collision with subtype properties.
  internal lateinit var _diffAppender: DiffAppender

  @Suppress("PropertyName") // Avoiding potential collision with subtype properties.
  internal val _children = mutableListOf<ProtocolNode>()

  public fun appendDiff(diff: PropertyDiff) {
    _diffAppender.append(diff)
  }

  public open fun sendEvent(event: Event) {
    throw IllegalStateException("Node ID $id of type $type does not handle events")
  }
}
/**
 * An [Applier] which records operations on the tree as models which can then be separately applied
 * by the display layer. Additionally, it has special handling for emulating nodes which contain
 * multiple children.
 *
 * Nodes in the tree are required to alternate between [ProtocolChildrenNode] instances and
 * non-[ProtocolChildrenNode] [ProtocolNode] subtypes starting from the root. This invariant is
 * maintained by virtue of the fact that all of the input `@Composables` should be generated code.
 *
 * For example, a node tree may look like this:
 * ```
 *                    Children(tag=1)
 *                     /          \
 *                    /            \
 *            ToolbarNode        ListNode
 *             /     \                 \
 *            /       \                 \
 * Children(tag=1)  Children(tag=2)   Children(tag=1)
 *        |              |               /       \
 *        |              |              /         \
 *   ButtonNode     ButtonNode     TextNode     TextNode
 * ```
 * But the protocol diff output would only record non-[ProtocolChildrenNode] nodes using their
 * [ProtocolChildrenNode.tag] value:
 * ```
 * Insert(id=<root-id>, tag=1, type=<toolbar-type>, childId=<toolbar-id>)
 * Insert(id=<toolbar-id>, tag=1, type=<button-type>, childId=..)
 * Insert(id=<toolbar-id>, tag=2, type=<button-type>, childId=..)
 * Insert(id=<root-id>, tag=1, type=<list-type>, childId=<list-id>)
 * Insert(id=<list-id>, tag=1, type=<text-type>, childId=..)
 * Insert(id=<list-id>, tag=1, type=<text-type>, childId=..)
 * ```
 */
internal class ProtocolApplier(
  override val factory: Any,
  private val diffAppender: DiffAppender,
) : AbstractApplier<ProtocolNode>(ProtocolChildrenNode.Root()),
  TreehouseApplier<Any, ProtocolNode> {
  private var nextId = RootId + 1
  internal val nodes = mutableMapOf(root.id to root)

  override fun onEndChanges() {
    diffAppender.trySend()
  }

  override fun insertTopDown(index: Int, instance: ProtocolNode) {
    current._children.add(index, instance)

    if (instance is ProtocolChildrenNode) {
      // Inherit the ID from the current node such that changes to the children can be reported
      // as if they occurred directly on the parent.
      instance.id = current.id
      // We do not add children instances to the map (they have no unique IDs and are only
      // available through indexing on the parent) and we do not send them over the wire to the
      // display (they are always implied by the display interfaces).
    } else {
      val current = current as ProtocolChildrenNode

      val id = nextId++
      instance.id = id
      instance._diffAppender = diffAppender

      nodes[id] = instance
      diffAppender.append(ChildrenDiff.Insert(current.id, current.tag, id, instance.type, index))
    }
  }

  override fun insertBottomUp(index: Int, instance: ProtocolNode) {
    // Ignored, we insert top-down for now.
  }

  override fun remove(index: Int, count: Int) {
    // Children instances are never removed from their parents.
    val current = current as ProtocolChildrenNode
    val children = current._children

    // TODO We should not have to track this and send it as part of the protocol.
    //  Ideally this would be entirely encapsulated on the display-side with additional bookkeeping.
    //  For now, we track it here and send it in the protocol as a simple solution.
    val removedIds = ArrayList<Long>(count)
    for (i in index until index + count) {
      removedIds.add(children[i].id)
    }

    nodes.keys.removeAll(removedIds)
    children.remove(index, count)
    diffAppender.append(ChildrenDiff.Remove(current.id, current.tag, index, count, removedIds))
  }

  override fun move(from: Int, to: Int, count: Int) {
    // Children instances are never moved within their parents.
    val current = current as ProtocolChildrenNode

    current._children.move(from, to, count)
    diffAppender.append(ChildrenDiff.Move(current.id, current.tag, from, to, count))
  }

  override fun onClear() {
    current._children.clear()
    nodes.clear()
    nodes[current.id] = current // Restore root node into map.
    diffAppender.append(ChildrenDiff.Clear)
  }
}
