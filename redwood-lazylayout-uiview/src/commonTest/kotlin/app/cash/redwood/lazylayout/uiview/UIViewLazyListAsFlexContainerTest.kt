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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.Text
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.toUIColor
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UILabel
import platform.UIKit.UIView

class UIViewLazyListAsFlexContainerTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractFlexContainerTest<UIView>() {
  private val widgetFactory = UIViewRedwoodLazyLayoutWidgetFactory()

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ) = ViewTestFlexContainer(widgetFactory.LazyList(), direction, backgroundColor)

  override fun row() = UIViewRedwoodLayoutWidgetFactory().Row()

  override fun column() = UIViewRedwoodLayoutWidgetFactory().Column()

  override fun text(): Text<UIView> = object : Text<UIView>, ResizableWidget<UIView> {
    override val value = UILabel().apply {
      numberOfLines = 0
      textColor = platform.UIKit.UIColor.blackColor
    }
    override var modifier: Modifier = Modifier
    override val measureCount = 0
    override var sizeListener: ResizableWidget.SizeListener? = null

    override fun text(text: String) {
      value.text = text
      sizeListener?.invalidateSize()
    }

    override fun bgColor(color: Int) {
      value.backgroundColor = color.toUIColor()
    }
  }

  override fun verifySnapshot(widget: UIView, name: String?) {
    val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.
    widget.setFrame(screenSize)

    // Snapshot the container on a white background.
    val frame = UIView().apply {
      backgroundColor = platform.UIKit.UIColor.whiteColor
      setFrame(screenSize)
      addSubview(widget)
      layoutIfNeeded()
    }

    // Unfortunately even with animations forced off, UITableView's animation system breaks
    // synchronous snapshots. The simplest workaround is to delay snapshots one frame.
    callback.verifySnapshot(frame, name, delay = 1.milliseconds.toDouble(DurationUnit.SECONDS))
    widget.removeFromSuperview()
  }

  class ViewTestFlexContainer private constructor(
    private val delegate: LazyList<UIView>,
  ) : TestFlexContainer<UIView>,
    LazyList<UIView> by delegate {
    private var childCount = 0
    private var onScroll: ((Px) -> Unit)? = null

    constructor(delegate: LazyList<UIView>, direction: FlexDirection, backgroundColor: Int) : this(
      delegate.apply {
        isVertical(direction == FlexDirection.Column)
        value.backgroundColor = backgroundColor.toUIColor()
      },
    )

    override val children: Widget.Children<UIView> = delegate.items

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

    override fun add(widget: Widget<UIView>) {
      addAt(childCount, widget)
    }

    override fun addAt(index: Int, widget: Widget<UIView>) {
      delegate.items.insert(index, widget)
      childCount++
    }

    override fun removeAt(index: Int) {
      delegate.items.remove(index = index, count = 1)
      childCount--
    }

    override fun onEndChanges() {
      (delegate as ChangeListener).onEndChanges()
    }
  }
}
