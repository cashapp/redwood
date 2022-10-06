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

package app.cash.redwood.flexcontainer

import app.cash.redwood.flexcontainer.FlexItem.Companion.DefaultFlexBasisPercent
import app.cash.redwood.flexcontainer.FlexItem.Companion.DefaultFlexGrow
import app.cash.redwood.flexcontainer.FlexItem.Companion.UndefinedFlexShrink
import app.cash.redwood.flexcontainer.Measurable.Companion.MatchParent
import kotlin.math.roundToInt

/**
 * A class that measures and positions its children according to its flex properties.
 */
public class FlexContainer {
  /**
   * The flex direction attribute of the container.
   */
  public var flexDirection: FlexDirection = FlexDirection.Row

  /**
   * The flex wrap attribute of the container.
   */
  public var flexWrap: FlexWrap = FlexWrap.NoWrap

  /**
   * The justify content attribute of the container.
   */
  public var justifyContent: JustifyContent = JustifyContent.FlexStart

  /**
   * The align content attribute of the container.
   */
  public var alignItems: AlignItems = AlignItems.FlexStart

  /**
   * The align items attribute of the container.
   */
  public var alignContent: AlignContent = AlignContent.FlexStart

  /**
   * The padding of the container.
   */
  public var padding: Spacing = Spacing.Zero

  /**
   * The current value of the maximum number of flex lines.
   */
  public var maxLines: Int = Int.MAX_VALUE

  /**
   * Returns the items held in the container.
   */
  public val items: MutableList<FlexItem> = ObservableMutableList(
    onChange = { reorderedItems = listOf() },
  )

  /**
   * Holds the list of [items] after [FlexItem.order] has been taken into account.
   */
  private var reorderedItems: List<FlexItem> = listOf()
    get() {
      if (field.size != items.size) {
        // Lazily sort the items by their order descending.
        field = items.withIndex()
          .sortedWith(compareBy({ -it.value.order }, { it.index }))
          .map { it.value }
      }
      return field
    }

  /**
   * The computed flex lines after calling [measure].
   */
  internal var flexLines: List<FlexLine> = listOf()

  /**
   * Calculate how many flex lines are needed in the flex container.
   * This method should calculate all the flex lines from the existing flex items.
   */
  internal fun calculateHorizontalFlexLines(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ) = calculateFlexLines(
    mainMeasureSpec = widthMeasureSpec,
    crossMeasureSpec = heightMeasureSpec,
    needsCalcAmount = Int.MAX_VALUE,
    fromIndex = 0,
    toIndex = -1,
  )

  /**
   * Calculate how many flex lines are needed in the flex container.
   * This method should calculate all the flex lines from the existing flex items.
   */
  internal fun calculateVerticalFlexLines(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ) = calculateFlexLines(
    mainMeasureSpec = heightMeasureSpec,
    crossMeasureSpec = widthMeasureSpec,
    needsCalcAmount = Int.MAX_VALUE,
    fromIndex = 0,
    toIndex = -1,
  )

