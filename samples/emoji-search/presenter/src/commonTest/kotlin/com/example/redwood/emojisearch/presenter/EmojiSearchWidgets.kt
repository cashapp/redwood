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
package com.example.redwood.emojisearch.presenter

import app.cash.redwood.LayoutModifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.compose.testing.MutableWidget
import app.cash.redwood.compose.testing.RedwoodTester
import app.cash.redwood.compose.testing.WidgetValue
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import app.cash.redwood.treehouse.lazylayout.widget.RedwoodTreehouseLazyLayoutWidgetFactory
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactories
import com.example.redwood.emojisearch.widget.EmojiSearchWidgetFactory
import com.example.redwood.emojisearch.widget.Image
import com.example.redwood.emojisearch.widget.Text
import com.example.redwood.emojisearch.widget.TextInput
import example.values.TextFieldState
import kotlinx.coroutines.CoroutineScope

// TODO(swankjesse): GENERATE THIS.

@OptIn(RedwoodCodegenApi::class)
fun EmojiSearchTester(scope: CoroutineScope): RedwoodTester {
  return RedwoodTester(
    scope = scope,
    provider = EmojiSearchWidgetFactories(
      EmojiSearch = MutableEmojiSearchWidgetFactory(),
      RedwoodLayout = MutableRedwoodLayoutWidgetFactory(),
      RedwoodTreehouseLazyLayout = MutableRedwoodTreehouseLazyLayoutWidgetFactory(),
    ),
  )
}

@RedwoodCodegenApi
private class MutableEmojiSearchWidgetFactory : EmojiSearchWidgetFactory<MutableWidget> {
  override fun TextInput(): TextInput<MutableWidget> = MutableTextInput()

  override fun Text(): Text<MutableWidget> = MutableText()

  override fun Image(): Image<MutableWidget> = MutableImage()
}

@RedwoodCodegenApi
private class MutableTextInput : TextInput<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier

  var state: TextFieldState? = null
  var hint: String? = null
  var onChange: ((TextFieldState) -> Unit)? = null

  override fun state(state: TextFieldState) {
    this.state = state
  }

  override fun hint(hint: String) {
    this.hint = hint
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    this.onChange = onChange
  }

  override fun snapshot() = TextInputValue(layoutModifiers, state, hint, onChange)
}

@RedwoodCodegenApi
private class MutableText : Text<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier

  var text: String? = null

  override fun text(text: String) {
    this.text = text
  }

  override fun snapshot() = TextValue(layoutModifiers, text)
}

@RedwoodCodegenApi
private class MutableImage : Image<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier

  var url: String? = null

  override fun url(url: String) {
    this.url = url
  }

  override fun snapshot() = ImageValue(layoutModifiers, url)
}

@RedwoodCodegenApi
private class MutableRedwoodLayoutWidgetFactory : RedwoodLayoutWidgetFactory<MutableWidget> {
  override fun Column(): Column<MutableWidget> = error("TODO")

  override fun Row(): Row<MutableWidget> = error("TODO")
}

@RedwoodCodegenApi
private class MutableRedwoodTreehouseLazyLayoutWidgetFactory :
  RedwoodTreehouseLazyLayoutWidgetFactory<MutableWidget> {
  override fun LazyColumn(): LazyColumn<MutableWidget> = error("TODO")
}

class TextInputValue(
  val layoutModifiers: LayoutModifier = LayoutModifier,
  val state: TextFieldState? = null,
  val hint: String? = null,
  /** Note lambdas are omitted from equals() and hashCode(). */
  val onChange: ((TextFieldState) -> Unit)? = {},
) : WidgetValue {
  override fun equals(other: Any?): Boolean {
    return other is TextInputValue &&
      other.layoutModifiers == layoutModifiers &&
      other.state == state &&
      other.hint == hint
  }

  override fun hashCode(): Int {
    return listOf(layoutModifiers, state, hint).hashCode()
  }

  override fun toString(): String {
    return "TextValue(layoutModifiers=$layoutModifiers, state=$state, hint=$hint)"
  }
}

class TextValue(
  val layoutModifiers: LayoutModifier = LayoutModifier,
  val text: String? = null,
) : WidgetValue {
  override fun equals(other: Any?): Boolean {
    return other is TextValue &&
      other.layoutModifiers == layoutModifiers &&
      other.text == text
  }

  override fun hashCode(): Int {
    return listOf(layoutModifiers, text).hashCode()
  }

  override fun toString(): String {
    return "TextValue(layoutModifiers=$layoutModifiers, text=$text)"
  }
}

class ImageValue(
  val layoutModifiers: LayoutModifier = LayoutModifier,
  val url: String? = null,
) : WidgetValue {
  override fun equals(other: Any?): Boolean {
    return other is ImageValue &&
      other.layoutModifiers == layoutModifiers &&
      other.url == url
  }

  override fun hashCode(): Int {
    return listOf(layoutModifiers, url).hashCode()
  }

  override fun toString(): String {
    return "ImageValue(layoutModifiers=$layoutModifiers, url=$url)"
  }
}
