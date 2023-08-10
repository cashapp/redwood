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
package com.example.redwood.counter.browser

import app.cash.redwood.Modifier
import com.example.redwood.counter.widget.Button
import com.example.redwood.counter.widget.Text
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlText(
  override val value: HTMLSpanElement,
) : Text<HTMLElement> {
  override var modifier: Modifier = Modifier

  override fun text(text: String?) {
    value.textContent = text
  }
}

class HtmlButton(
  override val value: HTMLButtonElement,
) : Button<HTMLElement> {
  override var modifier: Modifier = Modifier

  override fun text(text: String?) {
    value.textContent = text
  }

  override fun enabled(enabled: Boolean) {
    value.disabled = !enabled
  }

  override fun onClick(onClick: (() -> Unit)?) {
    value.onclick = if (onClick != null) {
      { onClick() }
    } else {
      null
    }
  }
}
