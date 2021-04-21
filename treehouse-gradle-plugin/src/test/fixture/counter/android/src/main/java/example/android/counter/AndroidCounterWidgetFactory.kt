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
package example.android.counter

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.counter.widget.CounterBox
import example.counter.widget.CounterButton
import example.counter.widget.CounterText
import example.counter.widget.CounterWidgetFactory

class AndroidCounterWidgetFactory(
  private val context: Context,
) : CounterWidgetFactory<View> {
  override fun CounterBox(): CounterBox<View> {
    val view = LinearLayout(context)
    return AndroidCounterBox(view)
  }

  override fun CounterText(): CounterText<View> {
    val view = TextView(context)
    return AndroidCounterText(view)
  }

  override fun CounterButton(): CounterButton<View> {
    val view = Button(context)
    return AndroidCounterButton(view)
  }
}
