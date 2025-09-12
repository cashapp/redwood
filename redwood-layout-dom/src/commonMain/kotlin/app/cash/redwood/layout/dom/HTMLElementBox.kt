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
package app.cash.redwood.layout.dom

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.Widget
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

internal class HTMLElementBox(
  override val value: HTMLDivElement,
) : Box<HTMLElement> {
  override val children: Widget.Children<HTMLElement> = Children(value)

  override var modifier: Modifier = Modifier

  private var horizontalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start
  private var verticalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start

  init {
    value.style.position = "relative"
    value.style.width = "100%"
    value.style.height = "100%"
  }

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

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    this.horizontalAlignment = horizontalAlignment
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    this.verticalAlignment = verticalAlignment
  }

  private inner class Children(
    private val container: HTMLElement,
    private val delegate: HTMLElementChildren = HTMLElementChildren(container),
  ) : Widget.Children<HTMLElement> by delegate {

    override val widgets: List<Widget<HTMLElement>>
      get() = delegate.widgets.map { (it as BoxChild).delegate }

    override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
      val boxChild = delegate.widgets[index] as BoxChild
      boxChild.applyModifiers()
      delegate.onModifierUpdated(index, widget)
    }

    override fun insert(index: Int, widget: Widget<HTMLElement>) {
      val boxChild = BoxChild(widget)
      boxChild.applyModifiers()
      delegate.insert(index, boxChild)
    }

    override fun detach() {
      delegate.detach()
    }
  }

  private inner class BoxChild(
    val delegate: Widget<HTMLElement>,
  ) : Widget<HTMLElement> {
    override val value: HTMLElement = (document.createElement("div") as HTMLDivElement).apply {
      style.position = "absolute"
      style.left = "0"
      style.top = "0"
      style.width = "100%"
      style.height = "100%"
      style.display = "flex"
      style.flexDirection = "column"
      appendChild(delegate.value)
    }

    override var modifier: Modifier by delegate::modifier
    override val allChildren: List<Widget.Children<HTMLElement>> by delegate::allChildren

    fun applyModifiers() {
      when (horizontalAlignment) {
        CrossAxisAlignment.Start -> {
          value.style.alignItems = "start"
        }
        CrossAxisAlignment.Center -> {
          value.style.alignItems = "center"
        }
        CrossAxisAlignment.End -> {
          value.style.alignItems = "end"
        }
        CrossAxisAlignment.Stretch -> {
          value.style.alignItems = "stretch"
        }
      }
      when (verticalAlignment) {
        CrossAxisAlignment.Start -> {
          value.style.justifyContent = "start"
          delegate.value.style.flex = "none"
        }
        CrossAxisAlignment.Center -> {
          value.style.justifyContent = "center"
          delegate.value.style.flex = "none"
        }
        CrossAxisAlignment.End -> {
          value.style.justifyContent = "end"
          delegate.value.style.flex = "none"
        }
        CrossAxisAlignment.Stretch -> {
          value.style.justifyContent = "start"
          delegate.value.style.flex = "1.0"
        }
      }
    }
  }
}
