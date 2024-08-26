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

import app.cash.redwood.layout.AbstractLazyListTest
import app.cash.redwood.lazylayout.uiview.UIViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.widget.Widget
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView

class UIViewLazyListTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractLazyListTest<UIView>() {
  private val widgetFactory = UIViewRedwoodLazyLayoutWidgetFactory()

  override fun text() = UIViewText()

  override fun lazyList(backgroundColor: Int) = widgetFactory.LazyList()

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

    // Unfortunately even with animations forced off, UITableView's animation system breaks
    // synchronous snapshots. The simplest workaround is to delay snapshots one frame.
    callback.verifySnapshot(frame, name, delay = 1.milliseconds.toDouble(DurationUnit.SECONDS))
    container.value.removeFromSuperview()
  }
}
