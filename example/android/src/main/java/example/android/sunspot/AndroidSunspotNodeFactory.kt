package example.android.sunspot

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.sunspot.client.SunspotBox
import example.sunspot.client.SunspotButton
import example.sunspot.client.SunspotNodeFactory
import example.sunspot.client.SunspotText

object AndroidSunspotNodeFactory : SunspotNodeFactory<View> {
  override fun SunspotBox(parent: View): SunspotBox<View> {
    val view = LinearLayout(parent.context)
    return AndroidSunspotBox(view)
  }

  override fun SunspotText(
    parent: View,
  ): SunspotText<View> {
    val view = TextView(parent.context)
    return AndroidSunspotText(view)
  }

  override fun SunspotButton(
    parent: View,
    onClick: () -> Unit,
  ): SunspotButton<View> {
    val view = Button(parent.context)
    return AndroidSunspotButton(view, onClick)
  }
}
