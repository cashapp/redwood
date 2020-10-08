package example.web

import app.cash.treehouse.Treehouse
import example.shared.launchCounterIn
import example.sunspot.SunspotBridge
import example.web.sunspot.HtmlContainerMutator
import example.web.sunspot.HtmlSunspotElement
import example.web.sunspot.HtmlSunspotNodeFactory
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import org.w3c.dom.HTMLElement

fun main() {
  val content = document.getElementById("content")!! as HTMLElement

  val treehouse = Treehouse(
    bridge = SunspotBridge(
      root = HtmlSunspotElement(content),
      factory = HtmlSunspotNodeFactory,
      mutator = HtmlContainerMutator,
    ),
  )

  treehouse.launchCounterIn(GlobalScope)
}
