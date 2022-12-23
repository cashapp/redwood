/*
 * Copyright (C) 2022 Square, Inc.
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
package com.example.redwood.counter.android.views

import android.widget.Button as WidgetButton
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.redwood.counter.widget.Box
import com.example.redwood.counter.widget.Button
import com.example.redwood.counter.widget.SchemaWidgetFactory
import com.example.redwood.counter.widget.Text

class AndroidWidgetFactory(
  private val context: Context,
) : SchemaWidgetFactory<View> {
  override fun Box(): Box<View> {
    val view = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
    }
    return AndroidBox(view)
  }

  override fun Text(): Text<View> {
    val view = TextView(context)
    return AndroidText(view)
  }

  override fun Button(): Button<View> {
    val view = WidgetButton(context)
    return AndroidButton(view)
  }
}
