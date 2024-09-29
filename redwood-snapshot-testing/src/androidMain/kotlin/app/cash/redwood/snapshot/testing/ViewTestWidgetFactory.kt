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
package app.cash.redwood.snapshot.testing

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import app.cash.redwood.Modifier
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp

class ViewTestWidgetFactory(
  private val context: Context,
) : TestWidgetFactory<View> {
  override fun color() = ViewColor(context)

  override fun text() = ViewText(context)
}

class ViewText(context: Context) : Text<View> {
  override val value = object : TextView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      measureCount++
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
  }.apply {
    textSize = 18f
    textDirection = View.TEXT_DIRECTION_LOCALE
    gravity = Gravity.CENTER_VERTICAL
    setTextColor(android.graphics.Color.BLACK)
  }
  override var modifier: Modifier = Modifier

  override var measureCount = 0
    private set

  override fun text(text: String) {
    value.text = text
  }

  override fun bgColor(color: Int) {
    value.setBackgroundColor(color)
  }
}

class ViewColor(context: Context) : Color<View> {
  private val density = Density(context.resources)
  override val value = object : View(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      setMeasuredDimension(minimumWidth, minimumHeight)
    }
  }
  override var modifier: Modifier = Modifier

  override fun width(width: Dp) {
    value.minimumWidth = with(density) {
      width.toPxInt()
    }
  }

  override fun height(height: Dp) {
    value.minimumHeight = with(density) {
      height.toPxInt()
    }
  }

  override fun color(color: Int) {
    value.setBackgroundColor(color)
  }
}
