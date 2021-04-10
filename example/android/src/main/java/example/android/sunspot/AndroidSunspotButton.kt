package example.android.sunspot

import android.view.View
import android.widget.Button
import example.sunspot.widget.SunspotButton

class AndroidSunspotButton(
  override val value: Button,
) : SunspotButton<View> {
  override fun text(text: String?) {
    value.text = text
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    value.setOnClickListener(if (onClick != null) {
      { onClick() }
    } else {
      null
    })
  }
}
