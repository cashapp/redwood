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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.modifier.Flex
import app.cash.redwood.layout.modifier.Grow
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Shrink
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.Widget
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

public class HTMLElementRedwoodLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLayoutWidgetFactory<HTMLElement> {
  override fun Box(): Box<HTMLElement> {
    TODO("Not yet implemented")
  }

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
) : FlexContainer<HTMLElement> {
  init {
    value.style.display = "flex"
    value.style.flexDirection = direction
  }

  override val children: Widget.Children<HTMLElement> = HTMLFlexElementChildren(value)

  override fun width(width: Constraint) {
    value.style.width = width.toCss()
  }

  override fun height(height: Constraint) {
    value.style.height = height.toCss()
  }

  override fun margin(margin: Margin) {
    value.style.apply {
      marginInlineStart = margin.start.toPxString()
      marginInlineEnd = margin.end.toPxString()
      marginTop = margin.top.toPxString()
      marginBottom = margin.bottom.toPxString()
    }
  }

  override fun overflow(overflow: Overflow) {
    value.overflowSetter(overflow.toCss())
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    value.style.alignItems = crossAxisAlignment.toCss()
  }

  override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    value.style.justifyContent = mainAxisAlignment.toCss()
  }

  override var modifier: Modifier = Modifier
}

private class HTMLSpacer(
  override val value: HTMLDivElement,
) : Spacer<HTMLElement> {
  override fun width(width: Dp) {
    value.style.width = width.toPxString()
  }

  override fun height(height: Dp) {
    value.style.height = height.toPxString()
  }

  override var modifier: Modifier = Modifier
}

private class HTMLFlexElementChildren(
  private val container: HTMLElement,
  private val delegate: HTMLElementChildren = HTMLElementChildren(container),
) :
  Widget.Children<HTMLElement> by delegate {
  override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers()
    delegate.onModifierUpdated(index, widget)
  }

  override fun insert(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers()
    delegate.insert(index, widget)
  }

  private fun Widget<HTMLElement>.applyModifiers() {
    modifier.forEach { element ->
      when (element) {
        is MarginModifier -> {
          value.style.apply {
            marginInlineStart = element.margin.start.toPxString()
            marginInlineEnd = element.margin.end.toPxString()
            marginTop = element.margin.top.toPxString()
            marginBottom = element.margin.bottom.toPxString()
          }
        }
        is Grow -> value.style.apply {
          flexGrow = element.value.toString()
        }
        is Shrink -> value.style.apply {
          flexShrink = element.value.toString()
        }
        is Flex -> value.style.apply {
          flex = element.value.toString()
        }
        is Width -> value.style.apply {
          width = element.width.toPxString()
        }
        is Height -> value.style.apply {
          height = element.height.toPxString()
        }
        is Size -> value.style.apply {
          width = element.width.toPxString()
          height = element.height.toPxString()
        }
      }
    }
  }
}
