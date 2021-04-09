package app.cash.treehouse.widget

import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff

interface Widget<T : Any> {
  val value: T

  fun apply(diff: PropertyDiff)

  fun children(tag: Int): Children<T> {
    throw IllegalArgumentException("Widget does not support children")
  }

  interface Children<T : Any> {
    fun insert(index: Int, widget: T)
    fun move(fromIndex: Int, toIndex: Int, count: Int)
    fun remove(index: Int, count: Int)
    fun clear()
  }

  interface Factory<T : Any> {
    fun create(parent: T, kind: Int, id: Long, events: (Event) -> Unit): Widget<T>
  }
}
