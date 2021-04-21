package app.cash.treehouse.widget

import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.subviews

class UIViewChildrenTest : AbstractWidgetChildrenTest<UIView>() {
  private val parent = UIView()
  override val children = UIViewChildren(parent)

  override fun widget(name: String): UIView {
    return UILabel().apply {
      text = name
    }
  }

  override fun names(): List<String> {
    return parent.subviews.map { (it as UILabel).text!! }
  }
}
