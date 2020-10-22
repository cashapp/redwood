package example.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.treehouse.server.TreehouseScope
import example.sunspot.server.Button
import example.sunspot.server.Text

@Composable
fun TreehouseScope.Counter(value: Int = 0) {
  var count by remember { mutableStateOf(value) }

  Button("-1", onClick = { count-- })
  Text(count.toString())
  Button("+1", onClick = { count++ })
}
