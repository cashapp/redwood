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
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.snapshot.testing.HTMLElementSnapshotter
import app.cash.redwood.snapshot.testing.HTMLElementTestWidgetFactory
import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.toCssColor
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class HTMLElementFlexContainerTest : AbstractFlexContainerTest<HTMLElement>() {
  override val snapshotterFactory: Snapshotter.Factory<HTMLElement> =
    HTMLElementSnapshotter.Factory(Frame.Companion.Iphone14)
  override val widgetFactory: TestWidgetFactory<HTMLElement> =
    HTMLElementTestWidgetFactory()
  private val layoutWidgetFactory = HTMLElementRedwoodLayoutWidgetFactory(document)

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): TestFlexContainer<HTMLElement> {
    return HTMLElementTestFlexContainer(
      delegate = HTMLFlexContainer(
        value = document.createElement("div") as HTMLDivElement,
        direction = when (direction) {
          FlexDirection.Companion.Row, FlexDirection.Companion.RowReverse -> "row"
          FlexDirection.Companion.Column, FlexDirection.Companion.ColumnReverse -> "column"
          else -> error("unexpected direction: $direction")
        },
        overflowSetter = { overflowX = it },
      ),
    ).apply {
      value.style.backgroundColor = backgroundColor.toCssColor()
    }
  }

  override fun row() = layoutWidgetFactory.Row()
  override fun column() = layoutWidgetFactory.Column()
  override fun spacer() = layoutWidgetFactory.Spacer()
}

private class HTMLElementTestFlexContainer(
  private val delegate: HTMLFlexContainer,
) : TestFlexContainer<HTMLElement>,
  Widget<HTMLElement> by delegate {
  override val value: HTMLElement
    get() = delegate.value
  override val children: Widget.Children<HTMLElement>
    get() = delegate.children
  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) =
    delegate.crossAxisAlignment(crossAxisAlignment)
  override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) =
    delegate.mainAxisAlignment(mainAxisAlignment)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun overflow(overflow: Overflow) = delegate.overflow(overflow)
  override fun onScroll(onScroll: ((Px) -> Unit)?) = delegate.onScroll(onScroll)
  override fun onEndChanges() = Unit
  override fun scroll(offset: Px) {
    delegate.value.scrollTop = offset.value
  }
}
