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
package com.example.redwood.counter.android.views

import android.widget.Button as WidgetButton
import android.view.View
import app.cash.redwood.LayoutModifier
import com.example.redwood.counter.widget.Button

internal class AndroidButton(
  override val value: WidgetButton,
) : Button<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun text(text: String?) {
    value.text = text
  }

  override fun enabled(enabled: Boolean) {
    value.isEnabled = enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    value.setOnClickListener(
      if (onClick != null) {
        { onClick() }
      } else {
        null
      }
    )
  }
}
