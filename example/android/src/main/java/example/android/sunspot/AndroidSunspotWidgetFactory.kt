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
package example.android.sunspot

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory

class AndroidSunspotWidgetFactory(
  private val context: Context,
) : SunspotWidgetFactory<View> {
  override fun SunspotBox(): SunspotBox<View> {
    val view = LinearLayout(context)
    return AndroidSunspotBox(view)
  }

  override fun SunspotText(): SunspotText<View> {
    val view = TextView(context)
    return AndroidSunspotText(view)
  }

  override fun SunspotButton(): SunspotButton<View> {
    val view = Button(context)
    return AndroidSunspotButton(view)
  }
}
