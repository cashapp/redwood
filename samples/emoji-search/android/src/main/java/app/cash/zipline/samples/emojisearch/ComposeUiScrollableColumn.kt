package app.cash.zipline.samples.emojisearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.redwood.LayoutModifier
import example.schema.widget.ScrollableColumn

class ComposeUiScrollableColumn : ScrollableColumn<@Composable () -> Unit> {
  override var layoutModifiers = LayoutModifier

  override val children = ComposeUiWidgetChildren()

  override val value = @Composable {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
    ) {
      children.render()
    }
  }
}
