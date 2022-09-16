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

/**
 * An interface that has the common behavior as a node contained in a flexbox.
 */
public interface Node {

  /**
   * The width attribute of the node.
   *
   * The attribute is about how wide the view wants to be. Can be one of the
   * constants [MatchParent] or [WrapContent], or an exact size.
   */
  public val width: Int

  /**
   * The height attribute of the node.
   *
   * The attribute is about how wide the view wants to be. Can be one of the
   * constants [MatchParent] or [WrapContent], or an exact size.
   */
  public val height: Int

  /**
   * The minimum width attribute of the node.
   *
   * The attribute determines the minimum width the child can shrink to.
   */
  public val minWidth: Int

  /**
   * The minimum height attribute of the node.
   *
   * The attribute determines the minimum height the child can shrink to.
   */
  public val minHeight: Int

  /**
   * The maximum width attribute of the node.
   *
   * The attribute determines the maximum width the child can expand to.
   */
  public val maxWidth: Int

  /**
   * The maximum height attribute of the node.
   */
  public val maxHeight: Int

  /**
   * True if this item is visible and should be laid out.
   */
  public val visible: Boolean

  /**
   * The baseline used for [AlignItems.Baseline] and [AlignSelf.Baseline].
   * If not specified, -1 is set as a default value.
   */
  public val baseline: Int

  /**
   * The order attribute of the node.
   *
   * The attribute can change the order in which the children are laid out.
   * By default, children are displayed and laid out in the same order as they added to the
   * [FlexboxEngine]. If not specified, [DefaultOrder] is set as a default value.
   */
  public val order: Int

  /**
   * The flex grow attribute of the node.
   *
   * The attribute determines how much this child will grow if positive free space is
   * distributed relative to the rest of other nodes included in the same flex line.
   * If not specified, [DefaultFlexGrow] is set as a default value.
   */
  public val flexGrow: Float

  /**
   * The flex shrink attribute of the node.
   *
   * The attribute determines how much this child will shrink if negative free space is
   * distributed relative to the rest of other nodes included in the same flex line.
   * If not specified, [DefaultFlexShrink] is set as a default value.
   */
  public val flexShrink: Float

  /**
   * The flexBasisPercent attribute of the node.
   *
   * The attribute determines the initial node length in a fraction format relative to its
   * parent.
   * The initial main size of this child View is trying to be expanded as the specified
   * fraction against the parent main size.
   * If this value is set, the length specified from layout_width
   * (or layout_height) is overridden by the calculated value from this attribute.
   * This attribute is only effective when the parent's MeasureSpec mode is
   * MeasureSpec.EXACTLY. The default value is -1, which means not set.
   */
  public val flexBasisPercent: Float

  /**
   * The align self attribute of the node.
   *
   * The attribute determines the alignment along the cross axis (perpendicular to the
   * main axis). The alignment in the same direction can be determined by the
   * align items attribute in the parent, but if this is set to other than
   * [AlignSelf.Auto], the cross axis alignment is overridden for this child.
   * The value needs to be one of the values in ([AlignSelf.Auto],
   * [AlignItems.Stretch], [AlignItems.FlexStart], [AlignItems.FlexEnd],
   * [AlignItems.Center], or [AlignItems.Baseline]).
   * If not specified, [AlignSelf.Auto] is set as a default value.
   */
  public val alignSelf: AlignSelf

  /**
   * The wrapBefore attribute of the node.
   *
   * The attribute forces a flex line wrapping. i.e. if this is set to `true` for a
   * node, the item will become the first item of the new flex line. (A wrapping happens
   * regardless of the nodes being processed in the the previous flex line)
   * This attribute is ignored if the flex_wrap attribute is set as nowrap.
   * The equivalent attribute isn't defined in the original CSS Flexible Box Module
   * specification, but having this attribute is useful for Android developers to flatten
   * the layouts when building a grid like layout or for a situation where developers want
   * to put a new flex line to make a semantic difference from the previous one, etc.
   */
  public val wrapBefore: Boolean

  /**
   * The margin of the node.
   */
  public val margin: Spacing

  /**
   * The measured width after invoking [measure].
   */
  public val measuredWidth: Int

  /**
   * The measured height after invoking [measure].
   */
  public val measuredHeight: Int

  /**
   * This is called to find out how big this node should be.
   */
  public fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec)

  /**
   * Place the node inside the given bounding box.
   */
  public fun layout(left: Int, top: Int, right: Int, bottom: Int)

  public companion object {
    /**
     * A special constant for [width] or [height] that means that the node wants to be as
     * big as its parent.
     */
    public const val MatchParent: Int = -1

    /**
     * A special constant for [width] or [height] that the node wants to be just large enough
     * to fit its own internal content, taking its own padding into account.
     */
    public const val WrapContent: Int = -2

    /** The default value for the order attribute */
    public const val DefaultOrder: Int = 1

    /** The default value for the flex grow attribute */
    public const val DefaultFlexGrow: Float = 0f

    /** The default value for the flex shrink attribute */
    public const val DefaultFlexShrink: Float = 1f

    /** The value representing the flex shrink attribute is not set */
    public const val UndefinedFlexShrink: Float = 0f

    /** The default value for the flex basis percent attribute */
    public const val DefaultFlexBasisPercent: Float = -1f
  }
}
