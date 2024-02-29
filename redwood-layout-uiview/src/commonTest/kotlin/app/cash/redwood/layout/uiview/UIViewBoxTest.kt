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
import app.cash.redwood.layout.widget.Box
import kotlinx.cinterop.useContents
import platform.UIKit.UIColor
import platform.UIKit.UIView

class UIViewBoxTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractBoxTest<UIView>() {

  override fun Box(block: Box<UIView>.() -> Unit): Box<UIView> {
    val box = UIViewBox().apply(block)
    box.value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 0.0, alpha = 128.0)
    return box
  }

  override fun Color(block: Color<UIView>.() -> Unit): Color<UIView> {
    return UIViewColor().apply(block)
  }

  override fun verifySnapshot(value: UIView) {
    val container = UIView()
    container.addSubview(value)
    container.frame.useContents {
      size.width = 250.0
      size.height = 250.0
    }
    container.sizeToFit()
    callback.verifySnapshot(container, null)
  }
}
