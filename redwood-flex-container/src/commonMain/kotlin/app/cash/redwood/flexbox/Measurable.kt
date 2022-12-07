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
package app.cash.redwood.flexbox

public open class Measurable {
  /**
   * The requested width of the item.
   *
   * The attribute is about how wide the view wants to be. Can be one of the
   * constants [MatchParent] or [WrapContent], or an exact size.
   */
  public open val requestedWidth: Double = WrapContent

  /**
   * The requested height of the item.
   *
   * The attribute is about how wide the view wants to be. Can be one of the
   * constants [MatchParent] or [WrapContent], or an exact size.
   */
  public open val requestedHeight: Double = WrapContent

  /**
   * The minimum width attribute of the item.
   *
   * The attribute determines the minimum width the child can shrink to.
   */
  public open val minWidth: Double = 0.0

  /**
   * The minimum height attribute of the item.
   *
   * The attribute determines the minimum height the child can shrink to.
   */
  public open val minHeight: Double = 0.0

  /**
   * The maximum width attribute of the item.
   *
   * The attribute determines the maximum width the child can expand to.
   */
  public open val maxWidth: Double = Double.MAX_VALUE

  /**
   * The maximum height attribute of the item.
   *
   * The attribute determines the maximum height the child can expand to.
   */
  public open val maxHeight: Double = Double.MAX_VALUE

  /**
   * Return the item's width given a fixed [height].
   */
  public open fun width(height: Double): Double {
    return measure(Constraints.fixedHeight(height)).width
  }

  /**
   * Return the item's height given a fixed [width].
   */
  public open fun height(width: Double): Double {
    return measure(Constraints.fixedWidth(width)).height
  }

  public open fun measure(constraints: Constraints): Size {
    return Size(
      width = constraints.constrainWidth(requestedWidth),
      height = constraints.constrainHeight(requestedHeight),
    )
  }

  public companion object {
    /**
     * A special constant for [requestedWidth] or [requestedHeight] that means that the item wants
     * to be as big as its parent.
     */
    public const val MatchParent: Double = -1.0

    /**
     * A special constant for [requestedWidth] or [requestedHeight] that the item wants to be just
     * large enough to fit its own internal content, taking its own padding into account.
     */
    public const val WrapContent: Double = -2.0
  }
}
