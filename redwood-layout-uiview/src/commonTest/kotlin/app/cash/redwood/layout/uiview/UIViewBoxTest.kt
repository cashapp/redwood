/*
 * Copyright (C) 2024 Square, Inc.
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

import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.Color
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.widget.Box
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView

class UIViewBoxTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractBoxTest<UIView>() {

  override fun box(): Box<UIView> {
    return UIViewBox().apply {
      value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 0.0, alpha = 0.5)
    }
  }

  override fun color(): Color<UIView> {
    return UIViewColor()
  }

  override fun text(): Text<UIView> {
    return UIViewText()
  }

  override fun verifySnapshot(value: UIView) {
    val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.

    // Snapshot the container on a white background.
    val frame = UIView().apply {
      backgroundColor = UIColor.whiteColor
      setFrame(screenSize)
      addSubview(value)
      layoutIfNeeded()
    }

    callback.verifySnapshot(frame, null)
    value.removeFromSuperview()
  }
}
