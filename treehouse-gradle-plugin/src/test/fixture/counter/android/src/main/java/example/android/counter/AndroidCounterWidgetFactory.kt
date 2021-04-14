package example.android.counter

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.counter.widget.CounterBox
import example.counter.widget.CounterButton
import example.counter.widget.CounterText
import example.counter.widget.CounterWidgetFactory

class AndroidCounterWidgetFactory(
  private val context: Context,
) : CounterWidgetFactory<View> {
  override fun CounterBox(): CounterBox<View> {
    val view = LinearLayout(context)
    return AndroidCounterBox(view)
  }

  override fun CounterText(): CounterText<View> {
    val view = TextView(context)
    return AndroidCounterText(view)
  }

  override fun CounterButton(): CounterButton<View> {
    val view = Button(context)
    return AndroidCounterButton(view)
  }
}
