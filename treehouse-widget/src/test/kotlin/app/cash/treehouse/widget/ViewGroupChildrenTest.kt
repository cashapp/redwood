package app.cash.treehouse.widget

import android.view.View
import android.widget.FrameLayout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ViewGroupChildrenTest : AbstractWidgetChildrenTest<View>() {
  private val parent = FrameLayout(RuntimeEnvironment.application)
  override val children = ViewGroupChildren(parent)

  override fun widget(name: String): View {
    return View(RuntimeEnvironment.application).apply {
      tag = name
    }
  }

  override fun names(): List<String> {
    return List(parent.childCount) {
      parent.getChildAt(it).tag as String
    }
  }
}
