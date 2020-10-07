package app.cash.treehouse

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff

interface TreeBridge<N : TreeNode> {
  val root: N

  fun insert(parent: N, insert: NodeDiff.Insert, events: EventSink): N
  fun move(parent: N, move: NodeDiff.Move)
  fun remove(parent: N, remove: NodeDiff.Remove)
}

interface TreeNode {
  fun apply(diff: PropertyDiff)
}

interface TreeMutator<N : Any> {
  fun insert(parent: N, index: Int, node: N)
  fun move(parent: N, fromIndex: Int, toIndex: Int, count: Int)
  fun remove(parent: N, index: Int, count: Int)
}

fun interface EventSink {
  fun send(nodeId: Long, eventId: Long)
}
