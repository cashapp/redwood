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
package app.cash.redwood.lazylayout.uiview

import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.lazylayout.toUIColor
import app.cash.redwood.snapshot.testing.UIViewSnapshotCallback
import app.cash.redwood.snapshot.testing.UIViewSnapshotter
import app.cash.redwood.snapshot.testing.UIViewTestWidgetFactory
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Px
import app.cash.redwood.yoga.FlexDirection
import platform.UIKit.UIView

class UIViewLazyListAsFlexContainerTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractFlexContainerTest<UIView>() {
  override val widgetFactory = UIViewTestWidgetFactory

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): TestFlexContainer<UIView> {
    return ViewTestFlexContainer(UIViewLazyList(), direction, backgroundColor)
      .apply { applyDefaults() }
  }

  override fun row() = UIViewRedwoodLayoutWidgetFactory().Row()
    .apply { applyDefaults() }

  override fun column() = UIViewRedwoodLayoutWidgetFactory().Column()
    .apply { applyDefaults() }

  override fun spacer(backgroundColor: Int): Spacer<UIView> {
    return UIViewRedwoodLayoutWidgetFactory().Spacer()
      .apply {
        value.backgroundColor = backgroundColor.toUIColor()
      }
  }

  override fun snapshotter(widget: UIView) = UIViewSnapshotter.framed(callback, widget)

  private class ViewTestFlexContainer private constructor(
    private val delegate: UIViewLazyList,
  ) : TestFlexContainer<UIView> {
    private var onScroll: ((Px) -> Unit)? = null

    constructor(delegate: UIViewLazyList, direction: FlexDirection, backgroundColor: Int) : this(
      delegate.apply {
        isVertical(direction == FlexDirection.Column)
        value.backgroundColor = backgroundColor.toUIColor()
      },
    )

    override val value get() = delegate.value
    override var modifier by delegate::modifier
    override val children get() = delegate.items
    override fun width(width: Constraint) = delegate.width(width)
    override fun height(height: Constraint) = delegate.height(height)
    override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(crossAxisAlignment)
    override fun margin(margin: Margin) = delegate.margin(margin)

    override fun onScroll(onScroll: ((Px) -> Unit)?) {
      this.onScroll = onScroll
    }

    override fun scroll(offset: Px) {
      onScroll?.invoke(offset)
    }

    override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    }

    override fun overflow(overflow: Overflow) {
    }

    override fun onEndChanges() = delegate.onEndChanges()
  }
}
