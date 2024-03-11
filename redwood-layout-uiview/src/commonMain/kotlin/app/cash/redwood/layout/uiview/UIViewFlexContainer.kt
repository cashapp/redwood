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
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import kotlinx.cinterop.convert
import platform.UIKit.UIView
import platform.darwin.NSInteger

internal class UIViewFlexContainer(
  direction: FlexDirection,
) : YogaFlexContainer<UIView>, ChangeListener {
  private val yogaView: YogaUIView = YogaUIView(
    applyModifier = { node, index ->
      node.applyModifier(children.widgets[index].modifier, Density.Default)
    },
  )
  override val rootNode: Node get() = yogaView.rootNode
  override val density: Density get() = Density.Default
  override val value: UIView get() = yogaView
  override val children = UIViewChildren(
    parent = value,
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
  }

  override fun width(width: Constraint) {
    yogaView.width = width
  }

  override fun height(height: Constraint) {
    yogaView.height = height
  }

  override fun overflow(overflow: Overflow) {
    yogaView.scrollEnabled = overflow == Overflow.Scroll
  }

  override fun onEndChanges() {
    value.invalidateIntrinsicContentSize() // Tell the enclosing view that our size changed.
    value.setNeedsLayout() // Update layout of subviews.
  }
}

private fun UIView.asNode(): Node {
  val childNode = Node()
  childNode.measureCallback = UIViewMeasureCallback(this)
  return childNode
}
