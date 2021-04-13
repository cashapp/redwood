package example.android.sunspot

import android.view.View
import android.widget.LinearLayout
import example.sunspot.widget.SunspotBox

class AndroidSunspotBox(
  override val value: LinearLayout,
) : SunspotBox<View> {
  override val children = ViewGroupChildren(value)
}
