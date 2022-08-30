package app.cash.zipline.samples.emojisearch

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import app.cash.redwood.LayoutModifier
import example.schema.widget.Column

class ComposeUiColumn : Column<@Composable () -> Unit> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val children = ComposeUiWidgetChildren()

  override val value = @Composable {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      children.render()
    }
  }
}
