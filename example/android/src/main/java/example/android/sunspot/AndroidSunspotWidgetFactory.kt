package example.android.sunspot

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory

class AndroidSunspotWidgetFactory(
  private val context: Context,
) : SunspotWidgetFactory<View> {
  override fun SunspotBox(): SunspotBox<View> {
    val view = LinearLayout(context)
    return AndroidSunspotBox(view)
  }

  override fun SunspotText(): SunspotText<View> {
    val view = TextView(context)
    return AndroidSunspotText(view)
  }

  override fun SunspotButton(): SunspotButton<View> {
    val view = Button(context)
    return AndroidSunspotButton(view)
  }
}
