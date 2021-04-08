package example.android.counter

import android.graphics.Color
import android.view.View
import android.widget.TextView
import example.counter.widget.CounterText

class AndroidCounterText(
  override val value: TextView,
) : CounterText<View> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun color(color: String) {
    value.setTextColor(Color.parseColor(color))
  }
}
