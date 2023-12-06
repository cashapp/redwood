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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.toUIColor
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView

class UIViewFlexContainerTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractFlexContainerTest<UIView>() {
  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): UIViewTestFlexContainer {
    val container = UIViewTestFlexContainer(UIViewFlexContainer(direction))
    container.value.backgroundColor = backgroundColor.toUIColor()
    return container
  }

  override fun row() = flexContainer(FlexDirection.Row)

  override fun column() = flexContainer(FlexDirection.Column)

  override fun widget(backgroundColor: Int): Text<UIView> {
    return object : Text<UIView> {
      override val value = UILabel().apply {
        this.numberOfLines = 0
        this.backgroundColor = backgroundColor.toUIColor()
        this.textColor = UIColor.blackColor
      }

      override var modifier: Modifier = Modifier

      override fun text(text: String) {
        value.text = text
      }
    }
  }

  class UIViewTestFlexContainer internal constructor(
    private val delegate: UIViewFlexContainer,
  ) : TestFlexContainer<UIView>, FlexContainer<UIView> by delegate, ChangeListener by delegate {
    private var childCount = 0

    init {
      value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 1.0, alpha = 0.2)
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

  override fun verifySnapshot(container: Widget<UIView>, name: String?) {
    val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.
    container.value.setFrame(screenSize)

    // Snapshot the container on a white background.
    val frame = UIView().apply {
      backgroundColor = UIColor.whiteColor
      setFrame(screenSize)
      addSubview(container.value)
      layoutIfNeeded()
    }

    callback.verifySnapshot(frame, name)
    container.value.removeFromSuperview()
  }
}
