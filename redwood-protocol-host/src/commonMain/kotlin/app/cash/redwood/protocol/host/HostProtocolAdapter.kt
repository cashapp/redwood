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
package app.cash.redwood.protocol.host

import androidx.collection.MutableIntObjectMap
import androidx.collection.mutableIntObjectMapOf
import androidx.collection.mutableScatterSetOf
import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Move
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import kotlin.native.ObjCName

/**
 * Runs the host side of the protocol.
 *
 * This type receives [Change]s from the guest and applies them to the widget tree as children of
 * the provided container.
 *
 * It sends events from widgets to the guest via [eventSink].
 */
@OptIn(RedwoodCodegenApi::class)
@ObjCName("HostProtocolAdapter", exact = true)
public class HostProtocolAdapter<W : Any>(
  @Suppress("UNUSED_PARAMETER")
  guestVersion: RedwoodVersion,
  container: Widget.Children<W>,
  factory: ProtocolFactory<W>,
  private val eventSink: UiEventSink,
  private val leakDetector: LeakDetector,
) : ChangesSink {
  private val factory = when (factory) {
    is GeneratedHostProtocol -> factory
  }

  // TODO Use MutableIntObjectMap once https://issuetracker.google.com/issues/378077858 is fixed.
  private val nodes =
    mutableMapOf<Int, ProtocolNode<W>>(Id.Root.value to RootProtocolNode(container))

  private val removeNodeById = IdVisitor { nodes.remove(it.value) }

  private val changedWidgets = mutableScatterSetOf<ChangeListener>()

  /** Nodes available for reuse. */
  private val pool = ArrayDeque<ProtocolNode<W>>()

  private var closed = false

  override fun sendChanges(changes: List<Change>) {
    check(!closed)

    @Suppress("NAME_SHADOWING")
    val changes = applyReuse(changes)

    for (i in changes.indices) {
      val change = changes[i]
      val id = change.id
      when (change) {
        is Create -> {
          val widgetProtocol = factory.widget(change.tag) ?: continue
          val node = widgetProtocol.createNode(id)
          val old = nodes.put(change.id.value, node)
          require(old == null) {
            "Insert attempted to replace existing widget with ID ${change.id.value}"
          }
        }

        is ChildrenChange -> {
          val node = node(id)
          val children = node.children(change.tag) ?: continue
          when (change) {
            is Add -> {
              val child = node(change.childId)
              children.insert(change.index, child)
            }

            is Move -> {
              children.move(change.fromIndex, change.toIndex, change.count)
            }

            is Remove -> {
              if (!change.detach) {
                for (childIndex in change.index until change.index + change.count) {
                  val child = children.nodes[childIndex]
                  child.visitIds(removeNodeById)
                  poolOrDetach(child)
                }
              }
              children.remove(change.index, change.count)
            }
          }

          val widget = node.widget
          if (widget is ChangeListener) {
            changedWidgets += widget
          }
        }

        is ModifierChange -> {
          val node = node(id)

          val modifier = change.elements.fold<_, Modifier>(Modifier) { outer, element ->
            val value = node.widget.value
            val inner = factory.createModifier(element)
            if (element.tag.value == REUSE_MODIFIER_TAG) {
              node.reuse = true
            }
            if (inner is Modifier.UnscopedElement) {
              factory.widgetSystem.apply(value, inner)
            }
            outer.then(inner)
          }
          node.updateModifier(modifier)

          val widget = node.widget
          if (widget is ChangeListener) {
            changedWidgets += widget
          }
        }

        is PropertyChange -> {
          val node = node(change.id)
          node.apply(change, eventSink)

          val widget = node.widget
          if (widget is ChangeListener) {
            changedWidgets += widget
          }
        }
      }
    }

    if (changedWidgets.isNotEmpty()) {
      changedWidgets.forEach { widget ->
        widget.onEndChanges()
      }
      changedWidgets.clear()
    }
  }

  internal fun node(id: Id): ProtocolNode<W> {
    return checkNotNull(nodes[id.value]) { "Unknown widget ID ${id.value}" }
  }

  /**
   * Proactively clear held widgets. (This avoids problems when mixing garbage-collected Kotlin
   * objects with reference-counted Swift objects.)
   */
  public fun close() {
    closed = true

    nodes.values.forEach { node ->
      node.detach()
    }
    nodes.clear()

    for (node in pool) {
      node.detach()
    }
    pool.clear()
  }

  private fun poolOrDetach(removedNode: ProtocolNode<W>) {
    if (removedNode.reuse) {
      removedNode.shapeHash = shapeHash(this.factory, removedNode)
      pool.addFirst(removedNode)
      if (pool.size > POOL_SIZE) {
        val evicted = pool.removeLast() // Evict the least-recently added element.
        watchForLeaksAndDetach(evicted, "evicted from reuse pool")
      }
    } else {
      watchForLeaksAndDetach(removedNode, "not eligible for reuse")
    }
  }

  private fun watchForLeaksAndDetach(node: ProtocolNode<W>, note: String) {
    leakDetector.watchReference(node.widget.value, note)
    leakDetector.watchReference(node.widget, note)
    leakDetector.watchReference(node, note)

    // Detaching frees the node's reference to the widget, so this must be done last.
    node.detach()
  }

  /**
   * Implements widget reuse (view recycling).
   *
   * When a widget is eligible from reuse:
   *
   *  * It is removed from [pool].
   *  * It is added to [nodes], alongside its descendant nodes.
   *
   * Returns the updated set of changes that omits any changes that were implemented with reuse.
   */
  private fun applyReuse(changes: List<Change>): List<Change> {
    if (pool.isEmpty()) return changes // Short circuit reuse.

    // Find nodes that have Modifier.reuse
    val idToNode = mutableIntObjectMapOf<ReuseNode<W>>()
    var lastCreatedId = Id.Root
    for (change in changes) {
      if (change is Create) {
        lastCreatedId = change.id
        continue
      }
      if (change !is ModifierChange) continue

      // Must have a reuse modifier.
      if (change.elements.none { it.tag.value == REUSE_MODIFIER_TAG }) continue

      // Must have a Create node that precedes it.
      if (lastCreatedId != change.id) continue

      idToNode[lastCreatedId.value] = ReuseNode(
        widgetId = lastCreatedId,
        // This is the root of the reuse tree, so it will never have a children tag from a parent.
        childrenTag = ChildrenTag(-1),
      )
    }

    // Return early if there's no widgets to attempt to reuse for this set of changes.
    if (idToNode.isEmpty()) return changes

    // Collect node information in rounds, eventually terminating when we loop through all of the
    // changes without encountering an 'Add' change that we hadn't seen in a prior round.
    while (putNodesForChildrenOfNodes(idToNode, changes)) {
      // Keep going.
    }

    // We know the shape of each subtree. Process the Create and ChildrenChange objects.
    populateCreateIndexAndEligibleForReuse(idToNode, changes)

    // If the _shape_ of a reuse candidate matches a pooled node, remove the corresponding changes
    // and use the pooled node.
    val changesAndNulls: Array<Change?> = changes.toTypedArray()
    idToNode.forEachValue { reuseNode ->
      // Only look for reuse roots.
      if (reuseNode.changeIndexForAdd != -1) return@forEachValue

      // Find a pooled node with the same shape hash.
      val shapeHash = shapeHash(factory, reuseNode)
      if (shapeHash == 0L) return@forEachValue // Ineligible for pooling.
      val pooledNodeIndex = pool.indexOfFirst { it.shapeHash == shapeHash }
      if (pooledNodeIndex == -1) return@forEachValue // No shape match.

      // Confirm the reuse node has the same shape. (This defends against hash collisions.)
      val pooledNode = pool[pooledNodeIndex]
      if (!shapesEqual(factory, reuseNode, pooledNode)) return@forEachValue

      // Success! Take the pooled node.
      pool.removeAt(pooledNodeIndex)
      reuseNode.assignPooledNodeRecursive(nodes, changesAndNulls, pooledNode)
      pooledNode.shapeHash = 0L // An updated hash will be computed if it's pooled again.
    }

    // Build a new changes list that omits the events we no longer need.
    return changesAndNulls.filterNotNull()
  }

  /**
   * Populate [idToNode] with the immediate children of the elements of [idToNode]. Call this
   * function in rounds until the entire tree is constructed.
   *
   * Returns true if new child nodes were found and added.
   */
  private fun putNodesForChildrenOfNodes(
    idToNode: MutableIntObjectMap<ReuseNode<W>>,
    changes: List<Change>,
  ): Boolean {
    var nodesAddedToMap = false
    for ((index, change) in changes.withIndex()) {
      if (change !is Add) continue
      val parent = idToNode[change.id.value] ?: continue // Parent isn't reused.
      if (change.childId.value in idToNode) continue // Child already created.

      val child = ReuseNode<W>(
        widgetId = change.childId,
        childrenTag = change.tag,
        indexInParent = change.index,
        changeIndexForAdd = index,
      )
      idToNode[change.childId.value] = child
      parent.children += child
      nodesAddedToMap = true
    }

    return nodesAddedToMap
  }

  /** Returns true if any nodes were added to the map. */
  private fun populateCreateIndexAndEligibleForReuse(
    idToNode: MutableIntObjectMap<ReuseNode<W>>,
    changes: List<Change>,
  ) {
    for ((index, change) in changes.withIndex()) {
      when {
        // Track the Create for each node in the reuse nodes.
        change is Create -> {
          val node = idToNode[change.id.value]
          if (node != null) {
            node.changeIndexForCreate = index
            node.widgetTag = change.tag
          }
        }

        // Any other children change disqualifies this node from reuse.
        change !is Add && change is ChildrenChange -> {
          val node = idToNode[change.id.value] ?: continue
          node.eligibleForReuse = false
        }
      }
    }
  }

  internal class ReuseNode<W : Any>(
    val widgetId: Id,
    /** Which of its parent's slots this node is added to. */
    val childrenTag: ChildrenTag,
    /** Where this node goes in that slot. */
    val indexInParent: Int = -1,
    /**
     * The index in the changes list to remove if the reuse is performed. This is -1 if this node
     * is the root of the reuse subtree.
     */
    var changeIndexForAdd: Int = -1,
  ) {
    /** Another index in the changes list to clear if the reuse is performed. */
    var changeIndexForCreate: Int = -1

    val children = mutableListOf<ReuseNode<W>>()
    var eligibleForReuse = true
    var widgetTag: WidgetTag = UnknownWidgetTag

    /**
     * When a reused node matches a newly-created node, this puts the reused node and its
     * descendants into the nodes map.
     */
    fun assignPooledNodeRecursive(
      nodes: MutableMap<Int, ProtocolNode<W>>,
      changesAndNulls: Array<Change?>,
      pooled: ProtocolNode<W>,
    ) {
      // Reuse the node.
      pooled.id = widgetId
      val old = nodes.put(widgetId.value, pooled)
      require(old == null) {
        "Insert attempted to replace existing widget with ID ${widgetId.value}"
      }

      // Remove the corresponding changes that we avoided by node reuse. We don't clear the 'Add'
      // that adds the node to its new parent.
      changesAndNulls[changeIndexForCreate] = null
      if (changeIndexForAdd != -1) {
        changesAndNulls[changeIndexForAdd] = null
      }

      for (child in children) {
        child.assignPooledNodeRecursive(
          nodes = nodes,
          pooled = pooled.children(child.childrenTag)!!.nodes[child.indexInParent],
          changesAndNulls = changesAndNulls,
        )
      }
    }
  }
}

