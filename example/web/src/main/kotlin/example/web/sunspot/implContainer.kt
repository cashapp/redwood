package example.web.sunspot

import app.cash.treehouse.TreeMutator
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

object HtmlContainerMutator : TreeMutator<HTMLElement> {
  override fun insert(parent: HTMLElement, index: Int, node: HTMLElement) {
    if (index == parent.childElementCount) {
      parent.appendChild(node)
    } else {
      val current = parent.children[index]!!
      parent.insertBefore(current, node)
    }
  }

  override fun move(parent: HTMLElement, fromIndex: Int, toIndex: Int, count: Int) {
    val nodes = Array(count) { offset ->
      parent.children[fromIndex + offset] as HTMLElement
    }
    remove(parent, fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    nodes.forEachIndexed { offset, node ->
      insert(parent, newIndex + offset, node)
    }
  }

  override fun remove(parent: HTMLElement, index: Int, count: Int) {
    repeat(count) {
      parent.removeChild(parent.children[index]!!)
    }
  }

  override fun clear(parent: HTMLElement) {
    remove(parent, 0, parent.childElementCount)
  }
}
