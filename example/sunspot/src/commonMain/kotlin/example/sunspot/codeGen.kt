package example.sunspot

import app.cash.treehouse.Event
import app.cash.treehouse.EventSink
import app.cash.treehouse.TreeBridge
import app.cash.treehouse.TreeMutator
import app.cash.treehouse.TreeNode
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff

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

interface SunspotText<out T : Any> : SunspotNode<T> {
  fun text(text: String?)
  fun color(color: String)

  override fun apply(diff: PropertyDiff) {
    when (val tag = diff.tag) {
      1 -> text(diff.value.toString() /* TODO serialization call */)
      2 -> color(diff.value.toString() /* TODO serialization call */)
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}

interface SunspotButton<out T: Any> : SunspotNode<T> {
  fun text(text: String?)
  fun clickable(clickable: Boolean)
  fun enabled(enabled: Boolean)

  override fun apply(diff: PropertyDiff) {
    when (val tag = diff.tag) {
      1 -> text(diff.value.toString() /* TODO serialization call */)
      2 -> clickable(diff.value as Boolean /* TODO serialization call */)
      3 -> enabled(diff.value as Boolean /* TODO serialization call */)
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
