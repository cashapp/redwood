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

import app.cash.redwood.dom.testing.Frame
import app.cash.redwood.layout.AbstractSpacerTest
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.snapshot.testing.HTMLElementSnapshotter
import app.cash.redwood.widget.Widget
import kotlinx.browser.document
import kotlinx.dom.appendText
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class HTMLElementSpacerTest : AbstractSpacerTest<HTMLElement>() {

  override val snapshotterFactory = HTMLElementSnapshotter.Factory(Frame.None)

  override fun widget(): Spacer<HTMLElement> = HTMLElementRedwoodLayoutWidgetFactory(document).Spacer()

  override fun wrap(widget: Widget<HTMLElement>, horizontal: Boolean): HTMLElement {
    return (document.createElement("div") as HTMLDivElement)
      .apply {
        style.display = "flex"
        style.flexDirection = when {
          horizontal -> "row"
          else -> "column"
        }
        appendText("Text 1")
        append(widget.value)
        appendText("Text 2")
      }
  }
}
