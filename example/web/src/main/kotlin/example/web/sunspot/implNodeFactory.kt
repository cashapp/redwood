package example.web.sunspot

import example.sunspot.client.SunspotButton
import example.sunspot.client.SunspotNode
import example.sunspot.client.SunspotNodeFactory
import example.sunspot.client.SunspotText
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

object HtmlSunspotNodeFactory : SunspotNodeFactory<HTMLElement> {
  override fun text(
    parent: SunspotNode<HTMLElement>,
  ): SunspotText<HTMLElement> {
    val span = parent.value.ownerDocument!!.createElement("span") as HTMLSpanElement
    return HtmlSunspotText(span)
  }

  override fun button(
    parent: SunspotNode<HTMLElement>,
    onClick: () -> Unit,
  ): SunspotButton<HTMLElement> {
    val button = parent.value.ownerDocument!!.createElement("button") as HTMLButtonElement
    return HtmlSunspotButton(button, onClick)
  }
}
