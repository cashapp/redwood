package example.android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.compose.TreehouseComposition
import app.cash.treehouse.display.EventSink
import app.cash.treehouse.display.TreehouseDisplay
import app.cash.treehouse.protocol.Event
import example.android.counter.AndroidCounterBox
import example.android.counter.AndroidCounterNodeFactory
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
      lateinit var composition: TreehouseComposition
      override fun send(event: Event) {
        Log.d("TreehouseEvent", event.toString())
        composition.sendEvent(event)
      }
    }

    val display = TreehouseDisplay(
      root = AndroidCounterBox(root),
      factory = AndroidCounterNodeFactory,
      events = eventSink,
    )

    val composition = TreehouseComposition(
      scope = scope,
      diff = { diff ->
        Log.d("TreehouseDiff", diff.toString())
        display.apply(diff)
      }
    )
    eventSink.composition = composition

    composition.setContent {
      Counter()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
