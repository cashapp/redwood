package example.android.sunspot

import android.view.View
import android.widget.LinearLayout
import example.sunspot.client.SunspotBox

class AndroidSunspotBox(
  override val value: LinearLayout,
) : SunspotBox<View> {
  override fun orientation(orientation: Boolean) {
    value.orientation = if (orientation) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
  }

  override val children = ViewGroupChildren(value)
}
