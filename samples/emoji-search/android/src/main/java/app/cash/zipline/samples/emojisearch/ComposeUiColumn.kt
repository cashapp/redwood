package app.cash.zipline.samples.emojisearch

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import app.cash.redwood.LayoutModifier
import example.schema.widget.Column

class ComposeUiColumn : Column<@Composable () -> Unit> {
  override var layoutModifiers = LayoutModifier

  override val children = ComposeUiWidgetChildren()

  override val value = @Composable {
    Column {
      children.render()
    }
  }
}
