package example.android.counter

import android.view.View
import android.widget.LinearLayout
import example.counter.widget.CounterBox

class AndroidCounterBox(
  override val value: LinearLayout,
) : CounterBox<View> {
  override val children = ViewGroupChildren(value)
}
