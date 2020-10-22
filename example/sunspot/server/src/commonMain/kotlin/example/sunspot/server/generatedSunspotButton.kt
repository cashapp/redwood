package example.sunspot.server

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.emit
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.server.Node
import app.cash.treehouse.server.TreehouseScope

@Composable
fun TreehouseScope.Button(
  text: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
) {
  emit<ButtonNode, Applier<Node>>({ ButtonNode(nextId()) }) {
    set(text) {
      appendDiff(PropertyDiff(id, 1 /* text */, text))
    }
    set(enabled) {
      appendDiff(PropertyDiff(id, 2 /* enabled */, enabled))
    }
    set(onClick) {
      this.onClick = onClick
      appendDiff(PropertyDiff(id, 3 /* onClick */, onClick != null))
    }
  }
}

private class ButtonNode(id: Long) : Node(id, 2) {
  var onClick: (() -> Unit)? = null

  override fun sendEvent(event: Event) {
    when (event.eventId) {
      1L -> onClick?.invoke()
      else -> throw IllegalArgumentException("Unknown event ID ${event.eventId}")
    }
  }
}
