package example.web.sunspot

import app.cash.treehouse.protocol.PropertyDiff
import example.sunspot.SunspotButton
import example.sunspot.SunspotNode
import example.sunspot.SunspotText
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlSunspotElement(
  override val value: HTMLElement,
) : SunspotNode<HTMLElement> {
  override fun apply(diff: PropertyDiff) {
    throw UnsupportedOperationException()
  }
}

class HtmlSunspotText(
  override val value: HTMLSpanElement,
) : SunspotText<HTMLSpanElement> {
  override fun text(text: String?) {
    value.textContent = text
  }

  override fun color(color: String) {
    value.style.color = color
  }
}

class HtmlSunspotButton(
  override val value: HTMLButtonElement,
  private val onClick: () -> Unit,
) : SunspotButton<HTMLButtonElement> {
  override fun text(text: String?) {
    value.textContent = text
  }

  override fun clickable(clickable: Boolean) {
    value.onclick = if (clickable) {
      { onClick.invoke() }
    } else {
      null
    }
  }

  override fun enabled(enabled: Boolean) {
    value.disabled = !enabled
  }
}
