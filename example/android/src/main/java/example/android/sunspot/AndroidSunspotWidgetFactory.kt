package example.android.sunspot

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory

object AndroidSunspotWidgetFactory : SunspotWidgetFactory<View> {
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
  ): SunspotButton<View> {
    val view = Button(parent.context)
    return AndroidSunspotButton(view)
  }
}
