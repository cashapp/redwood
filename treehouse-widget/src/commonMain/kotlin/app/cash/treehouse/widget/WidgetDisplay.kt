package app.cash.treehouse.widget

import app.cash.treehouse.protocol.ChildrenDiff
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootId
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event

interface Display {
  fun apply(diff: Diff, events: (Event) -> Unit)
}

class WidgetDisplay<T : Any>(
  private val root: Widget<T>,
  private val factory: Widget.Factory<T>,
) : Display {
  private val widgets = mutableMapOf(RootId to root)

  override fun apply(diff: Diff, events: (Event) -> Unit) {
    for (childrenDiff in diff.childrenDiffs) {
      val widget = checkNotNull(widgets[childrenDiff.id]) {
        "Unknown widget ID ${childrenDiff.id}"
      }
      val children = widget.children(childrenDiff.tag)

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(widget.value, childrenDiff.kind, childrenDiff.childId, events)
          widgets[childrenDiff.childId] = childWidget
          children.insert(childrenDiff.index, childWidget.value)
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
          // TODO we need to remove widgets from our map!
        }
        ChildrenDiff.Clear -> {
          children.clear()
          widgets.clear()
          widgets[RootId] = root
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val widget = checkNotNull(widgets[propertyDiff.id]) {
        "Unknown widget ID ${propertyDiff.id}"
      }

      widget.apply(propertyDiff)
    }
  }
}
