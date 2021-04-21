package example.android.sunspot

import android.view.View
import android.widget.LinearLayout
import app.cash.treehouse.widget.ViewGroupChildren
import example.sunspot.widget.SunspotBox

class AndroidSunspotBox(
  override val value: LinearLayout,
) : SunspotBox<View> {
  override val children = ViewGroupChildren(value)
}
