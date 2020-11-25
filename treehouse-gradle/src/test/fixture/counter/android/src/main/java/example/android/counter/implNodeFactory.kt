package example.android.counter

import android.view.View
import android.widget.Button
import android.widget.TextView
import example.counter.client.CounterButton
import example.counter.client.CounterNode
import example.counter.client.CounterNodeFactory
import example.counter.client.CounterText

object AndroidCounterNodeFactory : CounterNodeFactory<View> {
  override fun CounterText(
    parent: CounterNode<View>,
  ): CounterText<View> {
    val view = TextView(parent.value.context)
    return AndroidCounterText(view)
  }

  override fun CounterButton(
    parent: CounterNode<View>,
    onClick: () -> Unit,
  ): CounterButton<View> {
    val view = Button(parent.value.context)
    return AndroidCounterButton(view, onClick)
  }
}
