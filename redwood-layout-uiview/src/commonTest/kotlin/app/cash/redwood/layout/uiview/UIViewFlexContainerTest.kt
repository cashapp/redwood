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
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UIView

interface UIViewFlexContainerTestCallback {
  fun verifySnapshot(view: UIView, name: String?)
}

class UIViewFlexContainerTest(
  private val callback: UIViewFlexContainerTestCallback,
) : AbstractFlexContainerTest<UIView>() {
  override fun flexContainer(direction: FlexDirection): TestFlexContainer<UIView> {
    return UIViewTestFlexContainer(UIViewFlexContainer(direction))
  }

  override fun widget(): Text<UIView> {
    return object : Text<UIView> {
      override val value = UILabel().apply {
        backgroundColor = UIColor.greenColor
        textColor = UIColor.blackColor
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
      value.backgroundColor = UIColor.blueColor
    }

    override fun add(widget: Widget<UIView>) {
      delegate.children.insert(childCount++, widget)
    }
  }

  override fun verifySnapshot(container: TestFlexContainer<UIView>, name: String?) {
    val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.
    container.value.setFrame(screenSize)
    container.value.layoutIfNeeded()
    callback.verifySnapshot(container.value, name)
  }
}
