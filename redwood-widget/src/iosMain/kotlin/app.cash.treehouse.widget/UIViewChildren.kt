/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.widget

import kotlinx.cinterop.convert
import platform.UIKit.UIView
import platform.UIKit.insertSubview
import platform.UIKit.removeFromSuperview
import platform.UIKit.subviews
import platform.darwin.NSInteger

public class UIViewChildren(
  private val root: UIView,
) : Widget.Children<UIView> {
  override fun insert(index: Int, widget: UIView) {
    root.insertSubview(widget, index.convert<NSInteger>())
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    val views = Array(count) {
      val subview = root.subviews[fromIndex] as UIView
      subview.removeFromSuperview()
      subview
    }

    val newIndex = if (toIndex > fromIndex) {
      toIndex - count
    } else {
      toIndex
    }
    views.forEachIndexed { offset, view ->
      root.insertSubview(view, (newIndex + offset).convert<NSInteger>())
    }
  }

  override fun remove(index: Int, count: Int) {
    repeat(count) {
      (root.subviews[index] as UIView).removeFromSuperview()
    }
  }

  override fun clear() {
    for (subview in root.subviews) {
      (subview as UIView).removeFromSuperview()
    }
  }
}
