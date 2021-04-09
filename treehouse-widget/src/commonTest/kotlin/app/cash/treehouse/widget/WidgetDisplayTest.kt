package app.cash.treehouse.widget

import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WidgetDisplayTest {
  @Test fun rootWidgetMustHaveRootChildrenTag() {
    WidgetDisplay(GoodRootWidget, NullWidgetFactory)

    assertFailsWith<IllegalArgumentException> {
      WidgetDisplay(BadRootWidget, NullWidgetFactory)
    }
  }

  private object BadRootWidget : Widget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff) = throw UnsupportedOperationException()
    override fun children(tag: Int) = throw IllegalArgumentException()
  }

  private object GoodRootWidget : Widget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff) = throw UnsupportedOperationException()
    override fun children(tag: Int) = when (tag) {
      RootChildrenTag -> NullWidgetChildren
      else -> throw IllegalArgumentException()
    }
  }

  private object NullWidgetFactory : Widget.Factory<Unit> {
    override fun create(parent: Unit, kind: Int, id: Long, events: (Event) -> Unit) =
      throw UnsupportedOperationException()
  }

  private object NullWidgetChildren : Widget.Children<Unit> {
    override fun insert(index: Int, widget: Unit) = throw UnsupportedOperationException()
    override fun move(fromIndex: Int, toIndex: Int, count: Int) = throw UnsupportedOperationException()
    override fun remove(index: Int, count: Int) = throw UnsupportedOperationException()
    override fun clear() = throw UnsupportedOperationException()
  }
}
