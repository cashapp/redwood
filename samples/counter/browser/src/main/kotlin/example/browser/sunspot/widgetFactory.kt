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
package example.browser.sunspot

import example.sunspot.widget.SunspotBox
import example.sunspot.widget.SunspotButton
import example.sunspot.widget.SunspotText
import example.sunspot.widget.SunspotWidgetFactory
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement

class HtmlSunspotNodeFactory(
  private val document: Document,
) : SunspotWidgetFactory<HTMLElement>() {
  override fun SunspotBox(): SunspotBox<HTMLElement> {
    val div = document.createElement("div") as HTMLDivElement
    return HtmlSunspotBox(div)
  }

  override fun SunspotText(): SunspotText<HTMLElement> {
    val span = document.createElement("span") as HTMLSpanElement
    return HtmlSunspotText(span)
  }

  override fun SunspotButton(): SunspotButton<HTMLElement> {
    val button = document.createElement("button") as HTMLButtonElement
    return HtmlSunspotButton(button)
  }
}
