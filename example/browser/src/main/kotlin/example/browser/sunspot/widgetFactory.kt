package example.browser.sunspot

import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlSunspotNodeFactory(
  private val document: Document,
) : SunspotWidgetFactory<HTMLElement> {
  override fun SunspotBox(): SunspotBox<HTMLElement> {
    val div = document.createElement("div") as HTMLDivElement
    return HtmlSunspotBox(div)
  }

  override fun SunspotText(): SunspotText<HTMLElement> {
    val span = document.createElement("span") as HTMLSpanElement
    return HtmlSunspotText(span)
  }

  override fun SunspotButton(): SunspotButton<HTMLElement> {
    val button = document.createElement("button") as HTMLButtonElement
    return HtmlSunspotButton(button)
  }
}
