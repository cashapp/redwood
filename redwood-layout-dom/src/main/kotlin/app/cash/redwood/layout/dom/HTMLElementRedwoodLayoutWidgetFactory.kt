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
package app.cash.redwood.layout.dom

import app.cash.redwood.LayoutModifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.widget.HTMLElementChildren
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class HTMLElementRedwoodLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLayoutWidgetFactory<HTMLElement> {
  override fun Column(): Column<HTMLElement> =
    HTMLFlexContainer(
      value = document.createElement("div") as HTMLDivElement,
      direction = "column",
      overflowSetter = { style.overflowY = it },
    )

  override fun Row(): Row<HTMLElement> =
    HTMLFlexContainer(
      value = document.createElement("div") as HTMLDivElement,
      direction = "row",
      overflowSetter = { style.overflowX = it },
    )

  override fun Spacer(): Spacer<HTMLElement> =
    HTMLSpacer(
      value = document.createElement("div") as HTMLDivElement,
    )
}

private class HTMLFlexContainer(
  override val value: HTMLDivElement,
  direction: String,
  private val overflowSetter: HTMLDivElement.(String) -> Unit,
) : Row<HTMLElement>, Column<HTMLElement> {
  init {
    value.style.display = "flex"
    value.style.flexDirection = direction
  }

  override val children = HTMLElementChildren(value)

  override fun width(width: Constraint) {
    // TODO Determine how to map to CSS.
  }

  override fun height(height: Constraint) {
    // TODO Determine how to map to CSS.
  }

  override fun padding(padding: Padding) {
    value.style.apply {
      paddingLeft = unitsToPx(padding.start)
      paddingRight = unitsToPx(padding.end)
      paddingTop = unitsToPx(padding.top)
      paddingBottom = unitsToPx(padding.bottom)
    }
  }

  override fun overflow(overflow: Overflow) {
    value.overflowSetter(
      when (overflow) {
        Overflow.Clip -> "hidden"
        Overflow.Scroll -> "scroll"
        else -> throw AssertionError()
      },
    )
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    value.style.justifyContent = horizontalAlignment.toCss()
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.style.alignItems = horizontalAlignment.toCss()
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    value.style.justifyContent = verticalAlignment.toCss()
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.style.alignItems = verticalAlignment.toCss()
  }

  private fun MainAxisAlignment.toCss() = when (this) {
    MainAxisAlignment.Start -> "start"
    MainAxisAlignment.Center -> "center"
    MainAxisAlignment.End -> "end"
    MainAxisAlignment.SpaceBetween -> "space-between"
    MainAxisAlignment.SpaceAround -> "space-around"
    MainAxisAlignment.SpaceEvenly -> "space-evenly"
    else -> throw AssertionError()
  }

  private fun CrossAxisAlignment.toCss() = when (this) {
    CrossAxisAlignment.Start -> "start"
    CrossAxisAlignment.Center -> "center"
    CrossAxisAlignment.End -> "end"
    CrossAxisAlignment.Stretch -> "stretch"
    else -> throw AssertionError()
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
}

private class HTMLSpacer(
  override val value: HTMLDivElement,
) : Spacer<HTMLElement> {
  override fun width(width: Int) {
    value.style.width = unitsToPx(width)
  }

  override fun height(height: Int) {
    value.style.height = unitsToPx(height)
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier
}
