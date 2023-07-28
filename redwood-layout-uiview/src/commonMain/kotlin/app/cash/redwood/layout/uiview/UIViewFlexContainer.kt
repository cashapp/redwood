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
import app.cash.redwood.layout.widget.FlexContainer
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import kotlinx.cinterop.convert
import platform.UIKit.UIView
import platform.darwin.NSInteger

internal class UIViewFlexContainer(
  private val direction: FlexDirection,
) : FlexContainer<UIView>, ChangeListener {
  private val yogaView = YogaUIView()
  override val value: UIView get() = yogaView
  override val children = UIViewChildren(
    value,
    insert = { view, index ->
      yogaView.rootNode.children.add(index, view.asNode())
      value.insertSubview(view, index.convert<NSInteger>())
    },
    remove = { index, count ->
      yogaView.rootNode.children.remove(index, count)
      Array(count) {
        value.typedSubviews[index].also(UIView::removeFromSuperview)
      }
    },
  )
  override var modifier: Modifier = Modifier

  init {
    yogaView.rootNode.flexDirection = direction
    yogaView.applyModifier = { node, index ->
      node.applyModifier(children.widgets[index].modifier, Density.Default)
    }
  }

  override fun width(width: Constraint) {
    yogaView.width = width
  }

  override fun height(height: Constraint) {
    yogaView.height = height
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
  }

  override fun overflow(overflow: Overflow) {
    yogaView.scrollEnabled = overflow == Overflow.Scroll
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    yogaView.rootNode.alignItems = crossAxisAlignment.toAlignItems()
  }

  override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    yogaView.rootNode.justifyContent = mainAxisAlignment.toJustifyContent()
  }

  override fun onEndChanges() {
    value.setNeedsLayout()
  }
}

private fun UIView.asNode(): Node {
  val childNode = Node()
  childNode.measureCallback = UIViewMeasureCallback(this)
  return childNode
}
