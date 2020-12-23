package example.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.treehouse.compose.TreehouseScope
import example.counter.compose.CounterButton
import example.counter.compose.CounterText

@Composable
fun TreehouseScope.Counter(value: Int = 0) {
  var count by remember { mutableStateOf(value) }

  CounterButton("-1", onClick = { count-- })
  CounterText(count.toString())
  CounterButton("+1", onClick = { count++ })
}
