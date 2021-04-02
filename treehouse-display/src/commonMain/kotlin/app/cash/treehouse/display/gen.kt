package app.cash.treehouse.display

import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff

interface TreeNode<T : Any> {
  val value: T

  fun apply(diff: PropertyDiff)

  fun children(index: Int): Children<T> {
    throw IllegalArgumentException("Node does not support children")
  }

  interface Children<T : Any> {
    fun insert(index: Int, node: T)
    fun move(fromIndex: Int, toIndex: Int, count: Int)
    fun remove(index: Int, count: Int)
    fun clear()
  }
}

interface TreeNodeFactory<T : Any> {
  fun create(parent: T, kind: Int, id: Long, events: EventSink): TreeNode<T>
}

fun interface EventSink {
  fun send(event: Event)
}
