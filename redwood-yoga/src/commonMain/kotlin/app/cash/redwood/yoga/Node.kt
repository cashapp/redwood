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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.Yoga
import app.cash.redwood.yoga.internal.enums.YGEdge

@RedwoodYogaApi
public class Node internal constructor(
  internal val native: YGNode,
) {
  public constructor() : this(Yoga.YGNodeNew())

  // Inputs
  public val children: MutableList<Node> = Children()
  public val owner: Node?
    get() = native.owner?.let(::Node)
  public var direction: Direction
    get() = native.style.direction().toDirection()
    set(value) = Yoga.YGNodeStyleSetDirection(native, value.toYoga())
  public var flexDirection: FlexDirection
    get() = native.style.flexDirection().toFlexDirection()
    set(value) = Yoga.YGNodeStyleSetFlexDirection(native, value.toYoga())
  public var justifyContent: JustifyContent
    get() = native.style.justifyContent().toJustifyContent()
    set(value) = Yoga.YGNodeStyleSetJustifyContent(native, value.toYoga())
  public var alignItems: AlignItems
    get() = native.style.alignItems().toAlignItems()
    set(value) = Yoga.YGNodeStyleSetAlignItems(native, value.toYoga())
  public var alignSelf: AlignSelf
    get() = native.style.alignSelf().toAlignSelf()
    set(value) = Yoga.YGNodeStyleSetAlignSelf(native, value.toYoga())
  public var flexGrow: Float
    get() = Yoga.YGNodeStyleGetFlexGrow(native)
    set(value) = Yoga.YGNodeStyleSetFlexGrow(native, value)
  public var flexShrink: Float
    get() = Yoga.YGNodeStyleGetFlexShrink(native)
    set(value) = Yoga.YGNodeStyleSetFlexShrink(native, value)
  public var flexBasis: Float
    get() = Yoga.YGNodeStyleGetFlexBasisPercent(native)
    set(value) = if (value >= 0) {
      Yoga.YGNodeStyleSetFlexBasisPercent(native, value)
    } else {
      Yoga.YGNodeStyleSetFlexBasisAuto(native)
    }
  public var marginStart: Float
    get() = getMargin(YGEdge.YGEdgeStart)
    set(value) = setMargin(YGEdge.YGEdgeStart, value)
  public var marginEnd: Float
    get() = getMargin(YGEdge.YGEdgeEnd)
    set(value) = setMargin(YGEdge.YGEdgeEnd, value)
  public var marginTop: Float
    get() = getMargin(YGEdge.YGEdgeTop)
    set(value) = setMargin(YGEdge.YGEdgeTop, value)
  public var marginBottom: Float
    get() = getMargin(YGEdge.YGEdgeBottom)
    set(value) = setMargin(YGEdge.YGEdgeBottom, value)
  public var requestedWidth: Float
    get() = Yoga.YGNodeStyleGetWidth(native).value
    set(value) = Yoga.YGNodeStyleSetWidth(native, value)
  public var requestedHeight: Float
    get() = Yoga.YGNodeStyleGetHeight(native).value
    set(value) = Yoga.YGNodeStyleSetHeight(native, value)
  public var requestedMinWidth: Float
    get() = Yoga.YGNodeStyleGetMinWidth(native).value
    set(value) = Yoga.YGNodeStyleSetMinWidth(native, value)
  public var requestedMinHeight: Float
    get() = Yoga.YGNodeStyleGetMinHeight(native).value
    set(value) = Yoga.YGNodeStyleSetMinHeight(native, value)
  public var requestedMaxWidth: Float
    get() = Yoga.YGNodeStyleGetMaxWidth(native).value
    set(value) = Yoga.YGNodeStyleSetMaxWidth(native, value)
  public var requestedMaxHeight: Float
    get() = Yoga.YGNodeStyleGetMaxHeight(native).value
    set(value) = Yoga.YGNodeStyleSetMaxHeight(native, value)
  public var measureCallback: MeasureCallback?
    get() = (native.measure.noContext as MeasureCallbackCompat?)?.callback
    set(value) = Yoga.YGNodeSetMeasureFunc(native, value?.let(::MeasureCallbackCompat))

  // Outputs
  public val left: Float
    get() = Yoga.YGNodeLayoutGetLeft(native)
  public val top: Float
    get() = Yoga.YGNodeLayoutGetTop(native)
  public val width: Float
    get() = Yoga.YGNodeLayoutGetWidth(native)
  public val height: Float
    get() = Yoga.YGNodeLayoutGetHeight(native)

  public fun measure(parentWidth: Float, parentHeight: Float) {
    // TODO: Figure out how to measure incrementally safely.
    native.markDirtyAndPropogateDownwards()

    Yoga.YGNodeCalculateLayout(
      node = native,
      ownerWidth = parentWidth,
      ownerHeight = parentHeight,
      ownerDirection = native.style.direction(),
    )
  }

  private fun getMargin(edge: YGEdge): Float {
    if (owner != null) {
      return Yoga.YGNodeStyleGetMargin(native, edge).value
    } else {
      return Yoga.YGNodeStyleGetPadding(native, edge).value
    }
  }

  private fun setMargin(edge: YGEdge, value: Float) {
    if (owner != null) {
      Yoga.YGNodeStyleSetMargin(native, edge, value)
    } else {
      Yoga.YGNodeStyleSetPadding(native, edge, value)
    }
  }

  override fun toString(): String {
    return "Node($native)"
  }

  private inner class Children : AbstractMutableList<Node>() {
    override val size: Int
      get() = native.children.size

    override fun add(index: Int, element: Node) {
      Yoga.YGNodeInsertChild(native, element.native, index)
    }

    override fun get(index: Int): Node {
      return Node(native.children[index])
    }

    override fun removeAt(index: Int): Node {
      val removed = native.children[index]
      Yoga.YGNodeRemoveChild(native, removed)
      return Node(removed)
    }

    override fun set(index: Int, element: Node): Node {
      val replaced = native.children[index]
      Yoga.YGNodeSwapChild(native, element.native, index)
      return Node(replaced)
    }
  }
}
