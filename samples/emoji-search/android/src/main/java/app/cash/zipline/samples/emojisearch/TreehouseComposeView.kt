package app.cash.zipline.samples.emojisearch

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.platform.ComposeView
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.treehouse.*
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.JsonArray

@SuppressLint("ViewConstructor")
public class TreehouseComposeView<T : Any>(
  context: Context,
  private val treehouseApp: TreehouseApp<T>,
  public val widgetFactory: Widget.Factory<@Composable () -> Unit>,
) :  FrameLayout(context), TreehouseView<T> {
  public val composeView: ComposeView = ComposeView(context)
  init {
    addView(composeView)
  }

  /** This is always the user-supplied content. */
  private var content: TreehouseView.Content<T>? = null

  /** This is the actual content, or null if not attached to the screen. */
  override val boundContent: TreehouseView.Content<T>?
    get() {
      return when {
        isAttachedToWindow -> content
        else -> null
      }
    }

  override val protocolDisplayRoot: DiffConsumingWidget<*> = ProtocolDisplayRoot(this)

  public fun setContent(content: TreehouseView.Content<T>) {
    treehouseApp.dispatchers.checkMain()
    this.content = content
    treehouseApp.onContentChanged(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    treehouseApp.onContentChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    treehouseApp.onContentChanged(this)
  }

  override fun generateDefaultLayoutParams(): LayoutParams =
    LayoutParams(MATCH_PARENT, MATCH_PARENT)


}

class ProtocolDisplayRoot(
  private val treehouseComposeView: TreehouseComposeView<*>,
) : DiffConsumingWidget<@Composable () -> Unit> {

  init {
    treehouseComposeView.composeView.setContent {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        children.render()
      }
    }
  }

  override var layoutModifiers: app.cash.redwood.LayoutModifier = app.cash.redwood.LayoutModifier
  private val children = ComposeUiWidgetChildren()

  override val value: @Composable () -> Unit
    get() {
      error("unexpected call")
    }

  override fun updateLayoutModifier(value: JsonArray) {
  }

  override fun apply(diff: app.cash.redwood.protocol.PropertyDiff, eventSink: app.cash.redwood.protocol.EventSink) {
    error("unexpected update on view root: $diff")
  }

  override fun children(tag: Int) = children
}
