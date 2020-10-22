package example.web

import app.cash.treehouse.client.EventSink
import app.cash.treehouse.client.TreehouseClient
import example.sunspot.client.SunspotBridge
import example.web.sunspot.HtmlContainerMutator
import example.web.sunspot.HtmlSunspotElement
import example.web.sunspot.HtmlSunspotNodeFactory
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun main() {
  val content = document.getElementById("content")!! as HTMLElement

  val treehouse = TreehouseClient(
    bridge = SunspotBridge(
      root = HtmlSunspotElement(content),
      factory = HtmlSunspotNodeFactory,
      mutator = HtmlContainerMutator,
    ),
    EventSink { TODO() },
  )

  // TODO communicate with server
}