@OptIn(RedwoodCodegenApi::class)
private class RootProtocolNode<W : Any>(
  children: Widget.Children<W>,
) : ProtocolNode<W>(Id.Root),
  Widget<W> {
  override val widgetTag: WidgetTag get() = UnknownWidgetTag

  override val widgetName: String get() = "RootProtocolNode"

  private val children = ProtocolChildren(children)

  override fun apply(change: PropertyChange, eventSink: UiEventSink) {
    throw AssertionError("unexpected: $change")
  }

  override fun children(tag: ChildrenTag) = when (tag) {
    ChildrenTag.Root -> children
    else -> throw AssertionError("unexpected: $tag")
  }

  override fun visitIds(visitor: IdVisitor) {
    children.visitIds(visitor)
  }

  override val widget: Widget<W> get() = this

  override val value: W get() = throw AssertionError()

  override var modifier: Modifier
    get() = throw AssertionError()
    set(_) {
      throw AssertionError()
    }

  override fun detach() {
    // Do nothing because 'children' is owned by the host's RedwoodView.
  }
}

private const val REUSE_MODIFIER_TAG = -4_543_827

/**
 * Cache a fixed number of recently removed widgets with the 'reuse' modifier. This number balances
 * the number of cache hits against the memory cost of the pool, and the cost of searching the pool
 * for a match.
 */
internal const val POOL_SIZE = 16
