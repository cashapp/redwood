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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

// TODO(swankjesse): GENERATE THIS.

@OptIn(
  ExperimentalCoroutinesApi::class,
  RedwoodCodegenApi::class,
)
fun testEmojiSearch(testBody: suspend RedwoodTester.() -> Unit) = runTest {
  val changeTracker = ChangeTracker()

  val provider = EmojiSearchWidgetFactories(
    EmojiSearch = MutableEmojiSearchWidgetFactory(changeTracker),
    RedwoodLayout = MutableRedwoodLayoutWidgetFactory(changeTracker),
    RedwoodTreehouseLazyLayout = MutableRedwoodTreehouseLazyLayoutWidgetFactory(changeTracker),
  )

  val redwoodTester = RedwoodTester(
    scope = this@runTest,
    changeTracker = changeTracker,
    provider = provider,
  )

  try {
    redwoodTester.testBody()
  } finally {
    redwoodTester.cancel()
  }
}

@RedwoodCodegenApi
private class MutableEmojiSearchWidgetFactory(
  val changeTracker: ChangeTracker,
) : EmojiSearchWidgetFactory<MutableWidget> {
  override fun TextInput(): TextInput<MutableWidget> = MutableTextInput(changeTracker)

  override fun Text(): Text<MutableWidget> = MutableText(changeTracker)

  override fun Image(): Image<MutableWidget> = MutableImage(changeTracker)
}

@RedwoodCodegenApi
private class MutableTextInput(
  private val changeTracker: ChangeTracker,
) : TextInput<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier
    set(value) {
      field = value
      changeTracker.widgetChanged()
    }

  var state: TextFieldState? = null
  var hint: String? = null
  var onChange: ((TextFieldState) -> Unit)? = null

  override fun state(state: TextFieldState) {
    this.state = state
    changeTracker.widgetChanged()
  }

  override fun hint(hint: String) {
    this.hint = hint
    changeTracker.widgetChanged()
  }

  override fun onChange(onChange: ((TextFieldState) -> Unit)?) {
    this.onChange = onChange
    changeTracker.widgetChanged()
  }

  override fun snapshot() = TextInputValue(layoutModifiers, state, hint, onChange)
}

@RedwoodCodegenApi
private class MutableText(
  val changeTracker: ChangeTracker,
) : Text<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier
    set(value) {
      field = value
      changeTracker.widgetChanged()
    }

  var text: String? = null

  override fun text(text: String) {
    this.text = text
    changeTracker.widgetChanged()
  }

  override fun snapshot() = TextValue(layoutModifiers, text)
}

@RedwoodCodegenApi
private class MutableImage(
  val changeTracker: ChangeTracker,
) : Image<MutableWidget>, MutableWidget {
  override val value: MutableWidget
    get() = this

  override var layoutModifiers: LayoutModifier = LayoutModifier
    set(value) {
      field = value
      changeTracker.widgetChanged()
    }

  var url: String? = null

  override fun url(url: String) {
    this.url = url
    changeTracker.widgetChanged()
  }

  override fun snapshot() = ImageValue(layoutModifiers, url)
}

@RedwoodCodegenApi
private class MutableRedwoodLayoutWidgetFactory(
  val changeTracker: ChangeTracker,
) : RedwoodLayoutWidgetFactory<MutableWidget> {
  override fun Column(): Column<MutableWidget> = error("TODO")

  override fun Row(): Row<MutableWidget> = error("TODO")
}

@RedwoodCodegenApi
private class MutableRedwoodTreehouseLazyLayoutWidgetFactory(
  val changeTracker: ChangeTracker,
) : RedwoodTreehouseLazyLayoutWidgetFactory<MutableWidget> {
  override fun LazyColumn(): LazyColumn<MutableWidget> = error("TODO")
}

class TextInputValue(
  val layoutModifiers: LayoutModifier = LayoutModifier,
  val state: TextFieldState? = null,
  val hint: String? = null,
  /** Note lambdas are omitted from equals() and hashCode(). */
  val onChange: ((TextFieldState) -> Unit)? = {}
) : WidgetValue {
  override fun equals(other: Any?): Boolean {
    return other is TextInputValue
      && other.layoutModifiers == layoutModifiers
      && other.state == state
      && other.hint == hint
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
    return other is TextValue
      && other.layoutModifiers == layoutModifiers
      && other.text == text
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
    return other is ImageValue
      && other.layoutModifiers == layoutModifiers
      && other.url == url
  }

  override fun hashCode(): Int {
    return listOf(layoutModifiers, url).hashCode()
  }

  override fun toString(): String {
    return "ImageValue(layoutModifiers=$layoutModifiers, url=$url)"
  }
}


