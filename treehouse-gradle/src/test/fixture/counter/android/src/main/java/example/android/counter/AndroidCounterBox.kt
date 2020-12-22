package example.android.counter

import android.view.View
import android.widget.LinearLayout
import example.counter.display.CounterBox

class AndroidCounterBox(
  override val value: LinearLayout,
) : CounterBox<View> {
  override fun orientation(orientation: Boolean) {
    value.orientation = if (orientation) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
  }

  override val children = ViewGroupChildren(value)
}
