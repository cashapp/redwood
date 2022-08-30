package app.cash.zipline.samples.emojisearch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.LayoutModifier
import example.schema.widget.TextInput

class ComposeUiTextInput : TextInput<@Composable () -> Unit> {
  private var hint by mutableStateOf("")
  private var text by mutableStateOf("")
  private var onTextChanged: ((String) -> Unit)? = null

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value = @Composable {
    TextField(
      value = text,
      onValueChange = { onTextChanged?.invoke(it) },
      label = { Text(hint) },
      maxLines = 2,
      textStyle = Typography.h3,
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
    )
  }

  override fun hint(hint: String) {
    this.hint = hint
  }

  override fun text(text: String) {
    this.text = text
  }

  override fun onTextChanged(onTextChanged: ((String) -> Unit)?) {
    this.onTextChanged = onTextChanged
  }
}
