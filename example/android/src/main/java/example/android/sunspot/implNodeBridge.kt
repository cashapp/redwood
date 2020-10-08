package example.android.sunspot

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import app.cash.treehouse.protocol.PropertyDiff
import example.sunspot.SunspotButton
import example.sunspot.SunspotNode
import example.sunspot.SunspotText

class AndroidSunspotViewGroup(
  override val value: ViewGroup,
) : SunspotNode<ViewGroup> {
  override fun apply(diff: PropertyDiff) {
    throw UnsupportedOperationException()
  }
}

class AndroidSunspotText(
  override val value: TextView,
) : SunspotText<TextView> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun color(color: String) {
    value.setTextColor(Color.parseColor(color))
  }
}

class AndroidSunspotButton(
  override val value: Button,
  private val onClick: () -> Unit,
) : SunspotButton<Button>,
    View.OnClickListener {
  override fun text(text: String?) {
    value.text = text
  }

  override fun clickable(clickable: Boolean) {
    value.setOnClickListener(if (clickable) this else null)
  }

  override fun onClick(v: View?) {
    onClick.invoke()
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }
}
