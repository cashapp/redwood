/*
 * Copyright (C) 2021 Square, Inc.
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
package example.browser.counter

import example.counter.widget.CounterBox
import example.counter.widget.CounterButton
import example.counter.widget.CounterText
import example.counter.widget.CounterWidgetFactory
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

object HtmlCounterNodeFactory : CounterWidgetFactory<HTMLElement> {
  override fun CounterBox(parent: HTMLElement): CounterBox<HTMLElement> {
    val div = parent.ownerDocument!!.createElement("div") as HTMLDivElement
    return HtmlCounterBox(div)
  }

  override fun CounterText(
    parent: HTMLElement,
  ): CounterText<HTMLElement> {
    val span = parent.ownerDocument!!.createElement("span") as HTMLSpanElement
    return HtmlCounterText(span)
  }

  override fun CounterButton(
    parent: HTMLElement,
  ): CounterButton<HTMLElement> {
    val button = parent.ownerDocument!!.createElement("button") as HTMLButtonElement
    return HtmlCounterButton(button)
  }
}
