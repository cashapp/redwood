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
package com.example.redwood.emojisearch.browser

import app.cash.redwood.Modifier
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactory
import com.example.redwood.emojisearch.widget.Image
import com.example.redwood.emojisearch.widget.Text
import com.example.redwood.emojisearch.widget.TextInput
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
  private var onChange: ((TextFieldState) -> Unit)? = null
  private var state = TextFieldState()
  private var updating = false

  init {
    value.oninput = { stateChanged() }
    value.addEventListener("selectionchange", { stateChanged() })
  }

  private fun stateChanged() {
    if (updating) return

    val newState = state.userEdit(
      text = value.value,
      selectionStart = value.selectionStart ?: 0,
      selectionEnd = value.selectionEnd ?: 0,
    )
    if (!state.contentEquals(newState)) {
      state = newState
      onChange?.invoke(newState)
    }
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    this.onChange = onChange
  }

  override fun state(state: TextFieldState) {
    if (state.userEditCount < this.state.userEditCount) return

    if (!updating) {
      updating = true
      try {
        value.value = state.text
        value.selectionStart = state.selectionStart
        value.selectionEnd = state.selectionEnd
      } finally {
        updating = false
      }
    }
  }

  override fun hint(hint: String) {
    value.placeholder = hint
  }

  override var modifier: Modifier = Modifier
}

private class HtmlText(
  override val value: HTMLSpanElement,
) : Text<HTMLElement> {
  override var modifier: Modifier = Modifier

  override fun text(text: String) {
    value.textContent = text
  }
}

private class HtmlImage(
  override val value: HTMLImageElement,
) : Image<HTMLElement> {
  override var modifier: Modifier = Modifier

  override fun url(url: String) {
    value.src = url
  }
}
