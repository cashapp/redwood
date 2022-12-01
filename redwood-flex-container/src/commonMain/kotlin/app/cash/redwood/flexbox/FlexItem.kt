/*
 * Copyright 2016 Google Inc. All rights reserved.
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

/**
 * An item with properties that can be measured and laid out inside a [FlexContainer].
 */
public class FlexItem(
  /**
   * True if this item is visible and should be laid out.
   */
  public val visible: Boolean = true,

  /**
   * The baseline used for [AlignItems.Baseline] and [AlignSelf.Baseline].
   */
  public val baseline: Int = DefaultBaseline,

  /**
   * The flex grow attribute of the item.
   *
   * The attribute determines how much this child will grow if positive free space is
   * distributed relative to the rest of other items included in the same flex line.
   */
  public val flexGrow: Double = DefaultFlexGrow,

  /**
   * The flex shrink attribute of the item.
   *
   * The attribute determines how much this child will shrink if negative free space is
   * distributed relative to the rest of other items included in the same flex line.
   */
  public val flexShrink: Double = DefaultFlexShrink,

  /**
   * The flexBasisPercent attribute of the item.
   *
   * The attribute determines the initial item length in a fraction format relative to its
   * parent.
   * The initial main size of this child is trying to be expanded as the specified
   * fraction against the parent main size.
   * If this value is set, the length specified from layout_width
   * (or layout_height) is overridden by the calculated value from this attribute.
   * This attribute is only effective when the parent's MeasureSpec mode is
   * MeasureSpec.EXACTLY. The default value is -1, which means not set.
   */
  public val flexBasisPercent: Double = DefaultFlexBasisPercent,

  /**
   * The align self attribute of the item.
   *
   * The attribute determines the alignment along the cross axis (perpendicular to the
   * main axis). The alignment in the same direction can be determined by the
   * align items attribute in the parent, but if this is set to other than
   * [AlignSelf.Auto], the cross axis alignment is overridden for this child.
   * The value needs to be one of the values in ([AlignSelf.Auto],
   * [AlignItems.Stretch], [AlignItems.FlexStart], [AlignItems.FlexEnd],
   * [AlignItems.Center], or [AlignItems.Baseline]).
   */
  public val alignSelf: AlignSelf = AlignSelf.Auto,

  /**
   * The wrapBefore attribute of the item.
   *
   * The attribute forces a flex line wrapping. i.e. if this is set to `true` for an
   * item, the item will become the first item of the new flex line. (A wrapping happens
   * regardless of the items being processed in the previous flex line)
   * This attribute is ignored if the flex_wrap attribute is set as nowrap.
   * The equivalent attribute isn't defined in the original CSS Flexible Box Module
   * specification, but having this attribute is useful for Android developers to flatten
   * the layouts when building a grid like layout or for a situation where developers want
   * to put a new flex line to make a semantic difference from the previous one, etc.
   */
  public val wrapBefore: Boolean = false,

  /**
   * The margin of the item.
   */
  public val margin: Spacing = Spacing.Zero,

  /**
   * A callback to measure this item according to a set of measurement constraints.
   */
  public var measurable: Measurable = Measurable(),
) {

  /** The item's size after invoking [FlexContainer.measure]. */
  public var measuredWidth: Double = -1.0
  public var measuredHeight: Double = -1.0

  /** The item's bounds after invoking [FlexContainer.layout]. */
  public var left: Double = -1.0
  public var top: Double = -1.0
  public var right: Double = -1.0
  public var bottom: Double = -1.0

  public companion object {
    /** The default value for the baseline attribute */
    public const val DefaultBaseline: Int = -1

    /** The default value for the flex grow attribute */
    public const val DefaultFlexGrow: Double = 0.0

    /** The default value for the flex shrink attribute */
    public const val DefaultFlexShrink: Double = 1.0

    /** The value representing the flex shrink attribute is not set */
    public const val UndefinedFlexShrink: Double = 0.0

    /** The default value for the flex basis percent attribute */
    public const val DefaultFlexBasisPercent: Double = -1.0
  }
}
