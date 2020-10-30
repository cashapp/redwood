package example.android.sunspot

import android.view.View
import android.widget.Button
import android.widget.TextView
import example.sunspot.client.SunspotButton
import example.sunspot.client.SunspotNode
import example.sunspot.client.SunspotNodeFactory
import example.sunspot.client.SunspotText

object AndroidSunspotNodeFactory : SunspotNodeFactory<View> {
  override fun SunspotText(
    parent: SunspotNode<View>,
  ): SunspotText<View> {
    val view = TextView(parent.value.context)
    return AndroidSunspotText(view)
  }

  override fun SunspotButton(
    parent: SunspotNode<View>,
    onClick: () -> Unit,
  ): SunspotButton<View> {
    val view = Button(parent.value.context)
    return AndroidSunspotButton(view, onClick)
  }
}
