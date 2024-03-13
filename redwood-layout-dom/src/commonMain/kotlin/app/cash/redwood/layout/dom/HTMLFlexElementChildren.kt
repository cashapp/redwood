/*
 * Copyright (C) 2024 Square, Inc.
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

import app.cash.redwood.layout.modifier.Margin
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.Widget
import org.w3c.dom.HTMLElement

public class HTMLFlexElementChildren(
  private val container: HTMLElement,
  private val delegate: HTMLElementChildren = HTMLElementChildren(container),
) :
  Widget.Children<HTMLElement> by delegate {
  override fun onModifierUpdated(index: Int, widget: Widget<HTMLElement>) {
    delegate.onModifierUpdated(index, widget)
    widget.applyModifiers()
  }

  override fun insert(index: Int, widget: Widget<HTMLElement>) {
    delegate.insert(index, widget)
    widget.applyModifiers()
  }

  private fun Widget<HTMLElement>.applyModifiers() {
    modifier.forEach { element ->
      when (element) {
        is Margin -> {
          value.style.apply {
            marginInlineStart = element.margin.start.toPxString()
            marginInlineEnd = element.margin.end.toPxString()
            marginTop = element.margin.top.toPxString()
            marginBottom = element.margin.bottom.toPxString()
          }
        }
      }
    }
  }
}
