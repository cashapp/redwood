package app.cash.treehouse

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff

interface TreeBridge<N : TreeNode> {
  val root: N

  fun insert(parent: N, insert: NodeDiff.Insert, events: EventSink): N
  fun move(parent: N, move: NodeDiff.Move)
  fun remove(parent: N, remove: NodeDiff.Remove)
  fun clear()
}

interface TreeNode {
  fun apply(diff: PropertyDiff)
}

interface TreeMutator<N : Any> {
  fun insert(parent: N, index: Int, node: N)
  fun move(parent: N, fromIndex: Int, toIndex: Int, count: Int)
  fun remove(parent: N, index: Int, count: Int)
  fun clear(parent: N)
}

data class Event(
  val nodeId: Long,
  val eventId: Long,
  val value: Any?, // TODO
)

fun interface EventSink {
  fun send(event: Event)
}
