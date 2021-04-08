package example.android.sunspot

import android.view.View
import android.view.ViewGroup
import app.cash.treehouse.widget.Widget

class ViewGroupChildren(private val parent: ViewGroup) : Widget.Children<View> {
  override fun insert(index: Int, widget: View) {
    parent.addView(widget, index)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    val views = Array(count) { offset ->
      parent.getChildAt(fromIndex + offset)
    }
    parent.removeViews(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    views.forEachIndexed { offset, view ->
      parent.addView(view, newIndex + offset)
    }
  }

  override fun remove(index: Int, count: Int) {
    parent.removeViews(index, count)
  }

  override fun clear() {
    parent.removeAllViews()
  }
}