  /**
   * Calculates how many flex lines are needed in the flex container layout by measuring each child.
   * Expanding or shrinking the flex items depending on the flex grow and flex shrink
   * attributes are done in a later procedure, so the items' measured width and measured
   * height may be changed in a later process.
   *
   * @param mainMeasureSpec the main axis measure spec imposed by the flex container,
   * width for horizontal direction, height otherwise
   * @param crossMeasureSpec the cross axis measure spec imposed by the flex container,
   * height for horizontal direction, width otherwise
   * @param needsCalcAmount the amount of pixels where flex line calculation should be stopped
   * this is needed to avoid the expensive calculation if the
   * calculation is needed only the small part of the entire flex container.
   * @param fromIndex the index of the child from which the calculation starts
   * @param toIndex the index of the child to which the calculation ends (until the
   * flex line which include the which who has that index). If this
   * and needsCalcAmount are both set, first flex lines are calculated
   * to the index, calculate the amount of pixels as the needsCalcAmount
   * argument in addition to that
   */
  @Suppress("SameParameterValue")
  private fun calculateFlexLines(
    mainMeasureSpec: MeasureSpec,
    crossMeasureSpec: MeasureSpec,
    needsCalcAmount: Int,
    fromIndex: Int,
    toIndex: Int,
  ): List<FlexLine> {
    val orientation = flexDirection.toOrientation()
    val mainMode = mainMeasureSpec.mode
    val mainSize = mainMeasureSpec.size
    val flexLines = mutableListOf<FlexLine>()
    var reachedToIndex = toIndex == -1
    var largestSizeInCross = Int.MIN_VALUE

    // The amount of cross size calculated in this method call.
    var sumCrossSize = 0

    // The index of the item in the flex line.
    var indexInFlexLine = 0
    var flexLine = FlexLine()
    flexLine.firstIndex = fromIndex
    flexLine.mainSize = orientation.mainPaddingStart(padding) + orientation.mainPaddingEnd(padding)
    val childCount = items.size
    for (i in fromIndex until childCount) {
      val child = reorderedItems.getOrNull(i)
      if (child == null) {
        if (isLastFlexItem(i, childCount, flexLine)) {
          addFlexLine(flexLines, flexLine, i, sumCrossSize)
        }
        continue
      } else if (!child.visible) {
        flexLine.invisibleItemCount++
        flexLine.itemCount++
        if (isLastFlexItem(i, childCount, flexLine)) {
          addFlexLine(flexLines, flexLine, i, sumCrossSize)
        }
        continue
      }
      var childMainSize = orientation.mainSize(child)
      if (child.flexBasisPercent != DefaultFlexBasisPercent && mainMode == MeasureSpecMode.Exactly) {
        childMainSize = (mainSize * child.flexBasisPercent).roundToInt()
        // Use the dimension from the layout if the mainMode is not MeasureSpecMode.Exactly even
        // if any fraction value is set to flexBasisPercent.
      }
      val childMainMeasureSpec: MeasureSpec
      var childCrossMeasureSpec: MeasureSpec
      if (flexDirection.isHorizontal) {
        childMainMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = mainMeasureSpec,
          padding = orientation.mainPaddingStart(padding) + orientation.mainPaddingEnd(padding) +
            orientation.mainMarginStart(child) + orientation.mainMarginEnd(child),
          childDimension = childMainSize,
        )
        childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = crossMeasureSpec,
          padding = orientation.crossPaddingStart(padding) + orientation.crossPaddingEnd(padding) +
            orientation.crossMarginStart(child) + orientation.crossMarginEnd(child) + sumCrossSize,
          childDimension = orientation.crossSize(child),
        )
        child.applyMeasure(childMainMeasureSpec, childCrossMeasureSpec)
      } else {
        childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = crossMeasureSpec,
          padding = orientation.crossPaddingStart(padding) + orientation.crossPaddingEnd(padding) +
            orientation.crossMarginStart(child) + orientation.crossMarginEnd(child) + sumCrossSize,
          childDimension = orientation.crossSize(child),
        )
        childMainMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = mainMeasureSpec,
          padding = orientation.mainPaddingStart(padding) + orientation.mainPaddingEnd(padding) +
            orientation.mainMarginStart(child) + orientation.mainMarginEnd(child),
          childDimension = childMainSize,
        )
        child.applyMeasure(childCrossMeasureSpec, childMainMeasureSpec)
      }

      // Check the size constraint after the first measurement for the child to prevent the child's
      // width/height from violating the size constraints imposed by the Node.minWidth,
      // Node.minHeight, Node.maxWidth, Node.maxHeight attributes. E.g. When the child's width is
      // WrapContent the measured width may be less than the min width after the first measurement.
      measureWithConstraints(child)
      if (
        isWrapRequired(
          mode = mainMode,
          maxSize = mainSize,
          currentLength = flexLine.mainSize,
          childLength = orientation.mainMeasuredSize(child) + orientation.mainMarginStart(child) + orientation.mainMarginEnd(child),
          flexItem = child,
          flexLinesSize = flexLines.size,
        )
      ) {
        if (flexLine.itemCountVisible > 0) {
          addFlexLine(flexLines, flexLine, if (i > 0) i - 1 else 0, sumCrossSize)
          sumCrossSize += flexLine.crossSize
        }
        val measurable = child.measurable
        if (flexDirection.isHorizontal) {
          val height = measurable.height
          if (height == MatchParent) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_height is set to match_parent, the height
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the height of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
              spec = crossMeasureSpec,
              padding = padding.top + padding.bottom + child.margin.top + child.margin.bottom + sumCrossSize,
              childDimension = height,
            )
            child.applyMeasure(childMainMeasureSpec, childCrossMeasureSpec)
            measureWithConstraints(child)
          }
        } else {
          val width = measurable.width
          if (width == MatchParent) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_width is set to match_parent, the width
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the width of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
              spec = crossMeasureSpec,
              padding = padding.start + padding.end + child.margin.start + child.margin.end + sumCrossSize,
              childDimension = width,
            )
            child.applyMeasure(childCrossMeasureSpec, childMainMeasureSpec)
            measureWithConstraints(child)
          }
        }
        flexLine = FlexLine()
        flexLine.itemCount = 1
        flexLine.mainSize = orientation.mainPaddingStart(padding) + orientation.mainPaddingEnd(padding)
        flexLine.firstIndex = i
        indexInFlexLine = 0
        largestSizeInCross = Int.MIN_VALUE
      } else {
        flexLine.itemCount++
        indexInFlexLine++
      }
      flexLine.anyItemsHaveFlexGrow = flexLine.anyItemsHaveFlexGrow || (child.flexGrow != DefaultFlexGrow)
      flexLine.anyItemsHaveFlexShrink = flexLine.anyItemsHaveFlexShrink || (child.flexShrink != UndefinedFlexShrink)
      flexLine.mainSize += orientation.mainMeasuredSize(child) + orientation.mainMarginStart(child) + orientation.mainMarginEnd(child)
      flexLine.totalFlexGrow += child.flexGrow
      flexLine.totalFlexShrink += child.flexShrink
      largestSizeInCross = maxOf(
        largestSizeInCross,
        orientation.crossMeasuredSize(child) + orientation.crossMarginStart(child) + orientation.crossMarginEnd(child)
      )
      // Temporarily set the cross axis length as the largest child in the flexLine
      // Expand along the cross axis depending on the alignContent property if needed later
      flexLine.crossSize = maxOf(flexLine.crossSize, largestSizeInCross)
      if (flexDirection.isHorizontal) {
        if (flexWrap != FlexWrap.WrapReverse) {
          flexLine.maxBaseline = maxOf(
            flexLine.maxBaseline,
            child.baseline + child.margin.top,
          )
        } else {
          // if the flex wrap property is WrapReverse, calculate the
          // baseline as the distance from the cross end and the baseline
          // since the cross size calculation is based on the distance from the cross end
          flexLine.maxBaseline = maxOf(
            flexLine.maxBaseline,
            child.measuredHeight - child.baseline + child.margin.bottom,
          )
        }
      }
      if (isLastFlexItem(i, childCount, flexLine)) {
        addFlexLine(flexLines, flexLine, i, sumCrossSize)
        sumCrossSize += flexLine.crossSize
      }
      if (toIndex != -1 && flexLines.size > 0 &&
        flexLines[flexLines.size - 1].lastIndex >= toIndex &&
        i >= toIndex && !reachedToIndex
      ) {
        // Calculated to include a flex line which includes the flex item having the toIndex.
        // Let the sumCrossSize start from the negative value of the last flex line's
        // cross size because otherwise flex lines aren't calculated enough to fill the
        // visible area.
        sumCrossSize = -flexLine.crossSize
        reachedToIndex = true
      }
      if (sumCrossSize > needsCalcAmount && reachedToIndex) {
        // Stop the calculation if the sum of cross size calculated reached to the point
        // beyond the needsCalcAmount value to avoid unneeded calculation in a RecyclerView.
        // To be precise, the decoration length may be added to the sumCrossSize,
        // but we omit adding the decoration length because even without the decorator
        // length, it's guaranteed that calculation is done at least beyond the
        // needsCalcAmount
        break
      }
    }
    return flexLines
  }

  /**
   * Determine if a wrap is required (add a new flex line).
   *
   * @param mode the width or height mode along the main axis direction
   * @param maxSize the max size along the main axis direction
   * @param currentLength the accumulated current length
   * @param childLength the length of a child which is to be collected to the flex line
   * @param flexItem the LayoutParams for the view being determined whether a new flex line is needed
   * @param flexLinesSize the number of the existing flexlines
   * @return `true` if a wrap is required, `false` otherwise
   */
  private fun isWrapRequired(
    mode: MeasureSpecMode,
    maxSize: Int,
    currentLength: Int,
    childLength: Int,
    flexItem: FlexItem,
    flexLinesSize: Int,
  ): Boolean {
    if (flexWrap == FlexWrap.NoWrap) {
      return false
    }
    if (flexItem.wrapBefore) {
      return true
    }
    if (mode == MeasureSpecMode.Unspecified) {
      return false
    }
    // Judge the condition by adding 1 to the current flexLinesSize because the flex line
    // being computed isn't added to the flexLinesSize.
    if (maxLines <= flexLinesSize + 1) {
      return false
    }
    return maxSize < currentLength + childLength
  }

  private fun isLastFlexItem(
    childIndex: Int,
    childCount: Int,
    flexLine: FlexLine,
  ) = childIndex == childCount - 1 && flexLine.itemCountVisible > 0

  private fun addFlexLine(
    flexLines: MutableList<FlexLine>,
    flexLine: FlexLine,
    itemIndex: Int,
    usedCrossSizeSoFar: Int,
  ) {
    flexLine.sumCrossSizeBefore = usedCrossSizeSoFar
    flexLine.lastIndex = itemIndex
    flexLines.add(flexLine)
  }

  /**
   * Remeasures the item if its [FlexItem.measuredWidth] or [FlexItem.measuredHeight] violate the
   * minimum/maximum size constraints imposed by its min/max attributes.
   */
  private fun measureWithConstraints(item: FlexItem) {
    var needsMeasure = false
    var childWidth = item.measuredWidth
    var childHeight = item.measuredHeight
    val measurable = item.measurable
    val minWidth = measurable.minWidth
    if (childWidth < minWidth) {
      needsMeasure = true
      childWidth = minWidth
    } else {
      val maxWidth = measurable.maxWidth
      if (childWidth > maxWidth) {
        needsMeasure = true
        childWidth = maxWidth
      }
    }
    val minHeight = measurable.minHeight
    if (childHeight < minHeight) {
      needsMeasure = true
      childHeight = minHeight
    } else {
      val maxHeight = measurable.maxHeight
      if (childHeight > maxHeight) {
        needsMeasure = true
        childHeight = maxHeight
      }
    }
    if (needsMeasure) {
      val widthSpec = MeasureSpec.from(childWidth, MeasureSpecMode.Exactly)
      val heightSpec = MeasureSpec.from(childHeight, MeasureSpecMode.Exactly)
      item.applyMeasure(widthSpec, heightSpec)
    }
  }

  /**
   * Determine the main size by expanding (shrinking if negative remaining free space is given)
   * an individual child in each flex line if any children's flexGrow (or flexShrink if remaining
   * space is negative) properties are set to non-zero.
   */
  internal fun determineMainSize(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ) {
    // Holds the 'frozen' state of children during measure. If a item is frozen it will no longer
    // expand or shrink regardless of flex grow/flex shrink attributes.
    val childrenFrozen = BooleanArray(items.size)

    val mainSize: Int
    val paddingAlongMainAxis: Int
    if (flexDirection.isHorizontal) {
      mainSize = if (widthMeasureSpec.mode == MeasureSpecMode.Exactly) {
        widthMeasureSpec.size
      } else {
        minOf(getLargestMainSize(), widthMeasureSpec.size)
      }
      paddingAlongMainAxis = padding.start + padding.end
    } else {
      mainSize = if (heightMeasureSpec.mode == MeasureSpecMode.Exactly) {
        heightMeasureSpec.size
      } else {
        getLargestMainSize()
      }
      paddingAlongMainAxis = padding.top + padding.bottom
    }
    for (flexLine in flexLines) {
      if (flexLine.mainSize < mainSize && flexLine.anyItemsHaveFlexGrow) {
        expandFlexItems(
          childrenFrozen = childrenFrozen,
          widthMeasureSpec = widthMeasureSpec,
          heightMeasureSpec = heightMeasureSpec,
          flexLine = flexLine,
          maxMainSize = mainSize,
          paddingAlongMainAxis = paddingAlongMainAxis,
          calledRecursively = false,
        )
      } else if (flexLine.mainSize > mainSize && flexLine.anyItemsHaveFlexShrink) {
        shrinkFlexItems(
          childrenFrozen = childrenFrozen,
          widthMeasureSpec = widthMeasureSpec,
          heightMeasureSpec = heightMeasureSpec,
          flexLine = flexLine,
          maxMainSize = mainSize,
          paddingAlongMainAxis = paddingAlongMainAxis,
          calledRecursively = false,
        )
      }
    }
  }

  /**
   * Expand the flex items along the main axis based on the individual [FlexItem.flexGrow] attribute.
   *
   * @param widthMeasureSpec the horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Expanded main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   */
  private fun expandFlexItems(
    childrenFrozen: BooleanArray,
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    flexLine: FlexLine,
    maxMainSize: Int,
    paddingAlongMainAxis: Int,
    calledRecursively: Boolean,
  ) {
    if (flexLine.totalFlexGrow <= 0 || maxMainSize < flexLine.mainSize) {
      return
    }
    val sizeBeforeExpand = flexLine.mainSize
    var needsReexpand = false
    val unitSpace = (maxMainSize - flexLine.mainSize) / flexLine.totalFlexGrow
    flexLine.mainSize = paddingAlongMainAxis

    // Setting the cross size of the flex line as the temporal value since the cross size of
    // each flex item may be changed from the initial calculation
    // (in the measureHorizontal/measureVertical method) even this method is part of the main
    // size determination.
    // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
    // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
    // direction to enclose its content (in the measureHorizontal method), but
    // the width will be expanded in this method. In that case, the height needs to be measured
    // again with the expanded width.
    var largestCrossSize = 0
    if (!calledRecursively) {
      flexLine.crossSize = Int.MIN_VALUE
    }
    var accumulatedRoundError = 0f
    for (i in 0 until flexLine.itemCount) {
      val index = flexLine.firstIndex + i
      val child = reorderedItems.getOrNull(index)
      if (child == null || !child.visible) {
        continue
      }
      val measurable = child.measurable
      if (flexDirection == FlexDirection.Row || flexDirection == FlexDirection.RowReverse) {
        // The direction of the main axis is horizontal
        var childMeasuredWidth = child.measuredWidth
        var childMeasuredHeight = child.measuredHeight
        if (!childrenFrozen[index] && child.flexGrow > 0f) {
          var rawCalculatedWidth = (childMeasuredWidth + unitSpace * child.flexGrow)
          if (i == flexLine.itemCount - 1) {
            rawCalculatedWidth += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newWidth = rawCalculatedWidth.roundToInt()
          val maxWidth = measurable.maxWidth
          if (newWidth > maxWidth) {
            // This means the child can't expand beyond the value of the maxWidth
            // attribute.
            // To adjust the flex line length to the size of maxMainSize, remaining
            // positive free space needs to be re-distributed to other flex items
            // (children items). In that case, invoke this method again with the same
            // fromIndex.
            needsReexpand = true
            newWidth = maxWidth
            childrenFrozen[index] = true
            flexLine.totalFlexGrow -= child.flexGrow
          } else {
            accumulatedRoundError += rawCalculatedWidth - newWidth
            if (accumulatedRoundError > 1.0) {
              newWidth += 1
              accumulatedRoundError -= 1.0f
            } else if (accumulatedRoundError < -1.0) {
              newWidth -= 1
              accumulatedRoundError += 1.0f
            }
          }
          val childHeightMeasureSpec = getChildHeightMeasureSpecInternal(
            heightMeasureSpec,
            child,
            flexLine.sumCrossSizeBefore,
          )
          val childWidthMeasureSpec = MeasureSpec.from(newWidth, MeasureSpecMode.Exactly)
          child.applyMeasure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
        }
        largestCrossSize = maxOf(
          largestCrossSize,
          childMeasuredHeight + child.margin.top + child.margin.bottom,
        )
        flexLine.mainSize += (childMeasuredWidth + child.margin.start + child.margin.end)
      } else {
        // The direction of the main axis is vertical
        var childMeasuredHeight = child.measuredHeight
        var childMeasuredWidth = child.measuredWidth
        if (!childrenFrozen[index] && child.flexGrow > 0f) {
          var rawCalculatedHeight = (childMeasuredHeight + unitSpace * child.flexGrow)
          if (i == flexLine.itemCount - 1) {
            rawCalculatedHeight += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newHeight = rawCalculatedHeight.roundToInt()
          val maxHeight = measurable.maxHeight
          if (newHeight > maxHeight) {
            // This means the child can't expand beyond the value of the maxHeight
            // attribute.
            // To adjust the flex line length to the size of maxMainSize, remaining
            // positive free space needs to be re-distributed to other flex items
            // (children items). In that case, invoke this method again with the same
            // fromIndex.
            needsReexpand = true
            newHeight = maxHeight
            childrenFrozen[index] = true
            flexLine.totalFlexGrow -= child.flexGrow
          } else {
            accumulatedRoundError += rawCalculatedHeight - newHeight
            if (accumulatedRoundError > 1.0) {
              newHeight += 1
              accumulatedRoundError -= 1.0f
            } else if (accumulatedRoundError < -1.0) {
              newHeight -= 1
              accumulatedRoundError += 1.0f
            }
          }
          val childWidthMeasureSpec = getChildWidthMeasureSpecInternal(
            widthMeasureSpec = widthMeasureSpec,
            flexItem = child,
            padding = flexLine.sumCrossSizeBefore,
          )
          val childHeightMeasureSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
          child.applyMeasure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
        }
        largestCrossSize = maxOf(
          largestCrossSize,
          childMeasuredWidth + child.margin.start + child.margin.end,
        )
        flexLine.mainSize += (childMeasuredHeight + child.margin.top + child.margin.bottom)
      }
      flexLine.crossSize = maxOf(flexLine.crossSize, largestCrossSize)
    }
    if (needsReexpand && sizeBeforeExpand != flexLine.mainSize) {
      // Re-invoke the method with the same flex line to distribute the positive free space
      // that wasn't fully distributed (because of maximum length constraint)
      expandFlexItems(
        childrenFrozen = childrenFrozen,
        widthMeasureSpec = widthMeasureSpec,
        heightMeasureSpec = heightMeasureSpec,
        flexLine = flexLine,
        maxMainSize = maxMainSize,
        paddingAlongMainAxis = paddingAlongMainAxis,
        calledRecursively = true,
      )
    }
  }

  /**
   * Shrink the flex items along the main axis based on the individual [FlexItem.flexShrink] attribute.
   *
   * @param widthMeasureSpec the horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Shrank main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   */
  private fun shrinkFlexItems(
    childrenFrozen: BooleanArray,
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    flexLine: FlexLine,
    maxMainSize: Int,
    paddingAlongMainAxis: Int,
    calledRecursively: Boolean,
  ) {
    val sizeBeforeShrink = flexLine.mainSize
    if (flexLine.totalFlexShrink <= 0 || maxMainSize > flexLine.mainSize) {
      return
    }
    var needsReshrink = false
    val unitShrink = (flexLine.mainSize - maxMainSize) / flexLine.totalFlexShrink
    var accumulatedRoundError = 0f
    flexLine.mainSize = paddingAlongMainAxis

    // Setting the cross size of the flex line as the temporal value since the cross size of
    // each flex item may be changed from the initial calculation
    // (in the measureHorizontal/measureVertical method) even this method is part of the main
    // size determination.
    // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
    // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
    // direction to enclose its content (in the measureHorizontal method), but
    // the width will be expanded in this method. In that case, the height needs to be measured
    // again with the expanded width.
    var largestCrossSize = 0
    if (!calledRecursively) {
      flexLine.crossSize = Int.MIN_VALUE
    }
    for (i in 0 until flexLine.itemCount) {
      val index = flexLine.firstIndex + i
      val child = reorderedItems.getOrNull(index)
      if (child == null || !child.visible) {
        continue
      }
      val measurable = child.measurable
      if (flexDirection.isHorizontal) {
        // The direction of main axis is horizontal
        var childMeasuredWidth = child.measuredWidth
        var childMeasuredHeight = child.measuredHeight
        if (!childrenFrozen[index] && child.flexShrink > 0f) {
          var rawCalculatedWidth = childMeasuredWidth - unitShrink * child.flexShrink
          if (i == flexLine.itemCount - 1) {
            rawCalculatedWidth += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newWidth = rawCalculatedWidth.roundToInt()
          val minWidth = measurable.minWidth
          if (newWidth < minWidth) {
            // This means the child doesn't have enough space to distribute the negative
            // free space. To adjust the flex line length down to the maxMainSize, remaining
            // negative free space needs to be re-distributed to other flex items (children
            // items). In that case, invoke this method again with the same fromIndex.
            needsReshrink = true
            newWidth = minWidth
            childrenFrozen[index] = true
            flexLine.totalFlexShrink -= child.flexShrink
          } else {
            accumulatedRoundError += rawCalculatedWidth - newWidth
            if (accumulatedRoundError > 1.0) {
              newWidth += 1
              accumulatedRoundError -= 1f
            } else if (accumulatedRoundError < -1.0) {
              newWidth -= 1
              accumulatedRoundError += 1f
            }
          }
          val childHeightMeasureSpec = getChildHeightMeasureSpecInternal(
            heightMeasureSpec,
            child,
            flexLine.sumCrossSizeBefore,
          )
          val childWidthMeasureSpec = MeasureSpec.from(newWidth, MeasureSpecMode.Exactly)
          child.applyMeasure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
        }
        largestCrossSize = maxOf(
          largestCrossSize,
          childMeasuredHeight + child.margin.top + child.margin.bottom,
        )
        flexLine.mainSize += (childMeasuredWidth + child.margin.start + child.margin.end)
      } else {
        // The direction of main axis is vertical
        var childMeasuredHeight = child.measuredHeight
        var childMeasuredWidth = child.measuredWidth
        if (!childrenFrozen[index] && child.flexShrink > 0f) {
          var rawCalculatedHeight = childMeasuredHeight - unitShrink * child.flexShrink
          if (i == flexLine.itemCount - 1) {
            rawCalculatedHeight += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newHeight = rawCalculatedHeight.roundToInt()
          val minHeight = measurable.minHeight
          if (newHeight < minHeight) {
            // Need to invoke this method again like the case flex direction is vertical
            needsReshrink = true
            newHeight = minHeight
            childrenFrozen[index] = true
            flexLine.totalFlexShrink -= child.flexShrink
          } else {
            accumulatedRoundError += rawCalculatedHeight - newHeight
            if (accumulatedRoundError > 1.0) {
              newHeight += 1
              accumulatedRoundError -= 1f
            } else if (accumulatedRoundError < -1.0) {
              newHeight -= 1
              accumulatedRoundError += 1f
            }
          }
          val childWidthMeasureSpec = getChildWidthMeasureSpecInternal(
            widthMeasureSpec = widthMeasureSpec,
            flexItem = child,
            padding = flexLine.sumCrossSizeBefore,
          )
          val childHeightMeasureSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
          child.applyMeasure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
        }
        largestCrossSize = maxOf(
          largestCrossSize,
          childMeasuredWidth + child.margin.start + child.margin.end,
        )
        flexLine.mainSize += (childMeasuredHeight + child.margin.top + child.margin.bottom)
      }
      flexLine.crossSize = maxOf(flexLine.crossSize, largestCrossSize)
    }
    if (needsReshrink && sizeBeforeShrink != flexLine.mainSize) {
      // Re-invoke the method with the same fromIndex to distribute the negative free space
      // that wasn't fully distributed (because some items length were not enough)
      shrinkFlexItems(
        childrenFrozen = childrenFrozen,
        widthMeasureSpec = widthMeasureSpec,
        heightMeasureSpec = heightMeasureSpec,
        flexLine = flexLine,
        maxMainSize = maxMainSize,
        paddingAlongMainAxis = paddingAlongMainAxis,
        calledRecursively = true,
      )
    }
  }

  private fun getChildWidthMeasureSpecInternal(
    widthMeasureSpec: MeasureSpec,
    flexItem: FlexItem,
    padding: Int,
  ): MeasureSpec {
    val measurable = flexItem.measurable
    val childWidthMeasureSpec = MeasureSpec.getChildMeasureSpec(
      spec = widthMeasureSpec,
      padding = this.padding.start + this.padding.end + flexItem.margin.start + flexItem.margin.end + padding,
      childDimension = measurable.width,
    )
    val childWidth = childWidthMeasureSpec.size
    val maxWidth = measurable.maxWidth
    if (childWidth > maxWidth) {
      return MeasureSpec.from(maxWidth, childWidthMeasureSpec.mode)
    }
    val minWidth = measurable.minWidth
    if (childWidth < minWidth) {
      return MeasureSpec.from(minWidth, childWidthMeasureSpec.mode)
    }
    return childWidthMeasureSpec
  }

  private fun getChildHeightMeasureSpecInternal(
    heightMeasureSpec: MeasureSpec,
    flexItem: FlexItem,
    padding: Int,
  ): MeasureSpec {
    val measurable = flexItem.measurable
    val childHeightMeasureSpec = MeasureSpec.getChildMeasureSpec(
      spec = heightMeasureSpec,
      padding = this.padding.top + this.padding.bottom + flexItem.margin.top + flexItem.margin.bottom + padding,
      childDimension = measurable.height,
    )
    val childHeight = childHeightMeasureSpec.size
    val maxHeight = measurable.maxHeight
    if (childHeight > maxHeight) {
      return MeasureSpec.from(maxHeight, childHeightMeasureSpec.mode)
    }
    val minHeight = measurable.minHeight
    if (childHeight < minHeight) {
      return MeasureSpec.from(minHeight, childHeightMeasureSpec.mode)
    }
    return childHeightMeasureSpec
  }

  /**
   * Determines the cross size (Calculate the length along the cross axis).
   * Expand the cross size only if the height mode is [MeasureSpecMode.Exactly], otherwise
   * use the sum of cross sizes of all flex lines.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   * @param paddingAlongCrossAxis the padding value for the flexbox along the cross axis
   */
  internal fun determineCrossSize(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    paddingAlongCrossAxis: Int,
  ) {
    val mode: MeasureSpecMode // The MeasureSpec mode along the cross axis
    val size: Int // The MeasureSpec size along the cross axis
    if (flexDirection.isHorizontal) {
      mode = heightMeasureSpec.mode
      size = heightMeasureSpec.size
    } else {
      mode = widthMeasureSpec.mode
      size = widthMeasureSpec.size
    }
    if (mode == MeasureSpecMode.Exactly) {
      val totalCrossSize = getSumOfCrossSize() + paddingAlongCrossAxis
      if (flexLines.size == 1) {
        flexLines[0].crossSize = size - paddingAlongCrossAxis
        // alignContent property is valid only if the Flexbox has at least two lines
      } else if (flexLines.size >= 2) {
        when (alignContent) {
          AlignContent.Stretch -> run switch@{
            if (totalCrossSize >= size) {
              return@switch
            }
            val freeSpaceUnit = (size - totalCrossSize) / flexLines.size.toFloat()
            var accumulatedError = 0f
            var i = 0
            val flexLinesSize = flexLines.size
            while (i < flexLinesSize) {
              val flexLine = flexLines[i]
              var newCrossSizeAsFloat = flexLine.crossSize + freeSpaceUnit
              if (i == flexLines.size - 1) {
                newCrossSizeAsFloat += accumulatedError
                accumulatedError = 0f
              }
              var newCrossSize = newCrossSizeAsFloat.roundToInt()
              accumulatedError += newCrossSizeAsFloat - newCrossSize
              if (accumulatedError > 1) {
                newCrossSize += 1
                accumulatedError -= 1f
              } else if (accumulatedError < -1) {
                newCrossSize -= 1
                accumulatedError += 1f
              }
              flexLine.crossSize = newCrossSize
              i++
            }
          }
          AlignContent.SpaceAround -> run switch@{
            if (totalCrossSize >= size) {
              // If the size of the content is larger than the flex container, the
              // Flex lines should be aligned center like ALIGN_CONTENT_CENTER
              flexLines = constructFlexLinesForAlignContentCenter(
                flexLines = flexLines,
                size = size,
                totalCrossSize = totalCrossSize,
              )
              return@switch
            }
            // The value of free space along the cross axis which needs to be put on top
            // and below the bottom of each flex line.
            var spaceTopAndBottom = size - totalCrossSize
            // The number of spaces along the cross axis
            val numberOfSpaces = flexLines.size * 2
            spaceTopAndBottom /= numberOfSpaces
            val newFlexLines = ArrayList<FlexLine>()
            val dummySpaceFlexLine = FlexLine()
            dummySpaceFlexLine.crossSize = spaceTopAndBottom
            for (flexLine in flexLines) {
              newFlexLines.add(dummySpaceFlexLine)
              newFlexLines.add(flexLine)
              newFlexLines.add(dummySpaceFlexLine)
            }
            flexLines = newFlexLines
          }
          AlignContent.SpaceBetween -> run switch@{
            if (totalCrossSize >= size) {
              return@switch
            }
            // The value of free space along the cross axis between each flex line.
            var spaceBetweenFlexLine = (size - totalCrossSize).toFloat()
            val numberOfSpaces = flexLines.size - 1
            spaceBetweenFlexLine /= numberOfSpaces.toFloat()
            var accumulatedError = 0f
            val newFlexLines = ArrayList<FlexLine>()
            var i = 0
            val flexLineSize = flexLines.size
            while (i < flexLineSize) {
              val flexLine = flexLines[i]
              newFlexLines.add(flexLine)
              if (i != flexLines.size - 1) {
                val dummySpaceFlexLine = FlexLine()
                if (i == flexLines.size - 2) {
                  // The last dummy space block in the flex container.
                  // Adjust the cross size by the accumulated error.
                  dummySpaceFlexLine.crossSize = (spaceBetweenFlexLine + accumulatedError).roundToInt()
                  accumulatedError = 0f
                } else {
                  dummySpaceFlexLine.crossSize = spaceBetweenFlexLine.roundToInt()
                }
                accumulatedError += (spaceBetweenFlexLine - dummySpaceFlexLine.crossSize)
                if (accumulatedError > 1) {
                  dummySpaceFlexLine.crossSize += 1
                  accumulatedError -= 1f
                } else if (accumulatedError < -1) {
                  dummySpaceFlexLine.crossSize -= 1
                  accumulatedError += 1f
                }
                newFlexLines.add(dummySpaceFlexLine)
              }
              i++
            }
            flexLines = newFlexLines
          }
          AlignContent.Center -> {
            flexLines = constructFlexLinesForAlignContentCenter(
              flexLines = flexLines,
              size = size,
              totalCrossSize = totalCrossSize,
            )
          }
          AlignContent.FlexEnd -> {
            val spaceTop = size - totalCrossSize
            val dummySpaceFlexLine = FlexLine()
            dummySpaceFlexLine.crossSize = spaceTop
            flexLines = flexLines.toMutableList().apply { add(0, dummySpaceFlexLine) }
          }
        }
      }
    }
  }

  private fun constructFlexLinesForAlignContentCenter(
    flexLines: List<FlexLine>,
    size: Int,
    totalCrossSize: Int,
  ): List<FlexLine> {
    val spaceAboveAndBottom = (size - totalCrossSize) / 2
    val newFlexLines = ArrayList<FlexLine>()
    val dummySpaceFlexLine = FlexLine()
    dummySpaceFlexLine.crossSize = spaceAboveAndBottom
    for (i in flexLines.indices) {
      if (i == 0) {
        newFlexLines.add(dummySpaceFlexLine)
      }
      val flexLine = flexLines[i]
      newFlexLines.add(flexLine)
      if (i == flexLines.size - 1) {
        newFlexLines.add(dummySpaceFlexLine)
      }
    }
    return newFlexLines
  }

  /**
   * Expand the item if the [FlexContainer.alignItems] attribute is set to
   * [AlignItems.Stretch] or [FlexItem.alignSelf] is set as [AlignItems.Stretch].
   */
  internal fun stretchChildren() {
    if (alignItems == AlignItems.Stretch) {
      for (flexLine in flexLines) {
        for (i in 0 until flexLine.itemCount) {
          val itemIndex = flexLine.firstIndex + i
          if (i >= items.size) {
            continue
          }
          val item = reorderedItems.getOrNull(itemIndex)
          if (item == null || !item.visible) {
            continue
          }
          if (item.alignSelf != AlignSelf.Auto && item.alignSelf != AlignSelf.Stretch) {
            continue
          }
          if (flexDirection.isHorizontal) {
            stretchViewVertically(item, flexLine.crossSize)
          } else {
            stretchViewHorizontally(item, flexLine.crossSize)
          }
        }
      }
    } else {
      for (flexLine in flexLines) {
        for (index in flexLine.firstIndex..flexLine.lastIndex) {
          val item = reorderedItems[index]
          if (item.alignSelf == AlignSelf.Stretch) {
            if (flexDirection.isHorizontal) {
              stretchViewVertically(item, flexLine.crossSize)
            } else {
              stretchViewHorizontally(item, flexLine.crossSize)
            }
          }
        }
      }
    }
  }

  /**
   * Expand the item vertically to the size of the [crossSize] (considering [item]'s margins).
   */
  private fun stretchViewVertically(item: FlexItem, crossSize: Int) {
    val newHeight = (crossSize - item.margin.top - item.margin.bottom)
      .coerceIn(item.measurable.minHeight, item.measurable.maxHeight)
    val childWidthSpec = MeasureSpec.from(item.measuredWidth, MeasureSpecMode.Exactly)
    val childHeightSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
    item.applyMeasure(childWidthSpec, childHeightSpec)
  }

  /**
   * Expand the item horizontally to the size of the crossSize (considering [item]'s margins).
   */
  private fun stretchViewHorizontally(item: FlexItem, crossSize: Int) {
    val newWidth = (crossSize - item.margin.start - item.margin.end)
      .coerceIn(item.measurable.minWidth, item.measurable.maxWidth)
    val childHeightSpec = MeasureSpec.from(item.measuredHeight, MeasureSpecMode.Exactly)
    val childWidthSpec = MeasureSpec.from(newWidth, MeasureSpecMode.Exactly)
    item.applyMeasure(childWidthSpec, childHeightSpec)
  }

  /**
   * Place a single View when the layout direction is horizontal
   * ([FlexContainer.flexDirection] is either [FlexDirection.Row] or [FlexDirection.RowReverse]).
   */
  private fun layoutSingleChildHorizontal(
    item: FlexItem,
    flexLine: FlexLine,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    var alignItems = alignItems
    if (item.alignSelf != AlignSelf.Auto) {
      // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
      // Assigning the alignSelf value as alignItems should work.
      alignItems = item.alignSelf.toAlignItems()
    }
    val crossSize = flexLine.crossSize
    when (alignItems) {
      AlignItems.FlexStart, AlignItems.Stretch -> if (flexWrap != FlexWrap.WrapReverse) {
        item.layout(left, top + item.margin.top, right, bottom + item.margin.top)
      } else {
        item.layout(left, top - item.margin.bottom, right, bottom - item.margin.bottom)
      }
      AlignItems.Baseline -> if (flexWrap != FlexWrap.WrapReverse) {
        val marginTop = maxOf(flexLine.maxBaseline - item.baseline, item.margin.top)
        item.layout(left, top + marginTop, right, bottom + marginTop)
      } else {
        val marginBottom = maxOf(flexLine.maxBaseline - item.measuredHeight + item.baseline, item.margin.bottom)
        item.layout(left, top - marginBottom, right, bottom - marginBottom)
      }
      AlignItems.FlexEnd -> if (flexWrap != FlexWrap.WrapReverse) {
        item.layout(
          left = left,
          top = top + crossSize - item.measuredHeight - item.margin.bottom,
          right = right,
          bottom = top + crossSize - item.margin.bottom,
        )
      } else {
        // If the flexWrap == WrapReverse, the direction of the
        // flexEnd is flipped (from top to bottom).
        item.layout(
          left = left,
          top = top - crossSize + item.measuredHeight + item.margin.top,
          right = right,
          bottom = bottom - crossSize + item.measuredHeight + item.margin.top,
        )
      }
      AlignItems.Center -> {
        val topFromCrossAxis = (crossSize - item.measuredHeight + item.margin.top - item.margin.bottom) / 2
        if (flexWrap != FlexWrap.WrapReverse) {
          item.layout(
            left = left,
            top = top + topFromCrossAxis,
            right = right,
            bottom = top + topFromCrossAxis + item.measuredHeight,
          )
        } else {
          item.layout(
            left = left,
            top = top - topFromCrossAxis,
            right = right,
            bottom = top - topFromCrossAxis + item.measuredHeight,
          )
        }
      }
    }
  }

  /**
   * Place a single View when the layout direction is vertical
   * ([FlexContainer.flexDirection] is either [FlexDirection.Column] or [FlexDirection.ColumnReverse]).
   */
  private fun layoutSingleChildVertical(
    item: FlexItem,
    flexLine: FlexLine,
    isRtl: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    var alignItems = alignItems
    if (item.alignSelf != AlignSelf.Auto) {
      // Expecting the values for alignItems and alignSelf match except for Auto.
      // Assigning the alignSelf value as alignItems should work.
      alignItems = item.alignSelf.toAlignItems()
    }
    val crossSize = flexLine.crossSize
    when (alignItems) {
      AlignItems.FlexStart, AlignItems.Stretch, AlignItems.Baseline -> if (!isRtl) {
        item.layout(
          left = left + item.margin.start,
          top = top,
          right = right + item.margin.start,
          bottom = bottom,
        )
      } else {
        item.layout(
          left = left - item.margin.end,
          top = top,
          right = right - item.margin.end,
          bottom = bottom,
        )
      }
      AlignItems.FlexEnd -> if (!isRtl) {
        item.layout(
          left = left + crossSize - item.measuredWidth - item.margin.end,
          top = top,
          right = right + crossSize - item.measuredWidth - item.margin.end,
          bottom = bottom,
        )
      } else {
        // If the flexWrap == WrapReverse, the direction of the
        // flexEnd is flipped (from left to right).
        item.layout(
          left = left - crossSize + item.measuredWidth + item.margin.start,
          top = top,
          right = right - crossSize + item.measuredWidth + item.margin.start,
          bottom = bottom,
        )
      }
      AlignItems.Center -> {
        val leftFromCrossAxis = (crossSize - item.measuredWidth + item.margin.start - item.margin.end) / 2
        if (!isRtl) {
          item.layout(left + leftFromCrossAxis, top, right + leftFromCrossAxis, bottom)
        } else {
          item.layout(left - leftFromCrossAxis, top, right - leftFromCrossAxis, bottom)
        }
      }
    }
  }

  /**
   * Measures the flexbox's items according to the parent's [widthSpec] and [heightSpec] and returns
   * the expected size of the flexbox.
   *
   * @param widthSpec horizontal space requirements as imposed by the parent.
   * @param heightSpec vertical space requirements as imposed by the parent.
   */
  public fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    if (flexDirection.isHorizontal) {
      return measureHorizontal(widthSpec, heightSpec)
    } else {
      return measureVertical(widthSpec, heightSpec)
    }
  }

  private fun measureHorizontal(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    flexLines = calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    determineMainSize(widthMeasureSpec, heightMeasureSpec)

    if (alignItems == AlignItems.Baseline) {
      for (flexLine in flexLines) {
        // The largest height value that also take the baseline shift into account
        var largestHeightInLine = Int.MIN_VALUE
        for (i in flexLine.firstIndex..flexLine.lastIndex) {
          val child = reorderedItems.getOrNull(i)
          if (child == null || !child.visible) {
            continue
          }
          val heightInLine = if (flexWrap != FlexWrap.WrapReverse) {
            val marginTop = maxOf(flexLine.maxBaseline - child.baseline, child.margin.top)
            child.measuredHeight + marginTop + child.margin.bottom
          } else {
            val marginBottom = maxOf(flexLine.maxBaseline - child.measuredHeight + child.baseline, child.margin.bottom)
            child.measuredHeight + child.margin.top + marginBottom
          }
          largestHeightInLine = maxOf(largestHeightInLine, heightInLine)
        }
        flexLine.crossSize = largestHeightInLine
      }
    }
    determineCrossSize(
      widthMeasureSpec = widthMeasureSpec,
      heightMeasureSpec = heightMeasureSpec,
      paddingAlongCrossAxis = padding.top + padding.bottom,
    )
    // Now cross size for each flex line is determined.
    // Expand the items if alignItems (or alignSelf in each child) is set to stretch
    stretchChildren()
    return setMeasuredDimensionForFlex(widthMeasureSpec, heightMeasureSpec)
  }

  private fun measureVertical(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    flexLines = calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    determineMainSize(widthMeasureSpec, heightMeasureSpec)
    determineCrossSize(
      widthMeasureSpec = widthMeasureSpec,
      heightMeasureSpec = heightMeasureSpec,
      paddingAlongCrossAxis = padding.start + padding.end,
    )
    // Now cross size for each flex line is determined.
    // Expand the items if alignItems (or alignSelf in each child) is set to stretch
    stretchChildren()
    return setMeasuredDimensionForFlex(widthMeasureSpec, heightMeasureSpec)
  }

  /**
   * Set this flexbox's width and height depending on the calculated size of main axis and
   * cross axis.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  private fun setMeasuredDimensionForFlex(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    val calculatedMaxHeight: Int
    val calculatedMaxWidth: Int
    if (flexDirection.isHorizontal) {
      calculatedMaxHeight = getSumOfCrossSize() + padding.top + padding.bottom
      calculatedMaxWidth = getLargestMainSize()
    } else {
      calculatedMaxHeight = getLargestMainSize()
      calculatedMaxWidth = getSumOfCrossSize() + padding.start + padding.end
    }
    val width = when (widthMeasureSpec.mode) {
      MeasureSpecMode.Exactly -> {
        MeasureSpec.resolveSize(widthMeasureSpec.size, widthMeasureSpec)
      }
      MeasureSpecMode.AtMost -> {
        MeasureSpec.resolveSize(minOf(widthMeasureSpec.size, calculatedMaxWidth), widthMeasureSpec)
      }
      MeasureSpecMode.Unspecified -> {
        MeasureSpec.resolveSize(calculatedMaxWidth, widthMeasureSpec)
      }
      else -> throw AssertionError()
    }
    val height = when (heightMeasureSpec.mode) {
      MeasureSpecMode.Exactly -> {
        MeasureSpec.resolveSize(heightMeasureSpec.size, heightMeasureSpec)
      }
      MeasureSpecMode.AtMost -> {
        MeasureSpec.resolveSize(minOf(heightMeasureSpec.size, calculatedMaxHeight), heightMeasureSpec)
      }
      MeasureSpecMode.Unspecified -> {
        MeasureSpec.resolveSize(calculatedMaxHeight, heightMeasureSpec)
      }
      else -> throw AssertionError()
    }
    return Size(width, height)
  }

  public fun layout(left: Int, top: Int, right: Int, bottom: Int) {
    when (flexDirection) {
      FlexDirection.Row -> {
        layoutHorizontal(false, left, top, right, bottom)
      }
      FlexDirection.RowReverse -> {
        layoutHorizontal(true, left, top, right, bottom)
      }
      FlexDirection.Column -> {
        val isRtl = flexWrap == FlexWrap.WrapReverse
        layoutVertical(isRtl, false, left, top, right, bottom)
      }
      FlexDirection.ColumnReverse -> {
        val isRtl = flexWrap != FlexWrap.WrapReverse
        layoutVertical(isRtl, true, left, top, right, bottom)
      }
      else -> throw AssertionError()
    }
  }

  /**
   * Sub method for `onLayout` when the [FlexContainer.flexDirection] is either
   * [FlexDirection.Row] or [FlexDirection.RowReverse].
   *
   * @param isRtl `true` if the horizontal layout direction is right to left, `false` otherwise.
   */
  private fun layoutHorizontal(
    isRtl: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    val paddingLeft = padding.start
    val paddingRight = padding.end
    // Use float to reduce the round error that may happen in when justifyContent ==
    // SpaceBetween or SpaceAround
    var childLeft: Float
    val height = bottom - top
    val width = right - left
    // childBottom is used if the flexWrap is WrapReverse otherwise
    // childTop is used to align the vertical position of the children items.
    var childBottom = height - padding.bottom
    var childTop = padding.top

    // Used only for RTL layout
    // Use float to reduce the round error that may happen in when justifyContent ==
    // SpaceBetween or SpaceAround
    var childRight: Float
    for (flexLine in flexLines) {
      var spaceBetweenItem = 0f
      when (justifyContent) {
        JustifyContent.FlexStart -> {
          childLeft = paddingLeft.toFloat()
          childRight = (width - paddingRight).toFloat()
        }
        JustifyContent.FlexEnd -> {
          childLeft = (width - flexLine.mainSize + paddingRight).toFloat()
          childRight = (flexLine.mainSize - paddingLeft).toFloat()
        }
        JustifyContent.Center -> {
          childLeft = paddingLeft + (width - flexLine.mainSize) / 2f
          childRight = width - paddingRight - (width - flexLine.mainSize) / 2f
        }
        JustifyContent.SpaceAround -> {
          val visibleCount = flexLine.itemCountVisible
          if (visibleCount != 0) {
            spaceBetweenItem = ((width - flexLine.mainSize) / visibleCount.toFloat())
          }
          childLeft = paddingLeft + spaceBetweenItem / 2f
          childRight = width - paddingRight - spaceBetweenItem / 2f
        }
        JustifyContent.SpaceBetween -> {
          childLeft = paddingLeft.toFloat()
          val visibleCount = flexLine.itemCountVisible
          val denominator = if (visibleCount != 1) (visibleCount - 1).toFloat() else 1f
          spaceBetweenItem = (width - flexLine.mainSize) / denominator
          childRight = (width - paddingRight).toFloat()
        }
        JustifyContent.SpaceEvenly -> {
          val visibleCount = flexLine.itemCountVisible
          if (visibleCount != 0) {
            spaceBetweenItem = ((width - flexLine.mainSize) / (visibleCount + 1).toFloat())
          }
          childLeft = paddingLeft + spaceBetweenItem
          childRight = width - paddingRight - spaceBetweenItem
        }
        else -> throw AssertionError()
      }
      spaceBetweenItem = maxOf(spaceBetweenItem, 0f)
      for (i in 0 until flexLine.itemCount) {
        val index = flexLine.firstIndex + i
        val child = reorderedItems.getOrNull(index)
        if (child == null || !child.visible) {
          continue
        }
        childLeft += child.margin.start.toFloat()
        childRight -= child.margin.end.toFloat()
        if (flexWrap == FlexWrap.WrapReverse) {
          if (isRtl) {
            layoutSingleChildHorizontal(
              item = child,
              flexLine = flexLine,
              left = childRight.roundToInt() - child.measuredWidth,
              top = childBottom - child.measuredHeight,
              right = childRight.roundToInt(),
              bottom = childBottom,
            )
          } else {
            layoutSingleChildHorizontal(
              item = child,
              flexLine = flexLine,
              left = childLeft.roundToInt(),
              top = childBottom - child.measuredHeight,
              right = childLeft.roundToInt() + child.measuredWidth,
              bottom = childBottom,
            )
          }
        } else {
          if (isRtl) {
            layoutSingleChildHorizontal(
              item = child,
              flexLine = flexLine,
              left = childRight.roundToInt() - child.measuredWidth,
              top = childTop,
              right = childRight.roundToInt(),
              bottom = childTop + child.measuredHeight,
            )
          } else {
            layoutSingleChildHorizontal(
              item = child,
              flexLine = flexLine,
              left = childLeft.roundToInt(),
              top = childTop,
              right = childLeft.roundToInt() + child.measuredWidth,
              bottom = childTop + child.measuredHeight,
            )
          }
        }
        childLeft += child.measuredWidth + spaceBetweenItem + child.margin.end
        childRight -= child.measuredWidth + spaceBetweenItem + child.margin.start
      }
      childTop += flexLine.crossSize
      childBottom -= flexLine.crossSize
    }
  }

  /**
   * Sub method for `onLayout` when the [FlexContainer.flexDirection] is either
   * [FlexDirection.Column] or [FlexDirection.ColumnReverse].
   *
   * @param isRtl `true` if the horizontal layout direction is right to left, `false` otherwise
   * @param fromBottomToTop `true` if the layout direction is bottom to top, `false` otherwise
   */
  private fun layoutVertical(
    isRtl: Boolean,
    fromBottomToTop: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    val paddingTop = padding.top
    val paddingBottom = padding.bottom
    val paddingRight = padding.end
    var childLeft = padding.start
    val width = right - left
    val height = bottom - top
    // childRight is used if the flexWrap is WrapReverse otherwise
    // childLeft is used to align the horizontal position of the children items.
    var childRight = width - paddingRight

    // Use float to reduce the round error that may happen in when justifyContent ==
    // SpaceBetween or SpaceAround.
    var childTop: Float

    // Used only for if the direction is from bottom to top
    var childBottom: Float
    for (flexLine in flexLines) {
      var spaceBetweenItem = 0f
      when (justifyContent) {
        JustifyContent.FlexStart -> {
          childTop = paddingTop.toFloat()
          childBottom = (height - paddingBottom).toFloat()
        }
        JustifyContent.FlexEnd -> {
          childTop = (height - flexLine.mainSize + paddingBottom).toFloat()
          childBottom = (flexLine.mainSize - paddingTop).toFloat()
        }
        JustifyContent.Center -> {
          childTop = paddingTop + (height - flexLine.mainSize) / 2f
          childBottom = height - paddingBottom - (height - flexLine.mainSize) / 2f
        }
        JustifyContent.SpaceAround -> {
          val visibleCount = flexLine.itemCountVisible
          if (visibleCount != 0) {
            spaceBetweenItem = ((height - flexLine.mainSize) / visibleCount.toFloat())
          }
          childTop = paddingTop + spaceBetweenItem / 2f
          childBottom = height - paddingBottom - spaceBetweenItem / 2f
        }
        JustifyContent.SpaceBetween -> {
          childTop = paddingTop.toFloat()
          val visibleCount = flexLine.itemCountVisible
          val denominator = if (visibleCount != 1) visibleCount - 1f else 1f
          spaceBetweenItem = (height - flexLine.mainSize) / denominator
          childBottom = (height - paddingBottom).toFloat()
        }
        JustifyContent.SpaceEvenly -> {
          val visibleCount = flexLine.itemCountVisible
          if (visibleCount != 0) {
            spaceBetweenItem = ((height - flexLine.mainSize) / (visibleCount + 1f))
          }
          childTop = paddingTop + spaceBetweenItem
          childBottom = height - paddingBottom - spaceBetweenItem
        }
        else -> throw AssertionError()
      }
      spaceBetweenItem = maxOf(spaceBetweenItem, 0f)
      for (i in 0 until flexLine.itemCount) {
        val index = flexLine.firstIndex + i
        val child = reorderedItems.getOrNull(index)
        if (child == null || !child.visible) {
          continue
        }
        childTop += child.margin.top.toFloat()
        childBottom -= child.margin.bottom.toFloat()
        if (isRtl) {
          if (fromBottomToTop) {
            layoutSingleChildVertical(
              item = child,
              flexLine = flexLine,
              isRtl = true,
              left = childRight - child.measuredWidth,
              top = childBottom.roundToInt() - child.measuredHeight,
              right = childRight,
              bottom = childBottom.roundToInt(),
            )
          } else {
            layoutSingleChildVertical(
              item = child,
              flexLine = flexLine,
              isRtl = true,
              left = childRight - child.measuredWidth,
              top = childTop.roundToInt(),
              right = childRight,
              bottom = childTop.roundToInt() + child.measuredHeight,
            )
          }
        } else {
          if (fromBottomToTop) {
            layoutSingleChildVertical(
              item = child,
              flexLine = flexLine,
              isRtl = false,
              left = childLeft,
              top = childBottom.roundToInt() - child.measuredHeight,
              right = childLeft + child.measuredWidth,
              bottom = childBottom.roundToInt(),
            )
          } else {
            layoutSingleChildVertical(
              item = child,
              flexLine = flexLine,
              isRtl = false,
              left = childLeft,
              top = childTop.roundToInt(),
              right = childLeft + child.measuredWidth,
              bottom = childTop.roundToInt() + child.measuredHeight,
            )
          }
        }
        childTop += child.measuredHeight + spaceBetweenItem + child.margin.bottom
        childBottom -= child.measuredHeight + spaceBetweenItem + child.margin.top
      }
      childLeft += flexLine.crossSize
      childRight -= flexLine.crossSize
    }
  }

  /**
   * The largest main size of all flex lines.
   */
  private fun getLargestMainSize(): Int {
    return if (flexLines.isEmpty()) Int.MIN_VALUE else flexLines.maxOf { it.mainSize }
  }

  /**
   * The sum of the cross sizes of all flex lines.
   */
  private fun getSumOfCrossSize(): Int {
    return flexLines.sumOf { it.crossSize }
  }

  /**
   * Call [Measurable.measure] and update [FlexItem.measuredWidth] and [FlexItem.measuredHeight]
   * with the result.
   */
  private fun FlexItem.applyMeasure(widthSpec: MeasureSpec, heightSpec: MeasureSpec) {
    val size = measurable.measure(widthSpec, heightSpec)
    this.measuredWidth = size.width
    this.measuredHeight = size.height
  }
}
