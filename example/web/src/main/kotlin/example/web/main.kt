package example.web

import app.cash.treehouse.compose.TreehouseComposition
import app.cash.treehouse.compose.WindowAnimationFrameClock
import app.cash.treehouse.widget.EventSink
import app.cash.treehouse.widget.WidgetDisplay
import app.cash.treehouse.protocol.Event
import example.shared.Counter
import example.web.sunspot.HtmlSunspotBox
import example.web.sunspot.HtmlSunspotNodeFactory
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

fun main() {
  // Indirection to create a cyclic dependency between the client and the server for the demo.
  val eventSink = object : EventSink {
    lateinit var composition: TreehouseComposition
    override fun send(event: Event) {
      console.log("TreehouseEvent", event.toString())
      composition.sendEvent(event)
    }
  }

  val content = document.getElementById("content")!! as HTMLElement
  val display = WidgetDisplay(
    root = HtmlSunspotBox(content),
    factory = HtmlSunspotNodeFactory,
    events = eventSink,
  )

  val server = TreehouseComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    diff = { diff ->
      console.log("TreehouseDiff", diff.toString())
      display.apply(diff)
    }
  )
  eventSink.composition = server

  server.setContent {
    Counter()
  }
}
