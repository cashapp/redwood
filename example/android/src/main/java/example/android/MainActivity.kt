package example.android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.client.EventSink
import app.cash.treehouse.client.TreehouseClient
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.server.TreehouseServer
import example.android.sunspot.AndroidSunspotBox
import example.android.sunspot.AndroidSunspotNodeFactory
import example.shared.Counter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : Activity() {
  private val scope = MainScope()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val root = LinearLayout(this).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    setContentView(root)

    // Indirection to create a cyclic dependency between the client and the server for the demo.
    val eventSink = object : EventSink {
      lateinit var server: TreehouseServer
      override fun send(event: Event) {
        Log.d("TreehouseEvent", event.toString())
        server.sendEvent(event)
      }
    }

    val client = TreehouseClient(
      root = AndroidSunspotBox(root),
      factory = AndroidSunspotNodeFactory,
      events = eventSink,
    )

    val server = TreehouseServer(
      scope = scope,
      diff = { diff ->
        Log.d("TreehouseDiff", diff.toString())
        client.apply(diff)
      }
    )
    eventSink.server = server

    server.setContent {
      Counter()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
