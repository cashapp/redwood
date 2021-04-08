package example.android.sunspot

import android.view.View
import android.widget.Button
import example.sunspot.widget.SunspotButton

class AndroidSunspotButton(
  override val value: Button,
  private val onClick: () -> Unit,
) : SunspotButton<View>,
    View.OnClickListener {
  override fun text(text: String?) {
    value.text = text
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }

  override fun onClick(onClick: Boolean) {
    value.setOnClickListener(if (onClick) this else null)
  }

  override fun onClick(v: View?) {
    onClick.invoke()
  }
}
