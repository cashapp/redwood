package app.cash.treehouse.display

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.TreeDiff

class TreehouseDisplay<T : Any>(
  private val root: TreeNode<T>,
  private val factory: TreeNodeFactory<T>,
  private val events: EventSink,
) {
  private val nodes = mutableMapOf(TreeDiff.RootId to root)

  fun apply(diff: TreeDiff) {
    for (nodeDiff in diff.nodeDiffs) {
      val node = checkNotNull(nodes[nodeDiff.id]) {
        "Unknown node ${nodeDiff.id}"
      }

      when (nodeDiff) {
        is NodeDiff.Insert -> {
          val childNode = factory.create(node.value, nodeDiff.kind, nodeDiff.childId, events)
          nodes[nodeDiff.childId] = childNode
          node.children.insert(nodeDiff.index, childNode.value)
        }
        is NodeDiff.Move -> {
          node.children.move(nodeDiff.fromIndex, nodeDiff.toIndex, nodeDiff.count)
        }
        is NodeDiff.Remove -> {
          node.children.remove(nodeDiff.index, nodeDiff.count)
          // TODO we need to remove nodes from our map!
        }
        NodeDiff.Clear -> {
          node.children.clear()
          nodes.clear()
          nodes[TreeDiff.RootId] = root
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val node = checkNotNull(nodes[propertyDiff.id]) {
        "Unknown node ${propertyDiff.id}"
      }

      node.apply(propertyDiff)
    }
  }
}
