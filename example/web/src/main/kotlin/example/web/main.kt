package example.web

import app.cash.treehouse.client.TreehouseClient
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.TreeDiff
import example.sunspot.client.SunspotBridge
import example.web.sunspot.HtmlContainerMutator
import example.web.sunspot.HtmlSunspotElement
import example.web.sunspot.HtmlSunspotNodeFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.w3c.dom.HTMLElement

fun main() {
  val content = document.getElementById("content")!! as HTMLElement

  GlobalScope.launch {
    val client = HttpClient(Js) {
      install(WebSockets)
    }

    val serializer = Json {
      useArrayPolymorphism = true
      serializersModule = SerializersModule {
        polymorphic(Any::class) {
          subclass(String::class, String.serializer())
          subclass(Boolean::class, Boolean.serializer())
        }
      }
    }

    client.ws(host = "localhost", port = 8765, path = "/counter") {
      val treehouse = TreehouseClient(
        bridge = SunspotBridge(
          root = HtmlSunspotElement(content),
          factory = HtmlSunspotNodeFactory,
          mutator = HtmlContainerMutator,
        ),
        { event ->
          console.log("TreehouseEvent: $event")
          val json = serializer.encodeToString(Event.serializer(), event)
          outgoing.offer(Frame.Text(json))
        },
      )

      for (frame in incoming) {
        val json = (frame as Frame.Text).readText()
        val diff = serializer.decodeFromString(TreeDiff.serializer(), json)
        console.log("TreehouseDiff: $diff")
        treehouse.apply(diff)
      }
    }
  }
}
