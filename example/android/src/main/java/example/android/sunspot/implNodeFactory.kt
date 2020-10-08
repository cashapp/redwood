package example.android.sunspot

import android.view.View
import android.widget.Button
import android.widget.TextView
import example.sunspot.SunspotButton
import example.sunspot.SunspotNode
import example.sunspot.SunspotNodeFactory
import example.sunspot.SunspotText

object AndroidSunspotNodeFactory : SunspotNodeFactory<View> {
  override fun text(
    parent: SunspotNode<View>,
  ): SunspotText<View> {
    val view = TextView(parent.value.context)
    return AndroidSunspotText(view)
  }

  override fun button(
    parent: SunspotNode<View>,
    onClick: () -> Unit,
  ): SunspotButton<View> {
    val view = Button(parent.value.context)
    return AndroidSunspotButton(view, onClick)
  }
}
