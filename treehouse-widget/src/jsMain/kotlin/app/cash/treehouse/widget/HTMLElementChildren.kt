package app.cash.treehouse.widget

import app.cash.treehouse.widget.Widget.Children.Companion.validateInsert
import app.cash.treehouse.widget.Widget.Children.Companion.validateMove
import app.cash.treehouse.widget.Widget.Children.Companion.validateRemove
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

class HTMLElementChildren(
  private val parent: HTMLElement,
) : Widget.Children<HTMLElement> {
  override fun insert(index: Int, widget: HTMLElement) {
    validateInsert(parent.childElementCount, index)

    // Null element returned when index == childCount causes insertion at end.
    val current = parent.children[index]
    parent.insertBefore(widget, current)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    validateMove(parent.childElementCount, fromIndex, toIndex, count)

    val elements = Array(count) {
      val element = parent.children[fromIndex] as HTMLElement
      parent.removeChild(element)
      element
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    elements.forEachIndexed { offset, element ->
      // Null element returned when newIndex + offset == childCount causes insertion at end.
      val current = parent.children[newIndex + offset]
      parent.insertBefore(element, current)
    }
  }

  override fun remove(index: Int, count: Int) {
    validateRemove(parent.childElementCount, index, count)

    repeat(count) {
      parent.removeChild(parent.children[index]!!)
    }
  }

  override fun clear() {
    parent.clear()
  }
}
