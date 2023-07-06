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
package app.cash.redwood.layout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.yoga.AlignItems
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.JustifyContent
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIView

internal class UIViewFlexContainer(
  direction: FlexDirection,
) : Row<UIView>, Column<UIView> {
  private val yogaView = YogaUIView()
  private val hostView = HostUIView()

  override val value: UIView get() = hostView
  override val children = UIViewChildren(yogaView)
  override var modifier: Modifier = Modifier

  init {
    yogaView.rootNode.flexDirection = direction
    yogaView.applyModifier = { node, index ->
      node.applyModifier(children.widgets[index].modifier, Density.Default)
    }
  }

  override fun width(width: Constraint) {
    yogaView.width = width
    invalidate()
  }

  override fun height(height: Constraint) {
    yogaView.height = height
    invalidate()
  }

  override fun margin(margin: Margin) {
    with(yogaView.rootNode) {
      with(Density.Default) {
        marginStart = margin.start.toPx().toFloat()
        marginEnd = margin.end.toPx().toFloat()
        marginTop = margin.top.toPx().toFloat()
        marginBottom = margin.bottom.toPx().toFloat()
      }
    }
    invalidate()
  }

  override fun overflow(overflow: Overflow) {
    hostView.scrollEnabled = overflow == Overflow.Scroll
    invalidate()
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignItems(horizontalAlignment.toAlignItems())
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    justifyContent(verticalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignItems(verticalAlignment.toAlignItems())
  }

  private fun alignItems(alignItems: AlignItems) {
    yogaView.rootNode.alignItems = alignItems
    invalidate()
  }

  private fun justifyContent(justifyContent: JustifyContent) {
    yogaView.rootNode.justifyContent = justifyContent
    invalidate()
  }

  private fun invalidate() {
    hostView.setNeedsLayout()
    yogaView.setNeedsLayout()
  }

  private inner class HostUIView : UIView(cValue { CGRectZero }) {
    var scrollEnabled = false
      set(new) {
        val old = field
        field = new
        if (old != new) {
          updateViewHierarchy()
        }
      }

    init {
      updateViewHierarchy()
    }

    override fun intrinsicContentSize(): CValue<CGSize> {
      return yogaView.intrinsicContentSize()
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      return yogaView.sizeThatFits(size)
    }

    override fun layoutSubviews() {
      yogaView.setFrame(frame)
      yogaView.layoutSubviews()

      if (scrollEnabled) {
        val scrollView = typedSubviews.first() as UIScrollView
        scrollView.setFrame(frame)
        scrollView.layoutSubviews()

        val nodes = yogaView.rootNode.children
        if (nodes.isEmpty()) {
          scrollView.setContentSize(CGSizeMake(0.0, 0.0))
        } else {
          var left = Float.MAX_VALUE
          var right = Float.MIN_VALUE
          var top = Float.MAX_VALUE
          var bottom = Float.MIN_VALUE
          println("START")
          for (node in nodes) {
            left = minOf(left, node.left)
            right = maxOf(right, node.left + node.width)
            top = minOf(top, node.top)
            bottom = maxOf(bottom, node.top + node.height)
            println("HERE $left $right $top $bottom")
          }
          println("END")
          val contentSize = CGSizeMake(
            width = (right - left).toDouble(),
            height = (bottom - top).toDouble(),
          )
          scrollView.setContentSize(contentSize)
        }
      }
    }

    private fun updateViewHierarchy() {
      typedSubviews.forEach { it.removeFromSuperview() }
      yogaView.removeFromSuperview()

      if (scrollEnabled) {
        addSubview(newScrollView().apply { addSubview(yogaView) })
      } else {
        addSubview(yogaView)
      }
    }

    private fun newScrollView(): UIScrollView {
      return UIScrollView().apply {
        showsHorizontalScrollIndicator = false
        showsVerticalScrollIndicator = false
      }
    }
  }
}
