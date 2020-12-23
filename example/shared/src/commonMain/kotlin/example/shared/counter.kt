package example.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.treehouse.compose.TreehouseScope
import example.sunspot.compose.SunspotButton
import example.sunspot.compose.SunspotText

@Composable
fun TreehouseScope.Counter(value: Int = 0) {
  var count by remember { mutableStateOf(value) }

  SunspotButton("-1", onClick = { count-- })
  SunspotText(count.toString())
  SunspotButton("+1", onClick = { count++ })
}
