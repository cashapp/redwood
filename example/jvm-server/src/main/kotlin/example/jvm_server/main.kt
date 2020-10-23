@file:JvmName("Main")

package example.jvm_server

import androidx.compose.runtime.EmbeddingContext
import androidx.compose.runtime.yoloGlobalEmbeddingContext
import androidx.compose.runtime.dispatch.BroadcastFrameClock
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.TreeDiff
import app.cash.treehouse.server.TreehouseServer
import example.shared.Counter
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.coroutines.CoroutineContext

fun main() {
  val clock = BroadcastFrameClock()

  val executor = Executors.newSingleThreadScheduledExecutor()
  executor.scheduleAtFixedRate({ clock.sendFrame(0) }, 0, 100, MILLISECONDS)

  lateinit var mainThread: Thread
  executor.submit {
    mainThread = Thread.currentThread()
  }.get()

  val context = (executor.asCoroutineDispatcher() as CoroutineContext) + clock
  yoloGlobalEmbeddingContext = object : EmbeddingContext {
    override fun isMainThread(): Boolean {
      return Thread.currentThread() == mainThread
    }
    override fun mainThreadCompositionContext(): CoroutineContext {
      return context
    }
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

  embeddedServer(Netty, 8765) {
    install(WebSockets)

    routing {
      webSocket("/counter") {
        withContext(context) {
          val server = TreehouseServer(
            scope = this,
            diff = { diff ->
              println("TreehouseDiff: $diff")

              val json = serializer.encodeToString(TreeDiff.serializer(), diff)
              outgoing.offer(Frame.Text(json))
            }
          )

          server.setContent {
            Counter()
          }

          for (frame in incoming) {
            val json = (frame as Frame.Text).readText()
            val event = serializer.decodeFromString(Event.serializer(), json)
            println("TreehouseEvent: $event")

            server.sendEvent(event)
          }
        }
      }
    }
  }.start(wait = true)
}
