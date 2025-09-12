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
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Margin as MarginElement
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.Widget
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

internal class HTMLElementBox(
  override val value: HTMLDivElement,
) : Box<HTMLElement>,
  ChangeListener {
  private val _children = Children(value)
  override val children: Widget.Children<HTMLElement>
    get() = _children

  override var modifier: Modifier = Modifier

  private var horizontalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start
  private var verticalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start
  private var margin: Margin = Margin.Zero
  private var hasModifierChanges = false

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
    this.margin = margin
    this.hasModifierChanges = true
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    this.horizontalAlignment = horizontalAlignment
    this.hasModifierChanges = true
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    this.verticalAlignment = verticalAlignment
    this.hasModifierChanges = true
  }

  override fun onEndChanges() {
    if (!hasModifierChanges) return
    hasModifierChanges = false

    _children.applyModifiers()
  }

  /** Wrap each child element in a [BoxChild]. */
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

    fun applyModifiers() {
      for (boxChild in _children.delegate.widgets) {
        (boxChild as BoxChild).applyModifiers()
      }
    }
  }

  /**
   * This fills the enclosing box. It positions its one child element in the right place using a
   * single trivial flex column.
   */
  private inner class BoxChild(
    val delegate: Widget<HTMLElement>,
  ) : Widget<HTMLElement> {
    override val value: HTMLElement = (document.createElement("div") as HTMLDivElement).apply {
      // Fill the enclosing box.
      style.position = "absolute"
      style.left = "0"
      style.top = "0"
      style.boxSizing = "border-box"
      style.width = "100%"
      style.height = "100%"

      // Use a flex column to position the wrapped child.
      style.display = "flex"
      style.flexDirection = "column"
      appendChild(delegate.value)
    }

    override var modifier: Modifier by delegate::modifier
    override val allChildren: List<Widget.Children<HTMLElement>> by delegate::allChildren

    fun applyModifiers() {
      var margin = Margin.Zero
      var horizontalAlignment = horizontalAlignment
      var verticalAlignment = verticalAlignment
      var requestedWidth: Dp? = null
      var requestedHeight: Dp? = null

      modifier.forEach { element ->
        when (element) {
          is MarginElement -> margin = element.margin
          is HorizontalAlignment -> horizontalAlignment = element.alignment
          is VerticalAlignment -> verticalAlignment = element.alignment
          is Width -> requestedWidth = element.width
          is Height -> requestedHeight = element.height
          is Size -> {
            requestedWidth = element.width
            requestedHeight = element.height
          }
          else -> Unit
        }
      }

      // Use padding on this element to effect margins on the child element.
      val totalMargin = (this@HTMLElementBox.margin.plus(margin))
      with(Density(1.0)) {
        value.style.paddingLeft = totalMargin.start.toPxString()
        value.style.paddingRight = totalMargin.end.toPxString()
        value.style.paddingTop = totalMargin.top.toPxString()
        value.style.paddingBottom = totalMargin.bottom.toPxString()
      }

      // Size the child directly.
      // TODO: we have a fight between this code that reaches into the delegate, and what the
      //     delegate has requested for itself.
      if (requestedWidth != null) {
        delegate.value.style.width = requestedWidth.toPxString()
      }
      if (requestedHeight != null) {
        delegate.value.style.height = requestedHeight.toPxString()
      }

      // Use flex column to position the child.
      value.style.alignItems = when (horizontalAlignment) {
        CrossAxisAlignment.Start -> "start"
        CrossAxisAlignment.Center -> "center"
        CrossAxisAlignment.End -> "end"
        CrossAxisAlignment.Stretch -> "stretch"
        else -> "start"
      }
      value.style.justifyContent = when (verticalAlignment) {
        CrossAxisAlignment.Start -> "start"
        CrossAxisAlignment.Center -> "center"
        CrossAxisAlignment.End -> "end"
        else -> "start"
      }
      delegate.value.style.flex = when (verticalAlignment) {
        CrossAxisAlignment.Stretch -> "1.0"
        else -> "none"
      }
    }
  }
}
