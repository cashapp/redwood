package app.cash.treehouse.widget

import android.view.View
import android.view.ViewGroup
import app.cash.treehouse.widget.Widget.Children.Companion.validateInsert
import app.cash.treehouse.widget.Widget.Children.Companion.validateMove
import app.cash.treehouse.widget.Widget.Children.Companion.validateRemove

class ViewGroupChildren(private val parent: ViewGroup) : Widget.Children<View> {
  override fun insert(index: Int, widget: View) {
    validateInsert(parent.childCount, index)

    parent.addView(widget, index)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    validateMove(parent.childCount, fromIndex, toIndex, count)

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
    validateRemove(parent.childCount, index, count)

    parent.removeViews(index, count)
  }

  override fun clear() {
    parent.removeAllViews()
  }
}
