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
package app.cash.zipline.samples.emojisearch.views

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import app.cash.redwood.LayoutModifier
import example.schema.widget.Text

class ViewText(
  override val value: TextView,
) : Text<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  init {
    val tv = TypedValue()
    if (value.context.theme.resolveAttribute(R.attr.colorOnBackground, tv, true)) {
      value.setTextColor(tv.data)
    }
  }

  override fun text(text: String) {
    value.text = text
  }
}
