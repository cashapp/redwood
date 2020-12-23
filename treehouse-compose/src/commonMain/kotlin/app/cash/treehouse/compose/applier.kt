package app.cash.treehouse.compose

import androidx.compose.runtime.AbstractApplier
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff

interface TreehouseScope {
  fun nextId(): Long
  fun appendDiff(diff: NodeDiff)
  fun appendDiff(diff: PropertyDiff)
}

open class Node(
  val id: Long,
  val type: Int,
) {
  internal val children = mutableListOf<Node>()

  open fun sendEvent(event: Event) {
    throw IllegalStateException("Node ID $id of type $type does not handle events")
  }
}

internal class ProtocolApplier(
  root: Node,
  private val scope: TreehouseScope,
) : AbstractApplier<Node>(root) {
  val nodes = mutableMapOf(root.id to root)

  override fun insert(index: Int, instance: Node) {
    current.children.add(index, instance)
    nodes[instance.id] = instance
    scope.appendDiff(NodeDiff.Insert(current.id, instance.id, instance.type, index))
  }

  override fun remove(index: Int, count: Int) {
    val children = current.children
    for (i in index until index + count) {
      nodes.remove(children[i].id)
    }
    children.remove(index, count)
    scope.appendDiff(NodeDiff.Remove(current.id, index, count))
  }

  override fun move(from: Int, to: Int, count: Int) {
    current.children.move(from, to, count)
    scope.appendDiff(NodeDiff.Move(current.id, from, to, count))
  }

  override fun onClear() {
    current.children.clear()
    nodes.clear()
    nodes[current.id] = current // Restore root node into map.
    scope.appendDiff(NodeDiff.Clear)
  }
}
