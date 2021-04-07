package app.cash.treehouse.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff

interface TreehouseScope {
  fun nextId(): Long
  fun appendDiff(diff: NodeDiff)
  fun appendDiff(diff: PropertyDiff)
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
 */
@Composable
fun `$SyntheticChildren`(index: Int, content: @Composable () -> Unit) {
  ComposeNode<ChildrenNode.Intermediate, Applier<Node>>(
    factory = ChildrenNode::Intermediate,
    update = {
      set(index) {
        childrenIndex = index
      }
    },
    content = content,
  )
}

/**
 * A node which exists in the tree to emulate supporting multiple children sets but which does not
 * appear directly in the protocol.
 */
private sealed class ChildrenNode(id: Long) : Node(id, -1) {
  abstract val childrenIndex: Int

  class Intermediate : ChildrenNode(-1) {
    override var childrenIndex = -1
  }

  class Root : ChildrenNode(TreeDiff.RootId) {
    // TODO We cannot actually guarantee this is what index the display root uses for children.
    override val childrenIndex get() = 1
  }
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

/**
 * An [Applier] which records operations on the tree as models which can then be separately applied
 * by the display layer. Additionally, it has special handling for emulating nodes which contain
 * multiple children.
 *
 * Nodes in the tree are required to alternate between [ChildrenNode] instances and
 * non-[ChildrenNode] [Node] subtypes starting from the root. This invariant is maintained by
 * virtue of the fact that all of the input `@Composeables` should be generated code.
 *
 * For example, a node tree may look like this:
 * ```
 *                    ChildrenNode(1)
 *                     /          \
 *                    /            \
 *            ToolbarNode        ListNode
 *             /     \                 \
 *            /       \                 \
 * ChildrenNode(1)  ChildrenNode(2)   ChildrenNode(1)
 *        |              |               /       \
 *        |              |              /         \
 *   ButtonNode     ButtonNode     TextNode     TextNode
 * ```
 * But the protocol diff output would only record non-[ChildrenNode] nodes using their
 * [ChildrenNode.childrenIndex] value:
 * ```
 * Insert(id=<root-id>, childrenIndex=1, type=<toolbar-type>, childId=<toolbar-id>)
 * Insert(id=<toolbar-id>, childrenIndex=1, type=<button-type>, childId=..)
 * Insert(id=<toolbar-id>, childrenIndex=2, type=<button-type>, childId=..)
 * Insert(id=<root-id>, childrenIndex=1, type=<list-type>, childId=<list-id>)
 * Insert(id=<list-id>, childrenIndex=1, type=<text-type>, childId=..)
 * Insert(id=<list-id>, childrenIndex=1, type=<text-type>, childId=..)
 * ```
 */
internal class ProtocolApplier(
  private val scope: TreehouseScope,
) : AbstractApplier<Node>(ChildrenNode.Root()) {
  val nodes = mutableMapOf(root.id to root)

  override fun insertTopDown(index: Int, instance: Node) {
    current.children.add(index, instance)

    // We do not add ChildrenNode instances to the map (they have no unique IDs and are only
    // available through indexing on the parent) and we do not send them over the wire to the
    // display (they are always implied by the display interfaces).
    if (instance !is ChildrenNode) {
      val current = current as ChildrenNode

      nodes[instance.id] = instance
      scope.appendDiff(
        NodeDiff.Insert(current.id, current.childrenIndex, instance.id, instance.type, index))
    }
  }

  override fun insertBottomUp(index: Int, instance: Node) {
    // Ignored, we insert top-down for now.
  }

  override fun remove(index: Int, count: Int) {
    // ChildrenNode instances are never removed from their parents.
    val current = current as ChildrenNode

    val children = current.children
    for (i in index until index + count) {
      nodes.remove(children[i].id)
    }
    children.remove(index, count)
    scope.appendDiff(NodeDiff.Remove(current.id, current.childrenIndex, index, count))
  }

  override fun move(from: Int, to: Int, count: Int) {
    // ChildrenNode instances are never moved within their parents.
    val current = current as ChildrenNode

    current.children.move(from, to, count)
    scope.appendDiff(NodeDiff.Move(current.id, current.childrenIndex, from, to, count))
  }

  override fun onClear() {
    current.children.clear()
    nodes.clear()
    nodes[current.id] = current // Restore root node into map.
    scope.appendDiff(NodeDiff.Clear)
  }
}
