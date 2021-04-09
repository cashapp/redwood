package example.web.sunspot

import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

object HtmlSunspotNodeFactory : SunspotWidgetFactory<HTMLElement> {
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
  ): SunspotButton<HTMLElement> {
    val button = parent.ownerDocument!!.createElement("button") as HTMLButtonElement
    return HtmlSunspotButton(button)
  }
}
