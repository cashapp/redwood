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
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.HTMLElementChildren
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

// TODO Finish these bindings and move into a `redwood-layout-html` module.
class HTMLElementRedwoodLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLayoutWidgetFactory<HTMLElement> {
  override fun Column(): Column<HTMLElement> = HtmlColumn(document.createElement("div") as HTMLDivElement)

  override fun Row(): Row<HTMLElement> = HtmlRow(document.createElement("div") as HTMLDivElement)
}

private class HtmlRow(
  override val value: HTMLDivElement,
) : Row<HTMLElement> {
  init {
    value.style.display = "flex"
    value.style.flexDirection = "row"
  }

  override val children = HTMLElementChildren(value)

  override fun width(width: Constraint) {
  }

  override fun height(height: Constraint) {
  }

  override fun padding(padding: Padding) {
  }

  override fun overflow(overflow: Overflow) {
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
}

private class HtmlColumn(
  override val value: HTMLDivElement,
) : Column<HTMLElement> {
  init {
    value.style.display = "flex"
    value.style.flexDirection = "column"
  }

  override val children = HTMLElementChildren(value)

  override fun width(width: Constraint) {
  }

  override fun height(height: Constraint) {
  }

  override fun padding(padding: Padding) {
  }

  override fun overflow(overflow: Overflow) {
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
}
