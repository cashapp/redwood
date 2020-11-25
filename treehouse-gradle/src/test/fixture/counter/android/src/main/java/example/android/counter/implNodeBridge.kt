package example.android.counter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import app.cash.treehouse.protocol.PropertyDiff
import example.counter.client.CounterButton
import example.counter.client.CounterNode
import example.counter.client.CounterText

class AndroidCounterViewGroup(
  override val value: ViewGroup,
) : CounterNode<ViewGroup> {
  override fun apply(diff: PropertyDiff) {
    throw UnsupportedOperationException()
  }
}

class AndroidCounterText(
  override val value: TextView,
) : CounterText<TextView> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun color(color: String) {
    value.setTextColor(Color.parseColor(color))
  }
}

class AndroidCounterButton(
  override val value: Button,
  private val onClick: () -> Unit,
) : CounterButton<Button>,
    View.OnClickListener {
  override fun text(text: String?) {
    value.text = text
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }

  override fun onClick(onClick: Boolean) {
    value.setOnClickListener(if (onClick) this else null)
  }

  override fun onClick(v: View?) {
    onClick.invoke()
  }
}
