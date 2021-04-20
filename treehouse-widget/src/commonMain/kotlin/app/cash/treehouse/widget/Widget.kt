package app.cash.treehouse.widget

import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff

interface Widget<T : Any> {
  val value: T

  fun apply(diff: PropertyDiff, events: (Event) -> Unit)

  fun children(tag: Int): Children<T> {
    throw IllegalArgumentException("Widget does not support children")
  }

  interface Children<T : Any> {
    fun insert(index: Int, widget: T)
    fun move(fromIndex: Int, toIndex: Int, count: Int)
    fun remove(index: Int, count: Int)
    fun clear()

    companion object {
      fun validateInsert(childCount: Int, index: Int) {
        if (index < 0 || index > childCount) {
          throw IndexOutOfBoundsException("index must be in range [0, $childCount]: $index")
        }
      }

      fun validateMove(childCount: Int, fromIndex: Int, toIndex: Int, count: Int) {
        if (fromIndex < 0 || fromIndex >= childCount) {
          throw IndexOutOfBoundsException(
            "fromIndex must be in range [0, $childCount): $fromIndex"
          )
        }
        if (toIndex < 0 || toIndex > childCount) {
          throw IndexOutOfBoundsException(
            "toIndex must be in range [0, $childCount]: $toIndex"
          )
        }
        if (count < 0) {
          throw IndexOutOfBoundsException("count must be > 0: $count")
        }
        if (fromIndex + count > childCount) {
          throw IndexOutOfBoundsException(
            "count exceeds children: fromIndex=$fromIndex, count=$count, children=$childCount"
          )
        }
      }

      fun validateRemove(childCount: Int, index: Int, count: Int) {
        if (index < 0 || index >= childCount) {
          throw IndexOutOfBoundsException("Index must be in range [0, $childCount): $index")
        }
        val toIndex = index + count
        if (toIndex < index || toIndex > childCount) {
          throw IndexOutOfBoundsException(
            "Count must be in range [0, ${childCount - index}): $count"
          )
        }
      }
    }
  }

  interface Factory<T : Any> {
    fun create(kind: Int, id: Long): Widget<T>
  }
}
