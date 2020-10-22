package example.android.sunspot

import android.view.View
import android.view.ViewGroup
import app.cash.treehouse.client.TreeMutator

object AndroidContainerMutator : TreeMutator<View> {
  override fun insert(parent: View, index: Int, node: View) {
    (parent as ViewGroup).addView(node, index)
  }

  override fun move(parent: View, fromIndex: Int, toIndex: Int, count: Int) {
    val viewGroup = (parent as ViewGroup)
    val views = Array(count) { offset ->
      viewGroup.getChildAt(fromIndex + offset)
    }
    viewGroup.removeViews(fromIndex, count)

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    views.forEachIndexed { offset, view ->
      viewGroup.addView(view, newIndex + offset)
    }
  }

  override fun remove(parent: View, index: Int, count: Int) {
    (parent as ViewGroup).removeViews(index, count)
  }

  override fun clear(parent: View) {
    (parent as ViewGroup).removeAllViews()
  }
}
