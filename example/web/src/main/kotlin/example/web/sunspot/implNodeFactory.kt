package example.web.sunspot

import example.sunspot.client.SunspotBox
import example.sunspot.client.SunspotButton
import example.sunspot.client.SunspotNodeFactory
import example.sunspot.client.SunspotText
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

object HtmlSunspotNodeFactory : SunspotNodeFactory<HTMLElement> {
  override fun SunspotBox(parent: HTMLElement): SunspotBox<HTMLElement> {
    val div = parent.ownerDocument!!.createElement("div") as HTMLDivElement
    return HtmlSunspotBox(div)
  }

  override fun SunspotText(
    parent: HTMLElement,
  ): SunspotText<HTMLElement> {
    val span = parent.ownerDocument!!.createElement("span") as HTMLSpanElement
    return HtmlSunspotText(span)
  }

  override fun SunspotButton(
    parent: HTMLElement,
    onClick: () -> Unit,
  ): SunspotButton<HTMLElement> {
    val button = parent.ownerDocument!!.createElement("button") as HTMLButtonElement
    return HtmlSunspotButton(button, onClick)
  }
}
