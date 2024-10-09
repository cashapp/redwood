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
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.Widget
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

public class HTMLElementRedwoodLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLayoutWidgetFactory<HTMLElement> {
  override fun Box(): Box<HTMLElement> =
    HTMLBoxContainer(
      value = document.createElement("div") as HTMLDivElement,
    )

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

private class HTMLBoxContainer(
  override val value: HTMLDivElement
) : Box<HTMLElement> {
  override val children: Widget.Children<HTMLElement> = HTMLBoxChildren(value)

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

  // TODO: Make CrossAxisAlignment.Stretch work
  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    value.style.justifyContent = horizontalAlignment.toCss()
  }

  // TODO: Make CrossAxisAlignment.Stretch work
  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    value.style.alignItems = verticalAlignment.toCss()
  }

  override var modifier: Modifier = Modifier
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

  private var scrollEventListener: EventListener? = null

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

  override fun onScroll(onScroll: ((Px) -> Unit)?) {
    scrollEventListener?.let { eventListener ->
      value.removeEventListener("scroll", eventListener)
      scrollEventListener = null
    }

    if (onScroll != null) {
      val eventListener = object : EventListener {
        override fun handleEvent(event: Event) {
          val offset = when (value.style.flexDirection) {
            "row" -> value.scrollTop
            "column" -> value.scrollLeft
            else -> throw AssertionError()
          }
          onScroll(Px(offset))
        }
      }.also { scrollEventListener = it }
      value.addEventListener("scroll", eventListener)
    }
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

private class HTMLBoxChildren(
  private val container: HTMLElement,
  private val delegate: HTMLElementChildren = HTMLElementChildren(container),
) : Widget.Children<HTMLElement> by delegate {
  override fun insert(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers(clearStyles = false)
    // Use absolute positioning as a hack to make items overlap
    widget.value.style.position = "absolute"
    delegate.insert(index, widget)
  }

  override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers(clearStyles = true)
    delegate.onModifierUpdated(index, widget)
  }

  private fun Widget<HTMLElement>.applyModifiers(clearStyles: Boolean) {
    if (clearStyles) {
      value.removeModifierStyles()
    }

    modifier.forEachScoped { element ->
      when (element) {
        is MarginModifier -> {
          element.applyTo(value)
        }
      }
    }
  }
}

private class HTMLFlexElementChildren(
  private val container: HTMLElement,
  private val delegate: HTMLElementChildren = HTMLElementChildren(container),
) : Widget.Children<HTMLElement> by delegate {
  override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers(clearStyles = true)
    delegate.onModifierUpdated(index, widget)
  }

  override fun insert(index: Int, widget: Widget<HTMLElement>) {
    widget.applyModifiers(clearStyles = false)
    delegate.insert(index, widget)
  }

  private fun Widget<HTMLElement>.applyModifiers(clearStyles: Boolean) {
    if (clearStyles) {
      value.removeModifierStyles()
    }

    modifier.forEachScoped { element ->
      when (element) {
        is MarginModifier -> {
          element.applyTo(value)
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

  override fun detach() {
    delegate.detach()
  }
}

private fun MarginModifier.applyTo(element: HTMLElement) {
  val theMargin = margin
  with (margin) {
    element.style.apply {
      marginInlineStart = theMargin.start.toPxString()
      marginInlineEnd = theMargin.end.toPxString()
      marginTop = theMargin.top.toPxString()
      marginBottom = theMargin.bottom.toPxString()
    }
  }
}

private fun HTMLElement.removeModifierStyles() {
  style.apply {
    removeProperty("margin-inline-start")
    removeProperty("margin-inline-end")
    removeProperty("margin-top")
    removeProperty("margin-bottom")
    removeProperty("flex-grow")
    removeProperty("flex-shrink")
    removeProperty("flex")
    removeProperty("width")
    removeProperty("height")
  }
}
