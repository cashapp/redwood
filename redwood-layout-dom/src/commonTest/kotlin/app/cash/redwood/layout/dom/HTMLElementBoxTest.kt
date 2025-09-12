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
import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.snapshot.testing.HTMLElementSnapshotter
import app.cash.redwood.snapshot.testing.HTMLElementTestWidgetFactory
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.toCssColor
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

class HTMLElementBoxTest : AbstractBoxTest<HTMLElement>() {
  override val snapshotterFactory: Snapshotter.Factory<HTMLElement> =
    HTMLElementSnapshotter.Factory(Frame.Companion.Iphone14)
  override val widgetFactory: TestWidgetFactory<HTMLElement> =
    HTMLElementTestWidgetFactory()
  private val layoutWidgetFactory = HTMLElementRedwoodLayoutWidgetFactory(document)

  override fun box(): Box<HTMLElement> = layoutWidgetFactory.Box().apply {
    value.style.backgroundColor = 0x88000000.toInt().toCssColor()
    applyDefaults()
  }
}
