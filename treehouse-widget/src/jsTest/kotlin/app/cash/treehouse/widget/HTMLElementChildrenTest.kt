package app.cash.treehouse.widget

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

class HTMLElementChildrenTest : AbstractWidgetChildrenTest<HTMLElement>() {
  private val parent = widget("root")
  override val children = HTMLElementChildren(parent)

  override fun widget(name: String): HTMLElement {
    return (document.createElement("div") as HTMLDivElement).apply {
      id = name
    }
  }

  override fun names(): List<String> {
    val childNodes = parent.children
    return List(childNodes.length) { childNodes[it]!!.id }
  }
}
