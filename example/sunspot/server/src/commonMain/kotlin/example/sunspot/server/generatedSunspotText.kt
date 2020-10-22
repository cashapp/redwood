package example.sunspot.server

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.emit
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.server.Node
import app.cash.treehouse.server.TreehouseScope

@Composable
fun TreehouseScope.Text(
  text: String,
  color: String? = "black",
) {
  emit<Node, Applier<Node>>({ Node(nextId(), 1) }) {
    set(text) {
      appendDiff(PropertyDiff(id, 1 /* text */, text))
    }
    set(color) {
      appendDiff(PropertyDiff(id, 2 /* color */, color))
    }
  }
}
