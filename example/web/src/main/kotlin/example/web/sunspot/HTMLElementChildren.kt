package example.web.sunspot

import app.cash.treehouse.display.TreeNode
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

class HTMLElementChildren(
  private val parent: HTMLElement,
) : TreeNode.Children<HTMLElement> {
  override fun insert(index: Int, node: HTMLElement) {
    if (index == parent.childElementCount) {
      parent.appendChild(node)
    } else {
      val current = parent.children[index]!!
      parent.insertBefore(current, node)
    }
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    val nodes = Array(count) { offset ->
      parent.children[fromIndex + offset] as HTMLElement
    }
    remove(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    nodes.forEachIndexed { offset, node ->
      insert(newIndex + offset, node)
    }
  }

  override fun remove(index: Int, count: Int) {
    repeat(count) {
      parent.removeChild(parent.children[index]!!)
    }
  }

  override fun clear() {
    remove(0, parent.childElementCount)
  }
}
