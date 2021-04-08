package example.android.sunspot

import android.graphics.Color
import android.view.View
import android.widget.TextView
import example.sunspot.widget.SunspotText

class AndroidSunspotText(
  override val value: TextView,
) : SunspotText<View> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun color(color: String) {
    value.setTextColor(Color.parseColor(color))
  }
}
