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
package app.cash.redwood.layout.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ViewGroupChildren

internal class ViewBox(
  context: Context,
) : Box<View> {

  override val value = BoxViewGroup(context)

  override var modifier: Modifier = Modifier

  override val children = ViewGroupChildren(
    value,
    insert = { index, view ->
      value.addView(view, index)
    },
    remove = { index, count ->
      value.removeViews(index, count)
    },
  )

  override fun width(width: Constraint) {
    value.widthConstraint = width

  }

  override fun height(height: Constraint) {
    value.heightConstraint = height
  }

  override fun margin(margin: Margin) {
    value.margin = margin
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.horizontalAlignment = horizontalAlignment
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.verticalAlignment = verticalAlignment
  }

  internal class BoxViewGroup(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    internal var widthConstraint: Constraint = Constraint.Wrap
    internal var heightConstraint: Constraint = Constraint.Wrap

    internal var margin: Margin = Margin.Zero

    internal var horizontalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start
    internal var verticalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      // TODO: Determine the width and height based on constraints and container.
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      // TODO: Figure out layout logic here for children

      requestLayout()
    }
  }
}


