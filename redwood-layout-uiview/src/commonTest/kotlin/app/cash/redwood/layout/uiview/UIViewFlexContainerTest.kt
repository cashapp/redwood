/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.toUIColor
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIScrollView
import platform.UIKit.UIView

class UIViewFlexContainerTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractFlexContainerTest<UIView>() {
  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): UIViewTestFlexContainer {
    return UIViewTestFlexContainer(UIViewFlexContainer(direction)).apply {
      value.backgroundColor = backgroundColor.toUIColor()
    }
  }

  override fun row() = flexContainer(FlexDirection.Row)

  override fun column() = flexContainer(FlexDirection.Column)

  override fun text() = UIViewText()

  class UIViewTestFlexContainer internal constructor(
    private val delegate: UIViewFlexContainer,
  ) : TestFlexContainer<UIView>,
    FlexContainer<UIView> by delegate,
    ChangeListener by delegate {
    private var childCount = 0

    override val children: Widget.Children<UIView> = delegate.children

    init {
      value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 1.0, alpha = 0.2)
    }

    override fun onScroll(onScroll: ((Px) -> Unit)?) {
      delegate.onScroll(onScroll)
    }

    override fun scroll(offset: Px) {
      (delegate.value as UIScrollView).setContentOffset(cValue { y = offset.value }, false)
    }

    override fun add(widget: Widget<UIView>) {
      addAt(childCount, widget)
    }

    override fun addAt(index: Int, widget: Widget<UIView>) {
      delegate.children.insert(index, widget)
      childCount++
    }

    override fun removeAt(index: Int) {
      delegate.children.remove(index = index, count = 1)
      childCount--
    }
  }

  override fun verifySnapshot(widget: UIView, name: String?) {
    val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.
    widget.setFrame(screenSize)

    // Snapshot the container on a white background.
    val frame = UIView().apply {
      backgroundColor = UIColor.whiteColor
      setFrame(screenSize)
      addSubview(widget)
      layoutIfNeeded()
    }

    callback.verifySnapshot(frame, name)
    widget.removeFromSuperview()
  }
}
