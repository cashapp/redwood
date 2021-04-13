package example.android.counter

import android.view.View
import android.widget.Button
import example.counter.widget.CounterButton

class AndroidCounterButton(
  override val value: Button,
) : CounterButton<View> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    value.setOnClickListener(
      if (onClick != null) {
        { onClick() }
      } else {
        null
      }
    )
  }
}
