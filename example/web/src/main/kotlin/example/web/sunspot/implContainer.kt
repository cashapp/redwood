package example.web.sunspot

import app.cash.treehouse.TreeMutator
import org.w3c.dom.HTMLElement

object HtmlContainerMutator : TreeMutator<HTMLElement> {
  override fun insert(parent: HTMLElement, index: Int, node: HTMLElement) {
    require(index == parent.childElementCount) // TODO support more than appending
    parent.appendChild(node)
  }

  override fun move(parent: HTMLElement, fromIndex: Int, toIndex: Int, count: Int) {
    TODO("Not implemented")
  }

  override fun remove(parent: HTMLElement, index: Int, count: Int) {
    TODO("Not implemented")
  }
}
