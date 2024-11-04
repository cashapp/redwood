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
package app.cash.redwood.snapshot.testing

import app.cash.redwood.Modifier
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.ResizableWidget.SizeListener
import kotlinx.cinterop.CValue
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UIScrollView
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentFill
import platform.UIKit.UIStackViewDistributionEqualSpacing
import platform.UIKit.UIView

object UIViewTestWidgetFactory : TestWidgetFactory<UIView> {
  override fun color() = UIViewColor()

  override fun text() = UIViewText()

  override fun column() = UIViewSimpleColumn()

  override fun scrollWrapper() = UIViewScrollWrapper()
}

fun Int.toUIColor(): UIColor {
  return UIColor(
    red = ((this shr 16) and 0xff) / 255.0,
    green = ((this shr 8) and 0xff) / 255.0,
    blue = (this and 0xff) / 255.0,
    alpha = ((this shr 24) and 0xff) / 255.0,
  )
}

class UIViewText :
  Text<UIView>,
  ResizableWidget<UIView> {
  override val value = object : UILabel(CGRectZero.readValue()) {
    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      measureCount++
      return super.sizeThatFits(size)
    }
  }.apply {
    numberOfLines = 0
    textColor = UIColor.blackColor
  }
  override var modifier: Modifier = Modifier

  override var sizeListener: SizeListener? = null

  override var measureCount = 0
    private set

  override fun text(text: String) {
    value.text = text
    sizeListener?.invalidateSize()
  }

  override fun bgColor(color: Int) {
    value.backgroundColor = color.toUIColor()
  }
}

class UIViewColor : Color<UIView> {
  override val value: UIView = object : UIView(CGRectZero.readValue()) {
    override fun intrinsicContentSize(): CValue<CGSize> {
      return CGSizeMake(width, height)
    }
  }
  override var modifier: Modifier = Modifier

  private var width = 0.0
  private var height = 0.0

  override fun width(width: Dp) {
    this.width = with(Density.Default) { width.toPx() }
    invalidate()
  }

  override fun height(height: Dp) {
    this.height = with(Density.Default) { height.toPx() }
    invalidate()
  }

  override fun color(color: Int) {
    value.backgroundColor = color.toUIColor()
  }

  private fun invalidate() {
    value.setFrame(CGRectMake(0.0, 0.0, width, height))
    value.invalidateIntrinsicContentSize()
  }
}

class UIViewSimpleColumn : SimpleColumn<UIView> {
  override var modifier: Modifier = Modifier

  override val value = UIStackView(CGRectZero.readValue()).apply {
    this.axis = UILayoutConstraintAxisVertical
    this.alignment = UIStackViewAlignmentFill
    this.distribution = UIStackViewDistributionEqualSpacing
  }

  override fun add(child: UIView) {
    value.addArrangedSubview(child)
  }
}

class UIViewScrollWrapper : ScrollWrapper<UIView> {
  override var modifier: Modifier = Modifier

  override val value = UIScrollView(CGRectZero.readValue())

  override var content: UIView?
    get() = value.subviews().firstOrNull() as UIView?
    set(value) {
      val scrollView = this@UIViewScrollWrapper.value
      (scrollView.subviews.firstOrNull() as UIView?)?.removeFromSuperview()
      if (value == null) return

      scrollView.addSubview(value)
      value.translatesAutoresizingMaskIntoConstraints = false
      value.leadingAnchor.constraintEqualToAnchor(scrollView.leadingAnchor).active = true
      value.widthAnchor.constraintEqualToAnchor(scrollView.widthAnchor).active = true
      value.topAnchor.constraintEqualToAnchor(scrollView.topAnchor).active = true
      value.bottomAnchor.constraintEqualToAnchor(scrollView.bottomAnchor).active = true
    }
}
