package example.android.counter

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.counter.client.CounterBox
import example.counter.client.CounterButton
import example.counter.client.CounterNodeFactory
import example.counter.client.CounterText

object AndroidCounterNodeFactory : CounterNodeFactory<View> {
  override fun CounterBox(parent: View): CounterBox<View> {
    val view = LinearLayout(parent.context)
    return AndroidCounterBox(view)
  }

  override fun CounterText(
    parent: View,
  ): CounterText<View> {
    val view = TextView(parent.context)
    return AndroidCounterText(view)
  }

  override fun CounterButton(
    parent: View,
    onClick: () -> Unit,
  ): CounterButton<View> {
    val view = Button(parent.context)
    return AndroidCounterButton(view, onClick)
  }
}
