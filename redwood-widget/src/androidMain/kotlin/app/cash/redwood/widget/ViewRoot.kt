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
package app.cash.redwood.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children as viewGroupChildren
import app.cash.redwood.Modifier

/**
 * A default base implementation of [RedwoodView.Root] suitable for subclassing.
 *
 * This view contributes nothing to the view hierarchy. It forwards all measurement and layout calls
 * from its own parent view to its child views.
 */
public open class ViewRoot(
  context: Context,
) : ViewGroup(context),
  RedwoodView.Root<View> {
  final override val children: Widget.Children<View> = ViewGroupChildren(this)
  final override val value: View
    get() = this
  final override var modifier: Modifier = Modifier

  override fun contentState(
    loadCount: Int,
    attached: Boolean,
    uncaughtException: Throwable?,
  ) {
  }

  override fun restart(restart: (() -> Unit)?) {
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var maxWidth = 0
    var maxHeight = 0
    for (child in viewGroupChildren) {
      child.measure(widthMeasureSpec, heightMeasureSpec)
      maxWidth = maxOf(maxWidth, child.measuredWidth)
      maxHeight = maxOf(maxHeight, child.measuredHeight)
    }
    setMeasuredDimension(maxWidth, maxHeight)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    for (child in viewGroupChildren) {
      child.layout(0, 0, right - left, bottom - top)
    }
  }
}
