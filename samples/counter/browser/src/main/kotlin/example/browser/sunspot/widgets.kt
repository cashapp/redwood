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
package example.browser.sunspot

import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.HTMLElementChildren
import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlSunspotBox(
  override val value: HTMLElement,
) : SunspotBox<HTMLElement> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val children = HTMLElementChildren(value)
}

class HtmlSunspotText(
  override val value: HTMLSpanElement,
) : SunspotText<HTMLElement> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun text(text: String?) {
    value.textContent = text
  }
}

class HtmlSunspotButton(
  override val value: HTMLButtonElement,
) : SunspotButton<HTMLElement> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

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
