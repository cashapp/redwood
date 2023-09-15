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
package com.example.redwood.testing.android.views

import android.view.View
import android.widget.Button as WidgetButton
import app.cash.redwood.Modifier
import com.example.redwood.testing.widget.Button

internal class ViewButton(
  override val value: WidgetButton,
) : Button<View> {
  override var modifier: Modifier = Modifier

  override fun text(text: String?) {
    value.text = text
  }

  override fun onClick(onClick: (() -> Unit)?) {
    value.setOnClickListener(
      if (onClick != null) {
        { onClick.invoke() }
      } else {
        null
      },
    )
  }
}
