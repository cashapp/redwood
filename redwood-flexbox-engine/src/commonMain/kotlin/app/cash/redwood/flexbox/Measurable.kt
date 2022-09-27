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
  public open val width: Int = WrapContent

  /**
   * The requested height of the item.
   *
   * The attribute is about how wide the view wants to be. Can be one of the
   * constants [MatchParent] or [WrapContent], or an exact size.
   */
  public open val height: Int = WrapContent

  /**
   * The minimum width attribute of the item.
   *
   * The attribute determines the minimum width the child can shrink to.
   */
  public open val minWidth: Int = 0

  /**
   * The minimum height attribute of the item.
   *
   * The attribute determines the minimum height the child can shrink to.
   */
  public open val minHeight: Int = 0

  /**
   * The maximum width attribute of the item.
   *
   * The attribute determines the maximum width the child can expand to.
   */
  public open val maxWidth: Int = Int.MAX_VALUE

  /**
   * The maximum height attribute of the item.
   */
  public open val maxHeight: Int = Int.MAX_VALUE

  public open fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    return Size(
      width = MeasureSpec.resolveSize(width, widthSpec),
      height = MeasureSpec.resolveSize(height, heightSpec),
    )
  }

  public companion object {
    /**
     * A special constant for [width] or [height] that means that the item wants to be as big as its
     * parent.
     */
    public const val MatchParent: Int = -1

    /**
     * A special constant for [width] or [height] that the item wants to be just large enough to fit
     * its own internal content, taking its own padding into account.
     */
    public const val WrapContent: Int = -2
  }
}
