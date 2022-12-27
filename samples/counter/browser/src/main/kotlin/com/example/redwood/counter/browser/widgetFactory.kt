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
package com.example.redwood.counter.browser

import com.example.redwood.counter.widget.Box
import com.example.redwood.counter.widget.Button
import com.example.redwood.counter.widget.SchemaWidgetFactory
import com.example.redwood.counter.widget.Text
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlWidgetFactory(
  private val document: Document,
) : SchemaWidgetFactory<HTMLElement> {
  override fun Box(): Box<HTMLElement> {
    val div = document.createElement("div") as HTMLDivElement
    return HtmlBox(div)
  }

  override fun Text(): Text<HTMLElement> {
    val span = document.createElement("span") as HTMLSpanElement
    return HtmlText(span)
  }

  override fun Button(): Button<HTMLElement> {
    val button = document.createElement("button") as HTMLButtonElement
    return HtmlButton(button)
  }
}
