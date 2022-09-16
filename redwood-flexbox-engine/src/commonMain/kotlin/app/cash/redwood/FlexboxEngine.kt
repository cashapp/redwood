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

package app.cash.redwood

import kotlin.js.JsName

/**
 * Create a new [FlexboxEngine] instance.
 */
@JsName("newFlexboxEngine")
public fun FlexboxEngine(): FlexboxEngine = RealFlexboxEngine()

/**
 * A class that measures and positions its children according to flexbox properties.
 */
public interface FlexboxEngine {

  /**
   * The flex direction attribute of the flexbox.
   */
  public var flexDirection: FlexDirection

  /**
   * The flex wrap attribute of the flexbox.
   */
  public var flexWrap: FlexWrap

  /**
   * The justify content attribute of the flexbox.
   */
  public var justifyContent: JustifyContent

  /**
   * The align content attribute of the flexbox.
   */
  public var alignContent: AlignContent

  /**
   * The align items attribute of the flexbox.
   */
  public var alignItems: AlignItems

  /**
   * The padding of the flexbox.
   */
  public var padding: Spacing

  /**
   * The current value of the maximum number of flex lines.
   * If not set, [Undefined] is returned.
   */
  public var maxLines: Int

  /**
   * Returns the nodes contained in the flexbox.
   */
  public val nodes: List<Node>

  /**
   * Adds the node to the specified index of the flexbox.
   */
  public fun addNode(node: Node, index: Int = nodes.size)

  /**
   * Removes the node at the specified index.
   */
  public fun removeNode(index: Int)

  /**
   * Removes all the nodes contained in the flexbox.
   */
  public fun removeAllNodes()

  /**
   * This is called to find out how big the flexbox should be.
   */
  public fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size

  /**
   * Place the children inside the given bounding box.
   */
  public fun layout(left: Int, top: Int, right: Int, bottom: Int)
}
