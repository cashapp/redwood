/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.snapshot.testing

import app.cash.redwood.Modifier
import app.cash.redwood.ui.Dp
import kotlinx.browser.document
import kotlinx.dom.appendText
import kotlinx.dom.clear
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

class HTMLElementTestWidgetFactory : TestWidgetFactory<HTMLElement> {
  override val toolkitId: ToolkitId
    get() = ToolkitId.Html

  override fun color(): Color<HTMLElement> = HTMLElementColor()

  override fun text(): Text<HTMLElement> = HTMLElementText()

  override fun column(): SimpleColumn<HTMLElement> = HTMLElementSimpleColumn()

  override fun scrollWrapper(): ScrollWrapper<HTMLElement> = HTMLScrollWrapper()
}

private class HTMLElementColor : Color<HTMLElement> {
  override val value = document.createElement("div") as HTMLDivElement

  override var modifier: Modifier = Modifier

  override fun width(width: Dp) {
    value.style.width = "${width.value}px"
  }

  override fun height(height: Dp) {
    value.style.height = "${height.value}px"
  }

  override fun color(color: Int) {
    value.style.backgroundColor = color.toCssColor()
  }
}

private class HTMLElementText : Text<HTMLElement> {
  override val value = (document.createElement("div") as HTMLDivElement)
    .apply {
      // Honor '\n' in the source string.
      style.whiteSpace = "pre-wrap"
      // Vertically center the text in the frame.
      style.display = "flex"
      style.alignItems = "center"
    }

  override var modifier: Modifier = Modifier

  override val measureCount = 0

  override fun text(text: String) {
    value.clear()
    value.appendText(text)
  }

  override fun bgColor(color: Int) {
    value.style.backgroundColor = color.toCssColor()
  }
}

private class HTMLElementSimpleColumn : SimpleColumn<HTMLElement> {
  override val value = (document.createElement("div") as HTMLDivElement)
    .apply {
      style.display = "flex"
      style.flexDirection = "column"
    }

  override var modifier: Modifier = Modifier

  override fun add(child: HTMLElement) {
    value.appendChild(child)
  }
}

private class HTMLScrollWrapper : ScrollWrapper<HTMLElement> {
  override val value = (document.createElement("div") as HTMLDivElement)
    .apply {
      style.overflowY = "scroll"
      style.display = "flex"
      style.flexDirection = "column"
    }

  override var modifier: Modifier = Modifier

  override var content: HTMLElement?
    get() = when (value.children.length) {
      1 -> value.children[0] as HTMLElement
      else -> null
    }
    set(value) {
      this.value.clear()
      if (value != null) {
        this.value.appendChild(value)
      }
    }
}
