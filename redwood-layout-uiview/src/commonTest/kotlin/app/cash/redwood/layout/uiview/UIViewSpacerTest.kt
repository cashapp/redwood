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

import app.cash.redwood.layout.AbstractSpacerTest
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UILabel
import platform.UIKit.UILayoutConstraintAxisHorizontal
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UILayoutFittingCompressedSize
import platform.UIKit.UIStackView
import platform.UIKit.UIView

interface UIViewSpacerTestCallback {
  fun verifySnapshot(view: UIView)
}

class UIViewSpacerTest(
  private val callback: UIViewSpacerTestCallback,
) : AbstractSpacerTest<UIView>() {
  private val factory = UIViewRedwoodLayoutWidgetFactory()

  override fun widget() = factory.Spacer()

  override fun wrap(widget: Widget<UIView>, horizontal: Boolean): UIView {
    return UIStackView().apply {
      axis = if (horizontal) UILayoutConstraintAxisHorizontal else UILayoutConstraintAxisVertical
      addArrangedSubview(UILabel().apply { text = "Text 1" })
      addArrangedSubview(widget.value)
      addArrangedSubview(UILabel().apply { text = "Text 2" })

      // Force the frame to match the smallest size which fits the contents.
      val size = systemLayoutSizeFittingSize(UILayoutFittingCompressedSize.readValue())
      setFrame(
        size.useContents {
          CGRectMake(0.0, 0.0, width, height)
        },
      )
    }
  }

  override fun verifySnapshot(value: UIView) {
    callback.verifySnapshot(value)
  }
}
