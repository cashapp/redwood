package example.sunspot.client

import app.cash.treehouse.client.EventSink
import app.cash.treehouse.client.TreeBridge
import app.cash.treehouse.client.TreeMutator
import app.cash.treehouse.client.TreeNode
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.NodeDiff

interface SunspotNodeFactory<T : Any> {
  fun text(parent: SunspotNode<T>): SunspotText<T>
  fun button(parent: SunspotNode<T>, onClick: () -> Unit): SunspotButton<T>
}

interface SunspotNode<out T : Any> : TreeNode {
  val value: T
}

@Suppress("MoveLambdaOutsideParentheses") // Generated code ergo not important.
class SunspotBridge<N : Any>(
  override val root: SunspotNode<N>,
  private val factory: SunspotNodeFactory<N>,
  private val mutator: TreeMutator<N>,
) : TreeBridge<SunspotNode<N>> {
  override fun insert(
    parent: SunspotNode<N>,
    insert: NodeDiff.Insert,
    events: EventSink,
  ): SunspotNode<N> {
    val id = insert.childId
    val node = when (val type = insert.type) {
      1 -> factory.text(parent)
      2 -> factory.button(parent, { events.send(Event(id, 1L /* click */, null)) })
      else -> throw IllegalArgumentException("Unknown type $type")
    }
    mutator.insert(parent.value, insert.index, node.value)
    return node
  }

  override fun move(parent: SunspotNode<N>, move: NodeDiff.Move) {
    mutator.move(parent.value, move.fromIndex, move.toIndex, move.count)
  }

  override fun remove(parent: SunspotNode<N>, remove: NodeDiff.Remove) {
    mutator.remove(parent.value, remove.index, remove.count)
  }

  override fun clear() {
    mutator.clear(root.value)
  }
}
