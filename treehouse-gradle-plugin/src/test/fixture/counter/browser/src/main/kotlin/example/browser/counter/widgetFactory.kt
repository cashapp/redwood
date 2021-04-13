package example.browser.counter

import example.counter.widget.CounterBox
import example.counter.widget.CounterButton
import example.counter.widget.CounterText
import example.counter.widget.CounterWidgetFactory
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

object HtmlCounterNodeFactory : CounterWidgetFactory<HTMLElement> {
  override fun CounterBox(parent: HTMLElement): CounterBox<HTMLElement> {
    val div = parent.ownerDocument!!.createElement("div") as HTMLDivElement
    return HtmlCounterBox(div)
  }

  override fun CounterText(
    parent: HTMLElement,
  ): CounterText<HTMLElement> {
    val span = parent.ownerDocument!!.createElement("span") as HTMLSpanElement
    return HtmlCounterText(span)
  }

  override fun CounterButton(
    parent: HTMLElement,
  ): CounterButton<HTMLElement> {
    val button = parent.ownerDocument!!.createElement("button") as HTMLButtonElement
    return HtmlCounterButton(button)
  }
}
