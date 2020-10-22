package app.cash.treehouse.client

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.TreeDiff
import app.cash.treehouse.protocol.TreeDiff.Companion.RootId

class TreehouseClient<N : TreeNode>(
  private val bridge: TreeBridge<N>,
  private val events: EventSink,
) {
  private val nodes = mutableMapOf(RootId to bridge.root)

  fun apply(diff: TreeDiff) {
    for (nodeDiff in diff.nodeDiffs) {
      val container = nodes[nodeDiff.id]
      checkNotNull(container) { "Unknown node ${nodeDiff.id}" }

      @Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
      val exhaustive = when (nodeDiff) {
        is NodeDiff.Insert -> {
          nodes[nodeDiff.childId] = bridge.insert(container, nodeDiff, events)
        }
        is NodeDiff.Move -> {
          bridge.move(container, nodeDiff)
        }
        is NodeDiff.Remove -> {
          bridge.remove(container, nodeDiff)
        }
        NodeDiff.Clear -> {
          bridge.clear()
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val node = nodes[propertyDiff.id]
      checkNotNull(node) { "Unknown node ${propertyDiff.id}" }
      node.apply(propertyDiff)
    }
  }
}
