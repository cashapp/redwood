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
package com.example.redwood.testing.browser

import com.example.redwood.testing.widget.Button
import com.example.redwood.testing.widget.TestSchemaWidgetFactory
import com.example.redwood.testing.widget.Text
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlWidgetFactory(
  private val document: Document,
) : TestSchemaWidgetFactory<HTMLElement> {
  override fun Text(): Text<HTMLElement> {
    val span = document.createElement("span") as HTMLSpanElement
    return HtmlText(span)
  }

  override fun Button(): Button<HTMLElement> {
    val button = document.createElement("button") as HTMLButtonElement
    return HtmlButton(button)
  }

  override fun TextInput() = TODO()
  override fun Button2() = TODO()

  override fun ScopedTestRow() = throw UnsupportedOperationException()

  override fun TestRow() = throw UnsupportedOperationException()
}
