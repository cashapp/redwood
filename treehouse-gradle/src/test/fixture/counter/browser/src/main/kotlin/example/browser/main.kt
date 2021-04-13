package example.browser

import app.cash.treehouse.compose.TreehouseComposition
import app.cash.treehouse.compose.WindowAnimationFrameClock
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.widget.WidgetDisplay
import example.browser.counter.HtmlSunspotBox
import example.browser.counter.HtmlSunspotNodeFactory
import example.shared.Counter
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

fun main() {
  val content = document.getElementById("content")!! as HTMLElement
  val display = WidgetDisplay(
    root = HtmlSunspotBox(content),
    factory = HtmlSunspotNodeFactory,
  )

  lateinit var events: (Event) -> Unit
  val composition = TreehouseComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    diffs = { diff ->
      console.log("TreehouseDiff", diff.toString())
      display.apply(diff, events)
    }
  )
  events = { event ->
    console.log("TreehouseEvent", event.toString())
    composition.sendEvent(event)
  }

  composition.setContent {
    Counter()
  }
}
