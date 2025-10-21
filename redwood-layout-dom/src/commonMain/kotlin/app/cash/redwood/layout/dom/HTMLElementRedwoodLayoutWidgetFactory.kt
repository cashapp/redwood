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

import app.cash.redwood.layout.widget.Box
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

public class HTMLElementRedwoodLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLayoutWidgetFactory<HTMLElement> {
  override fun Box(): Box<HTMLElement> = HTMLElementBox(value = document.createElement("div") as HTMLDivElement)

  override fun Column(): Column<HTMLElement> = HTMLFlexContainer(
    value = document.createElement("div") as HTMLDivElement,
    direction = "column",
    overflowSetter = { overflowY = it },
  )

  override fun Row(): Row<HTMLElement> = HTMLFlexContainer(
    value = document.createElement("div") as HTMLDivElement,
    direction = "row",
    overflowSetter = { overflowX = it },
  )

  override fun Spacer(): Spacer<HTMLElement> = HTMLSpacer(document.createElement("div") as HTMLDivElement)
}
