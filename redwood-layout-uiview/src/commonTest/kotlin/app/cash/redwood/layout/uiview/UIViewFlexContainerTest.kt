/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.layout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.layout.AbstractFlexContainerTest
import app.cash.redwood.layout.TestFlexContainer
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.snapshot.testing.UIViewSnapshotCallback
import app.cash.redwood.snapshot.testing.UIViewSnapshotter
import app.cash.redwood.snapshot.testing.UIViewTestWidgetFactory
import app.cash.redwood.snapshot.testing.toUIColor
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.ResizableWidget.SizeListener
import app.cash.redwood.widget.Widget
import app.cash.redwood.yoga.FlexDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIScrollView
import platform.UIKit.UIView

class UIViewFlexContainerTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractFlexContainerTest<UIView>() {
  override val widgetFactory = UIViewTestWidgetFactory

  override fun flexContainer(
    direction: FlexDirection,
    backgroundColor: Int,
  ): UIViewTestFlexContainer {
    return UIViewTestFlexContainer(UIViewFlexContainer(direction)).apply {
      value.backgroundColor = backgroundColor.toUIColor()

      // Install a default SizeListener that doesn't do anything. Otherwise the test subject
      // benefits from fallback behavior that it might not get in other containers.
      sizeListener = object : SizeListener {
        override fun invalidateSize() {
        }
      }

      (this as TestFlexContainer<*>).applyDefaults()
    }
  }

  override fun row() = flexContainer(FlexDirection.Row)

  override fun column() = flexContainer(FlexDirection.Column)

  override fun spacer(backgroundColor: Int): Spacer<UIView> {
    return UIViewRedwoodLayoutWidgetFactory().Spacer()
      .apply {
        value.backgroundColor = backgroundColor.toUIColor()
      }
  }

  class UIViewTestFlexContainer internal constructor(
    private val delegate: UIViewFlexContainer,
  ) : TestFlexContainer<UIView>,
    ResizableWidget<UIView>,
    YogaFlexContainer<UIView> by delegate,
    ChangeListener by delegate {
    override var sizeListener: SizeListener? by delegate::sizeListener

    override val children: Widget.Children<UIView> = delegate.children

    init {
      value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 1.0, alpha = 0.2)
    }

    override fun onScroll(onScroll: ((Px) -> Unit)?) {
      delegate.onScroll(onScroll)
    }

    override fun scroll(offset: Px) {
      (delegate.value as UIScrollView).setContentOffset(cValue { y = offset.value }, false)
    }
  }

  override fun snapshotter(widget: UIView) = UIViewSnapshotter.framed(callback, widget)

  /**
   * Confirm that calling [ResizableWidget.SizeListener] is sufficient to trigger a subsequent call
   * to [UIView.layoutSubviews].
   */
  @Test
  fun testInvalidateSizeTriggersUIViewLayout() {
    var layoutSubviewsCount = 0

    val view = object : UIView(CGRectZero.readValue()) {
      override fun sizeThatFits(size: CValue<CGSize>) = CGSizeMake(10.0, 10.0)

      override fun layoutSubviews() {
        layoutSubviewsCount++
        super.layoutSubviews()
      }
    }

    val widget = object : ResizableWidget<UIView> {
      override val value = view
      override var modifier: Modifier = Modifier
      override var sizeListener: SizeListener? = null
    }

    val container = flexContainer(FlexDirection.Column).apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      crossAxisAlignment(CrossAxisAlignment.Start)
      add(widget)
    }

    val snapshotter = snapshotter(container.value)

    snapshotter.layoutSubject()
    assertEquals(1, layoutSubviewsCount)

    widget.sizeListener?.invalidateSize()

    snapshotter.layoutSubject()
    assertEquals(2, layoutSubviewsCount)
  }
}
