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
package example.browser

import app.cash.redwood.LayoutModifier
import example.schema.widget.EmojiSearchWidgetFactory
import example.schema.widget.Image
import example.schema.widget.Text
import example.schema.widget.TextInput
import example.values.TextFieldState
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement

class HTMLElementEmojiSearchWidgetFactory(private val document: Document) : EmojiSearchWidgetFactory<HTMLElement> {
  override fun TextInput(): TextInput<HTMLElement> = HtmlTextInput(document.createElement("input") as HTMLInputElement)
  override fun Text(): Text<HTMLElement> = HtmlText(document.createElement("span") as HTMLSpanElement)
  override fun Image(): Image<HTMLElement> = HtmlImage(document.createElement("img") as HTMLImageElement)
}

private class HtmlTextInput(
  override val value: HTMLInputElement,
) : TextInput<HTMLElement> {
  // TODO This user edit logic is not correct!
  private var userEditCount = 0

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    if (onChange != null) {
      value.oninput = { event ->
        val state = TextFieldState(value.value, userEditCount++)
        onChange(state)
      }
    } else {
      value.onchange = null
    }
  }

  override fun state(state: TextFieldState) {
    if (state.userEditCount < userEditCount) return
    value.value = state.text
  }

  override fun hint(hint: String) {
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
}

private class HtmlText(
  override val value: HTMLSpanElement,
) : Text<HTMLElement> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun text(text: String) {
    value.textContent = text
  }
}

private class HtmlImage(
  override val value: HTMLImageElement,
) : Image<HTMLElement> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun url(url: String) {
    value.src = url
  }
}
