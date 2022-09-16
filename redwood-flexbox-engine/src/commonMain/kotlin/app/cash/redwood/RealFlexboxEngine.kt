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
@file:Suppress("MemberVisibilityCanBePrivate")

package app.cash.redwood

import app.cash.redwood.Node.Companion.DefaultFlexBasisPercent
import app.cash.redwood.Node.Companion.DefaultFlexGrow
import app.cash.redwood.Node.Companion.MatchParent
import app.cash.redwood.Node.Companion.UndefinedFlexShrink
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A class that measures and positions its children according to flexbox properties.
 */
public class RealFlexboxEngine {

  /**
   * The flex direction attribute of the flexbox.
   */
  public var flexDirection: FlexDirection = FlexDirection.Row

  /**
   * The flex wrap attribute of the flexbox.
   */
  public var flexWrap: FlexWrap = FlexWrap.NoWrap

  /**
   * The justify content attribute of the flexbox.
   */
  public var justifyContent: JustifyContent = JustifyContent.FlexStart

  /**
   * The align content attribute of the flexbox.
   */
  public var alignItems: AlignItems = AlignItems.FlexStart

  /**
   * The align items attribute of the flexbox.
   */
  public var alignContent: AlignContent = AlignContent.FlexStart

  /**
   * The padding of the flexbox.
   */
  public var padding: Spacing = Spacing.Zero

  /**
   * The current value of the maximum number of flex lines. If not set, -1 is returned.
   */
  public var maxLines: Int = -1

  /**
   * Returns the nodes contained in the flexbox.
   */
  public val nodes: MutableList<Node> = ObservableMutableList(
    onChange = { indexToReorderedIndex = null },
  )

  /**
   * The computed flex lines after calling [measure].
   */
  internal var flexLines = listOf<Line>()

  /**
   * Holds the reordered indices after [Node.order] has been taken into account.
   */
  private var indexToReorderedIndex: IntArray? = null

  /**
   * Holds the 'frozen' state of children during measure. If a view is frozen it will no longer
   * expand or shrink regardless of flex grow/flex shrink attributes.
   */
  private var childrenFrozen: BooleanArray? = null

  /**
   * Map the view index to the flex line which contains the view represented by the index to
   * look for a flex line from a given view index in a constant time.
   * Key: index of the view
   * Value: index of the flex line that contains the given view
   *
   * E.g. if we have following flex lines,
   *
   * FlexLine(0): itemCount 3
   * FlexLine(1): itemCount 2
   *
   * this instance should have following entries
   *
   * [0, 0, 0, 1, 1, ...]
   */
  internal var indexToFlexLine: IntArray? = null

  /**
   * Cache the measured spec. The first 32 bit represents the height measure spec, the last
   * 32 bit represents the width measure spec of each flex item.
   * E.g. an entry is created like `(long) heightMeasureSpec << 32 | widthMeasureSpec`
   *
   * To retrieve a widthMeasureSpec, call [unpackLower] or [unpackHigher] for a heightMeasureSpec.
   */
  private var measureSpecCache: LongArray? = null

  /**
   * Cache a flex item's measured width and height. The first 32 bit represents the height, the
   * last 32 bit represents the width of each flex item.
   * E.g. an entry is created like `(long) view.measuredHeight << 32 | view.measuredWidth`
   *
   * To retrieve a width value, call [unpackLower] or [unpackHigher] for a height value.
   */
  private var measuredSizeCache: LongArray? = null

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
   * Calculates how many flex lines are needed in the flex container layout by measuring each
   * child.
   * Expanding or shrinking the flex items depending on the flex grow and flex shrink
   * attributes are done in a later procedure, so the views' measured width and measured
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
  ): List<Line> {
    val isMainHorizontal = flexDirection.isMainAxisHorizontal
    val mainMode = mainMeasureSpec.mode
    val mainSize = mainMeasureSpec.size
    val flexLines = mutableListOf<Line>()
    var reachedToIndex = toIndex == -1
    val mainPaddingStart = getPaddingStartMain(isMainHorizontal)
    val mainPaddingEnd = getPaddingEndMain(isMainHorizontal)
    val crossPaddingStart = getPaddingStartCross(isMainHorizontal)
    val crossPaddingEnd = getPaddingEndCross(isMainHorizontal)
    var largestSizeInCross = Int.MIN_VALUE

    // The amount of cross size calculated in this method call.
    var sumCrossSize = 0

    // The index of the view in the flex line.
    var indexInFlexLine = 0
    var flexLine = Line()
    flexLine.firstIndex = fromIndex
    flexLine.mainSize = mainPaddingStart + mainPaddingEnd
    val childCount = nodes.size
    for (i in fromIndex until childCount) {
      val child = getReorderedChildAt(i)
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
      if (child.alignSelf == AlignSelf.Stretch) {
        flexLine.indicesAlignSelfStretch += i
      }
      var childMainSize = getFlexItemSizeMain(child, isMainHorizontal)
      if (child.flexBasisPercent != DefaultFlexBasisPercent && mainMode == MeasureSpecMode.Exactly) {
        childMainSize = (mainSize * child.flexBasisPercent).roundToInt()
        // Use the dimension from the layout if the mainMode is not
        // MeasureSpecMode.Exactly even if any fraction value is set to
        // layout_flexBasisPercent.
      }
      var childMainMeasureSpec: MeasureSpec
      var childCrossMeasureSpec: MeasureSpec
      if (isMainHorizontal) {
        childMainMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = mainMeasureSpec,
          padding = mainPaddingStart + mainPaddingEnd +
            getFlexItemMarginStartMain(child, true) +
            getFlexItemMarginEndMain(child, true),
          childDimension = childMainSize,
        )
        childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = crossMeasureSpec,
          padding = crossPaddingStart + crossPaddingEnd +
            getFlexItemMarginStartCross(child, true) +
            getFlexItemMarginEndCross(child, true) + sumCrossSize,
          childDimension = getFlexItemSizeCross(child, true),
        )
        child.measure(childMainMeasureSpec, childCrossMeasureSpec)
        updateMeasureCache(i, childMainMeasureSpec, childCrossMeasureSpec, child)
      } else {
        childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = crossMeasureSpec,
          padding = crossPaddingStart + crossPaddingEnd +
            getFlexItemMarginStartCross(child, false) +
            getFlexItemMarginEndCross(child, false) + sumCrossSize,
          childDimension = getFlexItemSizeCross(child, false),
        )
        childMainMeasureSpec = MeasureSpec.getChildMeasureSpec(
          spec = mainMeasureSpec,
          padding = mainPaddingStart + mainPaddingEnd +
            getFlexItemMarginStartMain(child, false) +
            getFlexItemMarginEndMain(child, false),
          childDimension = childMainSize,
        )
        child.measure(childCrossMeasureSpec, childMainMeasureSpec)
        updateMeasureCache(i, childCrossMeasureSpec, childMainMeasureSpec, child)
      }

      // Check the size constraint after the first measurement for the child
      // To prevent the child's width/height violate the size constraints imposed by the
      // {@link Node#getMinWidth()}, {@link Node#getMinHeight()},
      // {@link Node#getMaxWidth()} and {@link Node#getMaxHeight()} attributes.
      // E.g. When the child's layout_width is wrap_content the measured width may be
      // less than the min width after the first measurement.
      checkSizeConstraints(child, i)
      if (isWrapRequired(
          mode = mainMode,
          maxSize = mainSize,
          currentLength = flexLine.mainSize,
          childLength = getViewMeasuredSizeMain(child, isMainHorizontal) +
            getFlexItemMarginStartMain(child, isMainHorizontal) +
            getFlexItemMarginEndMain(child, isMainHorizontal),
          flexItem = child,
          flexLinesSize = flexLines.size,
        )
      ) {
        if (flexLine.itemCountVisible > 0) {
          addFlexLine(flexLines, flexLine, if (i > 0) i - 1 else 0, sumCrossSize)
          sumCrossSize += flexLine.crossSize
        }
        if (isMainHorizontal) {
          if (child.height == MatchParent) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_height is set to match_parent, the height
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the height of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
              spec = crossMeasureSpec,
              padding = padding.top + padding.bottom + child.margin.top +
                child.margin.bottom + sumCrossSize,
              childDimension = child.height,
            )
            child.measure(childMainMeasureSpec, childCrossMeasureSpec)
            checkSizeConstraints(child, i)
          }
        } else {
          if (child.width == MatchParent) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_width is set to match_parent, the width
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the width of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec = MeasureSpec.getChildMeasureSpec(
              spec = crossMeasureSpec,
              padding = padding.start + padding.end + child.margin.start +
                child.margin.end + sumCrossSize,
              childDimension = child.width,
            )
            child.measure(childCrossMeasureSpec, childMainMeasureSpec)
            checkSizeConstraints(child, i)
          }
        }
        flexLine = Line()
        flexLine.itemCount = 1
        flexLine.mainSize = mainPaddingStart + mainPaddingEnd
        flexLine.firstIndex = i
        indexInFlexLine = 0
        largestSizeInCross = Int.MIN_VALUE
      } else {
        flexLine.itemCount++
        indexInFlexLine++
      }
      flexLine.anyItemsHaveFlexGrow = flexLine.anyItemsHaveFlexGrow ||
        (child.flexGrow != DefaultFlexGrow)
      flexLine.anyItemsHaveFlexShrink = flexLine.anyItemsHaveFlexShrink ||
        (child.flexShrink != UndefinedFlexShrink)
      if (indexToFlexLine != null) {
        indexToFlexLine!![i] = flexLines.size
      }
      flexLine.mainSize += (
        getViewMeasuredSizeMain(child, isMainHorizontal) +
          getFlexItemMarginStartMain(child, isMainHorizontal) +
          getFlexItemMarginEndMain(child, isMainHorizontal)
        )
      flexLine.totalFlexGrow += child.flexGrow
      flexLine.totalFlexShrink += child.flexShrink
      largestSizeInCross = max(
        largestSizeInCross,
        getViewMeasuredSizeCross(child, isMainHorizontal) +
          getFlexItemMarginStartCross(child, isMainHorizontal) +
          getFlexItemMarginEndCross(child, isMainHorizontal),
      )
      // Temporarily set the cross axis length as the largest child in the flexLine
      // Expand along the cross axis depending on the alignContent property if needed
      // later
      flexLine.crossSize = max(flexLine.crossSize, largestSizeInCross)
      if (isMainHorizontal) {
        if (flexWrap != FlexWrap.WrapReverse) {
          flexLine.maxBaseline = max(
            flexLine.maxBaseline,
            child.baseline + child.margin.top,
          )
        } else {
          // if the flex wrap property is WRAP_REVERSE, calculate the
          // baseline as the distance from the cross end and the baseline
          // since the cross size calculation is based on the distance from the cross end
          flexLine.maxBaseline = max(
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
        // Calculated to include a flex line which includes the flex item having the
        // toIndex.
        // Let the sumCrossSize start from the negative value of the last flex line's
        // cross size because otherwise flex lines aren't calculated enough to fill the
        // visible area.
        sumCrossSize = -flexLine.crossSize
        reachedToIndex = true
      }
      if (sumCrossSize > needsCalcAmount && reachedToIndex) {
        // Stop the calculation if the sum of cross size calculated reached to the point
        // beyond the needsCalcAmount value to avoid unneeded calculation in a
        // RecyclerView.
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
   * Returns the container's start padding in the main axis. Either start or top.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the start padding in the main axis
   */
  private fun getPaddingStartMain(isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) padding.start else padding.top
  }

  /**
   * Returns the container's end padding in the main axis. Either end or bottom.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the end padding in the main axis
   */
  private fun getPaddingEndMain(isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) padding.end else padding.bottom
  }

  /**
   * Returns the container's start padding in the cross axis. Either start or top.
   *
   * @param isMainHorizontal is the main axis horizontal.
   * @return the start padding in the cross axis
   */
  private fun getPaddingStartCross(isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) padding.top else padding.start
  }

  /**
   * Returns the container's end padding in the cross axis. Either end or bottom.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the end padding in the cross axis
   */
  private fun getPaddingEndCross(isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) padding.bottom else padding.end
  }

  /**
   * Returns the view's measured size in the main axis. Either width or height.
   *
   * @param node the view
   * @param isMainHorizontal is the main axis horizontal
   * @return the view's measured size in the main axis
   */
  private fun getViewMeasuredSizeMain(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.measuredWidth else node.measuredHeight
  }

  /**
   * Returns the view's measured size in the cross axis. Either width or height.
   *
   * @param node the view
   * @param isMainHorizontal is the main axis horizontal
   * @return the view's measured size in the cross axis
   */
  private fun getViewMeasuredSizeCross(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.measuredHeight else node.measuredWidth
  }

  /**
   * Returns the flexItem's size in the main axis. Either width or height.
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's size in the main axis
   */
  private fun getFlexItemSizeMain(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.width else node.height
  }

  /**
   * Returns the flexItem's size in the cross axis. Either width or height.
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's size in the cross axis
   */
  private fun getFlexItemSizeCross(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.height else node.width
  }

  /**
   * Returns the flexItem's start margin in the main axis. Either start or top.
   *
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's start margin in the main axis
   */
  private fun getFlexItemMarginStartMain(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.margin.start else node.margin.top
  }

  /**
   * Returns the flexItem's end margin in the main axis. Either end or bottom.
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's end margin in the main axis
   */
  private fun getFlexItemMarginEndMain(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.margin.end else node.margin.bottom
  }

  /**
   * Returns the flexItem's start margin in the cross axis. Either start or top.
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's start margin in the cross axis
   */
  private fun getFlexItemMarginStartCross(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.margin.top else node.margin.start
  }

  /**
   * Returns the flexItem's end margin in the cross axis. Either end or bottom.
   *
   * @param node the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's end margin in the cross axis
   */
  private fun getFlexItemMarginEndCross(node: Node, isMainHorizontal: Boolean): Int {
    return if (isMainHorizontal) node.margin.bottom else node.margin.end
  }

  /**
   * Determine if a wrap is required (add a new flex line).
   *
   * @param mode the width or height mode along the main axis direction
   * @param maxSize the max size along the main axis direction
   * @param currentLength the accumulated current length
   * @param childLength the length of a child view which is to be collected to the flex line
   * @param flexItem the LayoutParams for the view being determined whether a new flex line is needed
   * @param flexLinesSize the number of the existing flexlines size
   * @return `true` if a wrap is required, `false` otherwise
   */
  private fun isWrapRequired(
    mode: MeasureSpecMode,
    maxSize: Int,
    currentLength: Int,
    childLength: Int,
    flexItem: Node,
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
    if (maxLines != -1 && maxLines <= flexLinesSize + 1) {
      return false
    }
    return maxSize < currentLength + childLength
  }

  private fun isLastFlexItem(
    childIndex: Int,
    childCount: Int,
    flexLine: Line,
  ) = childIndex == childCount - 1 && flexLine.itemCountVisible > 0

  private fun addFlexLine(
    flexLines: MutableList<Line>,
    flexLine: Line,
    viewIndex: Int,
    usedCrossSizeSoFar: Int,
  ) {
    flexLine.sumCrossSizeBefore = usedCrossSizeSoFar
    flexLine.lastIndex = viewIndex
    flexLines.add(flexLine)
  }

  /**
   * Checks if the view's width/height don't violate the minimum/maximum size constraints imposed
   * by the [Node.minWidth], [Node.minHeight], [Node.maxWidth] and [Node.maxHeight] attributes.
   *
   * @param node the view to be checked
   * @param index index of the view
   */
  private fun checkSizeConstraints(node: Node, index: Int) {
    var needsMeasure = false
    var childWidth = node.measuredWidth
    var childHeight = node.measuredHeight
    if (childWidth < node.minWidth) {
      needsMeasure = true
      childWidth = node.minWidth
    } else if (childWidth > node.maxWidth) {
      needsMeasure = true
      childWidth = node.maxWidth
    }
    if (childHeight < node.minHeight) {
      needsMeasure = true
      childHeight = node.minHeight
    } else if (childHeight > node.maxHeight) {
      needsMeasure = true
      childHeight = node.maxHeight
    }
    if (needsMeasure) {
      val widthSpec = MeasureSpec.from(childWidth, MeasureSpecMode.Exactly)
      val heightSpec = MeasureSpec.from(childHeight, MeasureSpecMode.Exactly)
      node.measure(widthSpec, heightSpec)
      updateMeasureCache(index, widthSpec, heightSpec, node)
    }
  }

  /**
   * Determine the main size by expanding (shrinking if negative remaining free space is given)
   * an individual child in each flex line if any children's flexGrow (or flexShrink if remaining
   * space is negative) properties are set to non-zero.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  internal fun determineMainSize(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    fromIndex: Int = 0,
  ) {
    ensureChildrenFrozen(nodes.size)
    if (fromIndex >= nodes.size) {
      return
    }
    val mainSize: Int
    val paddingAlongMainAxis: Int
    when (flexDirection) {
      FlexDirection.Row, FlexDirection.RowReverse -> {
        val widthMode = widthMeasureSpec.mode
        val widthSize = widthMeasureSpec.size
        val largestMainSize = getLargestMainSize()
        mainSize = if (widthMode == MeasureSpecMode.Exactly) {
          widthSize
        } else {
          min(largestMainSize, widthSize)
        }
        paddingAlongMainAxis = (padding.start + padding.end)
      }
      FlexDirection.Column, FlexDirection.ColumnReverse -> {
        val heightMode = heightMeasureSpec.mode
        val heightSize = heightMeasureSpec.size
        mainSize = if (heightMode == MeasureSpecMode.Exactly) {
          heightSize
        } else {
          getLargestMainSize()
        }
        paddingAlongMainAxis = (padding.top + padding.bottom)
      }
      else -> error("Invalid FlexDirection: $flexDirection")
    }
    var flexLineIndex = 0
    if (indexToFlexLine != null) {
      flexLineIndex = indexToFlexLine!![fromIndex]
    }
    var i = flexLineIndex
    val size = flexLines.size
    while (i < size) {
      val flexLine = flexLines[i]
      if (flexLine.mainSize < mainSize && flexLine.anyItemsHaveFlexGrow) {
        expandFlexItems(
          widthMeasureSpec = widthMeasureSpec,
          heightMeasureSpec = heightMeasureSpec,
          flexLine = flexLine,
          maxMainSize = mainSize,
          paddingAlongMainAxis = paddingAlongMainAxis,
          calledRecursively = false,
        )
      } else if (flexLine.mainSize > mainSize && flexLine.anyItemsHaveFlexShrink) {
        shrinkFlexItems(
          widthMeasureSpec = widthMeasureSpec,
          heightMeasureSpec = heightMeasureSpec,
          flexLine = flexLine,
          maxMainSize = mainSize,
          paddingAlongMainAxis = paddingAlongMainAxis,
          calledRecursively = false,
        )
      }
      i++
    }
  }

  private fun ensureChildrenFrozen(size: Int) {
    if (childrenFrozen == null) {
      childrenFrozen = BooleanArray(max(size, 10))
    } else if (childrenFrozen!!.size < size) {
      val newCapacity = childrenFrozen!!.size * 2
      childrenFrozen = BooleanArray(max(newCapacity, size))
    } else {
      childrenFrozen!!.fill(false)
    }
  }

  /**
   * Expand the flex items along the main axis based on the individual [Node.flexGrow] attribute.
   *
   * @param widthMeasureSpec the horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Expanded main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   */
  private fun expandFlexItems(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    flexLine: Line,
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
      val child = getReorderedChildAt(index)
      if (child == null || !child.visible) {
        continue
      }
      if (flexDirection == FlexDirection.Row || flexDirection == FlexDirection.RowReverse) {
        // The direction of the main axis is horizontal
        var childMeasuredWidth = child.measuredWidth
        if (measuredSizeCache != null) {
          // Retrieve the measured width from the cache because there
          // are some cases that the view is re-created from the last measure, thus
          // View#getMeasuredWidth returns 0.
          // E.g. if the flex container is FlexboxLayoutManager, the case happens
          // frequently
          childMeasuredWidth = unpackLower(measuredSizeCache!![index])
        }
        var childMeasuredHeight = child.measuredHeight
        if (measuredSizeCache != null) {
          // Extract the measured height from the cache
          childMeasuredHeight = unpackHigher(measuredSizeCache!![index])
        }
        if (!childrenFrozen!![index] && child.flexGrow > 0f) {
          var rawCalculatedWidth = (childMeasuredWidth + unitSpace * child.flexGrow)
          if (i == flexLine.itemCount - 1) {
            rawCalculatedWidth += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newWidth = rawCalculatedWidth.roundToInt()
          if (newWidth > child.maxWidth) {
            // This means the child can't expand beyond the value of the maxWidth
            // attribute.
            // To adjust the flex line length to the size of maxMainSize, remaining
            // positive free space needs to be re-distributed to other flex items
            // (children views). In that case, invoke this method again with the same
            // fromIndex.
            needsReexpand = true
            newWidth = child.maxWidth
            childrenFrozen!![index] = true
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
          child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
          updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec, child)
        }
        largestCrossSize = max(
          largestCrossSize,
          childMeasuredHeight + child.margin.top + child.margin.bottom,
        )
        flexLine.mainSize += (childMeasuredWidth + child.margin.start + child.margin.end)
      } else {
        // The direction of the main axis is vertical
        var childMeasuredHeight = child.measuredHeight
        if (measuredSizeCache != null) {
          // Retrieve the measured height from the cache because there
          // are some cases that the view is re-created from the last measure, thus
          // View#getMeasuredHeight returns 0.
          // E.g. if the flex container is FlexboxLayoutManager, that case happens
          // frequently
          childMeasuredHeight = unpackHigher(measuredSizeCache!![index])
        }
        var childMeasuredWidth = child.measuredWidth
        if (measuredSizeCache != null) {
          // Extract the measured width from the cache
          childMeasuredWidth = unpackLower(measuredSizeCache!![index])
        }
        if (!childrenFrozen!![index] && child.flexGrow > 0f) {
          var rawCalculatedHeight = (childMeasuredHeight + unitSpace * child.flexGrow)
          if (i == flexLine.itemCount - 1) {
            rawCalculatedHeight += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newHeight = rawCalculatedHeight.roundToInt()
          if (newHeight > child.maxHeight) {
            // This means the child can't expand beyond the value of the maxHeight
            // attribute.
            // To adjust the flex line length to the size of maxMainSize, remaining
            // positive free space needs to be re-distributed to other flex items
            // (children views). In that case, invoke this method again with the same
            // fromIndex.
            needsReexpand = true
            newHeight = child.maxHeight
            childrenFrozen!![index] = true
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
            widthMeasureSpec,
            child,
            flexLine.sumCrossSizeBefore,
          )
          val childHeightMeasureSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
          child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
          updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec, child)
        }
        largestCrossSize = max(
          largestCrossSize,
          childMeasuredWidth + child.margin.start + child.margin.end,
        )
        flexLine.mainSize += (childMeasuredHeight + child.margin.top + child.margin.bottom)
      }
      flexLine.crossSize = max(flexLine.crossSize, largestCrossSize)
    }
    if (needsReexpand && sizeBeforeExpand != flexLine.mainSize) {
      // Re-invoke the method with the same flex line to distribute the positive free space
      // that wasn't fully distributed (because of maximum length constraint)
      expandFlexItems(
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
   * Shrink the flex items along the main axis based on the individual [Node.flexShrink] attribute.
   *
   * @param widthMeasureSpec the horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Shrank main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   */
  private fun shrinkFlexItems(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    flexLine: Line,
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
      val child = getReorderedChildAt(index)
      if (child == null || !child.visible) {
        continue
      }
      if (flexDirection == FlexDirection.Row || flexDirection == FlexDirection.RowReverse) {
        // The direction of main axis is horizontal
        var childMeasuredWidth = child.measuredWidth
        if (measuredSizeCache != null) {
          // Retrieve the measured width from the cache because there
          // are some cases that the view is re-created from the last measure, thus
          // View#getMeasuredWidth returns 0.
          // E.g. if the flex container is FlexboxLayoutManager, the case happens
          // frequently
          childMeasuredWidth = unpackLower(measuredSizeCache!![index])
        }
        var childMeasuredHeight = child.measuredHeight
        if (measuredSizeCache != null) {
          // Extract the measured height from the cache
          childMeasuredHeight = unpackHigher(measuredSizeCache!![index])
        }
        if (!childrenFrozen!![index] && child.flexShrink > 0f) {
          var rawCalculatedWidth = (childMeasuredWidth - unitShrink * child.flexShrink)
          if (i == flexLine.itemCount - 1) {
            rawCalculatedWidth += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newWidth = rawCalculatedWidth.roundToInt()
          if (newWidth < child.minWidth) {
            // This means the child doesn't have enough space to distribute the negative
            // free space. To adjust the flex line length down to the maxMainSize,
            // remaining
            // negative free space needs to be re-distributed to other flex items
            // (children views). In that case, invoke this method again with the same
            // fromIndex.
            needsReshrink = true
            newWidth = child.minWidth
            childrenFrozen!![index] = true
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
          child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
          updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec, child)
        }
        largestCrossSize = max(
          largestCrossSize,
          childMeasuredHeight + child.margin.top + child.margin.bottom,
        )
        flexLine.mainSize += (childMeasuredWidth + child.margin.start + child.margin.end)
      } else {
        // The direction of main axis is vertical
        var childMeasuredHeight = child.measuredHeight
        if (measuredSizeCache != null) {
          // Retrieve the measured height from the cache because there
          // are some cases that the view is re-created from the last measure, thus
          // View#getMeasuredHeight returns 0.
          // E.g. if the flex container is FlexboxLayoutManager, that case happens
          // frequently
          childMeasuredHeight = unpackHigher(measuredSizeCache!![index])
        }
        var childMeasuredWidth = child.measuredWidth
        if (measuredSizeCache != null) {
          // Extract the measured width from the cache
          childMeasuredWidth = unpackLower(measuredSizeCache!![index])
        }
        if (!childrenFrozen!![index] && child.flexShrink > 0f) {
          var rawCalculatedHeight = (
            childMeasuredHeight -
              unitShrink * child.flexShrink
            )
          if (i == flexLine.itemCount - 1) {
            rawCalculatedHeight += accumulatedRoundError
            accumulatedRoundError = 0f
          }
          var newHeight = rawCalculatedHeight.roundToInt()
          if (newHeight < child.minHeight) {
            // Need to invoke this method again like the case flex direction is vertical
            needsReshrink = true
            newHeight = child.minHeight
            childrenFrozen!![index] = true
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
            widthMeasureSpec,
            child,
            flexLine.sumCrossSizeBefore,
          )
          val childHeightMeasureSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
          child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
          childMeasuredWidth = child.measuredWidth
          childMeasuredHeight = child.measuredHeight
          updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec, child)
        }
        largestCrossSize = max(
          largestCrossSize,
          childMeasuredWidth + child.margin.start + child.margin.end,
        )
        flexLine.mainSize += (childMeasuredHeight + child.margin.top + child.margin.bottom)
      }
      flexLine.crossSize = max(flexLine.crossSize, largestCrossSize)
    }
    if (needsReshrink && sizeBeforeShrink != flexLine.mainSize) {
      // Re-invoke the method with the same fromIndex to distribute the negative free space
      // that wasn't fully distributed (because some views length were not enough)
      shrinkFlexItems(
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
    flexItem: Node,
    padding: Int,
  ): MeasureSpec {
    var childWidthMeasureSpec = MeasureSpec.getChildMeasureSpec(
      spec = widthMeasureSpec,
      padding = this.padding.start + this.padding.end + flexItem.margin.start +
        flexItem.margin.end + padding,
      childDimension = flexItem.width,
    )
    val childWidth = childWidthMeasureSpec.size
    if (childWidth > flexItem.maxWidth) {
      childWidthMeasureSpec = MeasureSpec.from(flexItem.maxWidth, childWidthMeasureSpec.mode)
    } else if (childWidth < flexItem.minWidth) {
      childWidthMeasureSpec = MeasureSpec.from(flexItem.minWidth, childWidthMeasureSpec.mode)
    }
    return childWidthMeasureSpec
  }

  private fun getChildHeightMeasureSpecInternal(
    heightMeasureSpec: MeasureSpec,
    flexItem: Node,
    padding: Int,
  ): MeasureSpec {
    var childHeightMeasureSpec = MeasureSpec.getChildMeasureSpec(
      spec = heightMeasureSpec,
      padding = this.padding.top + this.padding.bottom + flexItem.margin.top +
        flexItem.margin.bottom + padding,
      childDimension = flexItem.height,
    )
    val childHeight = childHeightMeasureSpec.size
    if (childHeight > flexItem.maxHeight) {
      childHeightMeasureSpec = MeasureSpec.from(flexItem.maxHeight, childHeightMeasureSpec.mode)
    } else if (childHeight < flexItem.minHeight) {
      childHeightMeasureSpec = MeasureSpec.from(flexItem.minHeight, childHeightMeasureSpec.mode)
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
   * @param paddingAlongCrossAxis the padding value for the FlexboxLayout along the cross axis
   */
  internal fun determineCrossSize(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    paddingAlongCrossAxis: Int,
  ) {
    val mode: MeasureSpecMode // The MeasureSpec mode along the cross axis
    val size: Int // The MeasureSpec size along the cross axis
    when (flexDirection) {
      FlexDirection.Row, FlexDirection.RowReverse -> {
        mode = heightMeasureSpec.mode
        size = heightMeasureSpec.size
      }
      FlexDirection.Column, FlexDirection.ColumnReverse -> {
        mode = widthMeasureSpec.mode
        size = widthMeasureSpec.size
      }
      else -> error("Invalid FlexDirection: $flexDirection")
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
            val newFlexLines = ArrayList<Line>()
            val dummySpaceFlexLine = Line()
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
            val newFlexLines = ArrayList<Line>()
            var i = 0
            val flexLineSize = flexLines.size
            while (i < flexLineSize) {
              val flexLine = flexLines[i]
              newFlexLines.add(flexLine)
              if (i != flexLines.size - 1) {
                val dummySpaceFlexLine = Line()
                if (i == flexLines.size - 2) {
                  // The last dummy space block in the flex container.
                  // Adjust the cross size by the accumulated error.
                  dummySpaceFlexLine.crossSize =
                    (spaceBetweenFlexLine + accumulatedError).roundToInt()
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
            val dummySpaceFlexLine = Line()
            dummySpaceFlexLine.crossSize = spaceTop
            flexLines = flexLines.toMutableList().apply { add(0, dummySpaceFlexLine) }
          }
        }
      }
    }
  }

  private fun constructFlexLinesForAlignContentCenter(
    flexLines: List<Line>,
    size: Int,
    totalCrossSize: Int,
  ): List<Line> {
    var spaceAboveAndBottom = size - totalCrossSize
    spaceAboveAndBottom /= 2
    val newFlexLines = ArrayList<Line>()
    val dummySpaceFlexLine = Line()
    dummySpaceFlexLine.crossSize = spaceAboveAndBottom
    var i = 0
    val flexLineSize = flexLines.size
    while (i < flexLineSize) {
      if (i == 0) {
        newFlexLines.add(dummySpaceFlexLine)
      }
      val flexLine = flexLines[i]
      newFlexLines.add(flexLine)
      if (i == flexLines.size - 1) {
        newFlexLines.add(dummySpaceFlexLine)
      }
      i++
    }
    return newFlexLines
  }

  /**
   * Expand the view if the [FlexboxEngine.alignItems] attribute is set to
   * [AlignItems.Stretch] or [Node.alignSelf] is set as [AlignItems.Stretch].
   *
   * @param fromIndex the index from which value, stretch is calculated
   */
  internal fun stretchViews(fromIndex: Int = 0) {
    if (fromIndex >= nodes.size) {
      return
    }
    if (alignItems == AlignItems.Stretch) {
      var flexLineIndex = 0
      if (indexToFlexLine != null) {
        flexLineIndex = indexToFlexLine!![fromIndex]
      }
      var i = flexLineIndex
      val size = flexLines.size
      while (i < size) {
        val flexLine = flexLines[i]
        var j = 0
        val itemCount = flexLine.itemCount
        while (j < itemCount) {
          val viewIndex = flexLine.firstIndex + j
          if (j >= nodes.size) {
            j++
            continue
          }
          val node = getReorderedChildAt(viewIndex)
          if (node == null || !node.visible) {
            j++
            continue
          }
          if (node.alignSelf != AlignSelf.Auto && node.alignSelf != AlignSelf.Stretch) {
            j++
            continue
          }
          when (flexDirection) {
            FlexDirection.Row, FlexDirection.RowReverse -> stretchViewVertically(
              node = node,
              crossSize = flexLine.crossSize,
              index = viewIndex,
            )
            FlexDirection.Column, FlexDirection.ColumnReverse -> stretchViewHorizontally(
              node = node,
              crossSize = flexLine.crossSize,
              index = viewIndex,
            )
            else -> error("Invalid FlexDirection: $flexDirection")
          }
          j++
        }
        i++
      }
    } else {
      for (flexLine in flexLines) {
        for (index in flexLine.indicesAlignSelfStretch) {
          val view = getReorderedChildAt(index)!!
          when (flexDirection) {
            FlexDirection.Row, FlexDirection.RowReverse -> stretchViewVertically(
              node = view,
              crossSize = flexLine.crossSize,
              index = index,
            )
            FlexDirection.Column, FlexDirection.ColumnReverse -> stretchViewHorizontally(
              node = view,
              crossSize = flexLine.crossSize,
              index = index,
            )
            else -> throw IllegalArgumentException("Invalid flex direction: $flexDirection")
          }
        }
      }
    }
  }

  /**
   * Expand the view vertically to the size of the [crossSize] (considering the view margins)
   *
   * @param node the View to be stretched
   * @param crossSize the cross size
   * @param index the index of the view
   */
  private fun stretchViewVertically(node: Node, crossSize: Int, index: Int) {
    var newHeight = crossSize - node.margin.top - node.margin.bottom
    newHeight = max(newHeight, node.minHeight)
    newHeight = min(newHeight, node.maxHeight)
    val measuredWidth = if (measuredSizeCache != null) {
      // Retrieve the measured height from the cache because there
      // are some cases that the view is re-created from the last measure, thus
      // View#getMeasuredHeight returns 0.
      // E.g. if the flex container is FlexboxLayoutManager, that case happens
      // frequently
      unpackLower(measuredSizeCache!![index])
    } else {
      node.measuredWidth
    }
    val childWidthSpec = MeasureSpec.from(measuredWidth, MeasureSpecMode.Exactly)
    val childHeightSpec = MeasureSpec.from(newHeight, MeasureSpecMode.Exactly)
    node.measure(childWidthSpec, childHeightSpec)
    updateMeasureCache(index, childWidthSpec, childHeightSpec, node)
  }

  /**
   * Expand the view horizontally to the size of the crossSize (considering the view margins)
   *
   * @param node      the View to be stretched
   * @param crossSize the cross size
   * @param index     the index of the view
   */
  private fun stretchViewHorizontally(node: Node, crossSize: Int, index: Int) {
    var newWidth = crossSize - node.margin.start - node.margin.end
    newWidth = max(newWidth, node.minWidth)
    newWidth = min(newWidth, node.maxWidth)
    val measuredHeight = if (measuredSizeCache != null) {
      // Retrieve the measured height from the cache because there
      // are some cases that the view is re-created from the last measure, thus
      // View#getMeasuredHeight returns 0.
      // E.g. if the flex container is FlexboxLayoutManager, that case happens
      // frequently
      unpackHigher(measuredSizeCache!![index])
    } else {
      node.measuredHeight
    }
    val childHeightSpec = MeasureSpec.from(measuredHeight, MeasureSpecMode.Exactly)
    val childWidthSpec = MeasureSpec.from(newWidth, MeasureSpecMode.Exactly)
    node.measure(childWidthSpec, childHeightSpec)
    updateMeasureCache(index, childWidthSpec, childHeightSpec, node)
  }

  /**
   * Place a single View when the layout direction is horizontal
   * ([FlexboxEngine.flexDirection] is either [FlexDirection.Row] or
   * [FlexDirection.RowReverse]).
   *
   * @param node the View to be placed
   * @param flexLine the [Line] where the View belongs to
   * @param left the left position of the View, which the View's margin is already taken
   * into account
   * @param top the top position of the flex line where the View belongs to. The actual
   * View's top position is shifted depending on the flexWrap and alignItems attributes
   * @param right the right position of the View, which the View's margin is already taken
   * into account
   * @param bottom the bottom position of the flex line where the View belongs to. The actual
   * View's bottom position is shifted depending on the flexWrap and alignItems attributes
   * @see Node.alignSelf
   */
  private fun layoutSingleChildHorizontal(
    node: Node,
    flexLine: Line,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    var alignItems = alignItems
    if (node.alignSelf != AlignSelf.Auto) {
      // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
      // Assigning the alignSelf value as alignItems should work.
      alignItems = AlignItems(node.alignSelf.ordinal)
    }
    val crossSize = flexLine.crossSize
    when (alignItems) {
      AlignItems.FlexStart, AlignItems.Stretch -> if (flexWrap != FlexWrap.WrapReverse) {
        node.layout(left, top + node.margin.top, right, bottom + node.margin.top)
      } else {
        node.layout(left, top - node.margin.bottom, right, bottom - node.margin.bottom)
      }
      AlignItems.Baseline -> if (flexWrap != FlexWrap.WrapReverse) {
        val marginTop = max(flexLine.maxBaseline - node.baseline, node.margin.top)
        node.layout(left, top + marginTop, right, bottom + marginTop)
      } else {
        val marginBottom = max(flexLine.maxBaseline - node.measuredHeight + node.baseline, node.margin.bottom)
        node.layout(left, top - marginBottom, right, bottom - marginBottom)
      }
      AlignItems.FlexEnd -> if (flexWrap != FlexWrap.WrapReverse) {
        node.layout(
          left = left,
          top = top + crossSize - node.measuredHeight - node.margin.bottom,
          right = right,
          bottom = top + crossSize - node.margin.bottom,
        )
      } else {
        // If the flexWrap == WRAP_REVERSE, the direction of the
        // flexEnd is flipped (from top to bottom).
        node.layout(
          left = left,
          top = top - crossSize + node.measuredHeight + node.margin.top,
          right = right,
          bottom = bottom - crossSize + node.measuredHeight + node.margin.top,
        )
      }
      AlignItems.Center -> {
        val topFromCrossAxis = (crossSize - node.measuredHeight + node.margin.top - node.margin.bottom) / 2
        if (flexWrap != FlexWrap.WrapReverse) {
          node.layout(
            left = left,
            top = top + topFromCrossAxis,
            right = right,
            bottom = top + topFromCrossAxis + node.measuredHeight,
          )
        } else {
          node.layout(
            left = left,
            top = top - topFromCrossAxis,
            right = right,
            bottom = top - topFromCrossAxis + node.measuredHeight,
          )
        }
      }
    }
  }

  /**
   * Place a single View when the layout direction is vertical
   * ([FlexboxEngine.flexDirection] is either [FlexDirection.Column] or
   * [FlexDirection.ColumnReverse]).
   *
   * @param node     the View to be placed
   * @param flexLine the [Line] where the View belongs to
   * @param isRtl    `true` if the layout direction is right to left, `false`
   * otherwise
   * @param left     the left position of the flex line where the View belongs to. The actual
   * View's left position is shifted depending on the isLayoutRtl and alignItems
   * attributes
   * @param top      the top position of the View, which the View's margin is already taken
   * into account
   * @param right    the right position of the flex line where the View belongs to. The actual
   * View's right position is shifted depending on the isLayoutRtl and alignItems
   * attributes
   * @param bottom   the bottom position of the View, which the View's margin is already taken
   * into account
   * @see Node.alignSelf
   */
  private fun layoutSingleChildVertical(
    node: Node,
    flexLine: Line,
    isRtl: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
  ) {
    var alignItems = alignItems
    if (node.alignSelf != AlignSelf.Auto) {
      // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
      // Assigning the alignSelf value as alignItems should work.
      alignItems = AlignItems(node.alignSelf.ordinal)
    }
    val crossSize = flexLine.crossSize
    when (alignItems) {
      AlignItems.FlexStart, AlignItems.Stretch, AlignItems.Baseline -> if (!isRtl) {
        node.layout(
          left = left + node.margin.start,
          top = top,
          right = right + node.margin.start,
          bottom = bottom,
        )
      } else {
        node.layout(
          left = left - node.margin.end,
          top = top,
          right = right - node.margin.end,
          bottom = bottom,
        )
      }
      AlignItems.FlexEnd -> if (!isRtl) {
        node.layout(
          left = left + crossSize - node.measuredWidth - node.margin.end,
          top = top,
          right = right + crossSize - node.measuredWidth - node.margin.end,
          bottom = bottom,
        )
      } else {
        // If the flexWrap == WRAP_REVERSE, the direction of the
        // flexEnd is flipped (from left to right).
        node.layout(
          left = left - crossSize + node.measuredWidth + node.margin.start,
          top = top,
          right = right - crossSize + node.measuredWidth + node.margin.start,
          bottom = bottom,
        )
      }
      AlignItems.Center -> {
        val leftFromCrossAxis = (crossSize - node.measuredWidth + node.margin.start - node.margin.end) / 2
        if (!isRtl) {
          node.layout(left + leftFromCrossAxis, top, right + leftFromCrossAxis, bottom)
        } else {
          node.layout(left - leftFromCrossAxis, top, right - leftFromCrossAxis, bottom)
        }
      }
    }
  }

  private fun updateMeasureCache(
    index: Int,
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
    node: Node,
  ) {
    if (measureSpecCache != null) {
      measureSpecCache!![index] = packLong(widthMeasureSpec.value, heightMeasureSpec.value)
    }
    if (measuredSizeCache != null) {
      measuredSizeCache!![index] = packLong(node.measuredWidth, node.measuredHeight)
    }
  }

  internal fun ensureIndexToFlexLine(size: Int) {
    if (indexToFlexLine == null) {
      indexToFlexLine = IntArray(max(size, 10))
    } else if (indexToFlexLine!!.size < size) {
      val newCapacity = max(indexToFlexLine!!.size * 2, size)
      indexToFlexLine = indexToFlexLine!!.copyOf(newCapacity)
    }
  }

  public fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    return when (flexDirection) {
      FlexDirection.Row, FlexDirection.RowReverse -> measureHorizontal(widthSpec, heightSpec)
      FlexDirection.Column, FlexDirection.ColumnReverse -> measureVertical(widthSpec, heightSpec)
      else -> error("Invalid FlexDirection: $flexDirection")
    }
  }

  /**
   * Sub method for `onMeasure`, when the main axis direction is horizontal
   * (either left to right or right to left).
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  private fun measureHorizontal(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    val flexLines = calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    determineMainSize(widthMeasureSpec, heightMeasureSpec)

    if (alignItems == AlignItems.Baseline) {
      for (flexLine in flexLines) {
        // The largest height value that also take the baseline shift into account
        var largestHeightInLine = Int.MIN_VALUE
        for (i in 0 until flexLine.itemCount) {
          val viewIndex = flexLine.firstIndex + i
          val child = getReorderedChildAt(viewIndex)
          if (child == null || !child.visible) {
            continue
          }
          largestHeightInLine = if (flexWrap != FlexWrap.WrapReverse) {
            val marginTop = max(flexLine.maxBaseline - child.baseline, child.margin.top)
            child.measuredHeight + marginTop + child.margin.bottom
          } else {
            val marginBottom = max(
              flexLine.maxBaseline - child.measuredHeight +
                child.baseline,
              child.margin.bottom,
            )
            child.measuredHeight + child.margin.top + marginBottom
          }.coerceAtLeast(largestHeightInLine)
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
    // Expand the views if alignItems (or alignSelf in each child view) is set to stretch
    stretchViews()
    return setMeasuredDimensionForFlex(
      flexDirection = flexDirection,
      widthMeasureSpec = widthMeasureSpec,
      heightMeasureSpec = heightMeasureSpec,
    )
  }

  /**
   * Sub method for `onMeasure` when the main axis direction is vertical
   * (either from top to bottom or bottom to top).
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  private fun measureVertical(
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    determineMainSize(widthMeasureSpec, heightMeasureSpec)
    determineCrossSize(
      widthMeasureSpec = widthMeasureSpec,
      heightMeasureSpec = heightMeasureSpec,
      paddingAlongCrossAxis = padding.start + padding.end,
    )
    // Now cross size for each flex line is determined.
    // Expand the views if alignItems (or alignSelf in each child view) is set to stretch
    stretchViews()
    return setMeasuredDimensionForFlex(
      flexDirection = flexDirection,
      widthMeasureSpec = widthMeasureSpec,
      heightMeasureSpec = heightMeasureSpec,
    )
  }

  /**
   * Set this FlexboxLayouts' width and height depending on the calculated size of main axis and
   * cross axis.
   *
   * @param flexDirection the value of the flex direction
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  private fun setMeasuredDimensionForFlex(
    flexDirection: FlexDirection,
    widthMeasureSpec: MeasureSpec,
    heightMeasureSpec: MeasureSpec,
  ): Size {
    val calculatedMaxHeight: Int
    val calculatedMaxWidth: Int
    when (flexDirection) {
      FlexDirection.Row, FlexDirection.RowReverse -> {
        calculatedMaxHeight = getSumOfCrossSize() + padding.top + padding.bottom
        calculatedMaxWidth = getLargestMainSize()
      }
      FlexDirection.Column, FlexDirection.ColumnReverse -> {
        calculatedMaxHeight = getLargestMainSize()
        calculatedMaxWidth = getSumOfCrossSize() + padding.start + padding.end
      }
      else -> error("Invalid FlexDirection: $flexDirection")
    }
    val widthSize = widthMeasureSpec.size
    val width = when (val widthMode = widthMeasureSpec.mode) {
      MeasureSpecMode.Exactly -> {
        MeasureSpec.resolveSize(widthSize, widthMeasureSpec)
      }
      MeasureSpecMode.AtMost -> {
        MeasureSpec.resolveSize(min(widthSize, calculatedMaxWidth), widthMeasureSpec)
      }
      MeasureSpecMode.Unspecified -> {
        MeasureSpec.resolveSize(calculatedMaxWidth, widthMeasureSpec)
      }
      else -> error("Unknown width mode: $widthMode")
    }
    val heightSize = heightMeasureSpec.size
    val height = when (val heightMode = heightMeasureSpec.mode) {
      MeasureSpecMode.Exactly -> {
        MeasureSpec.resolveSize(heightSize, heightMeasureSpec)
      }
      MeasureSpecMode.AtMost -> {
        MeasureSpec.resolveSize(min(heightSize, calculatedMaxHeight), heightMeasureSpec)
      }
      MeasureSpecMode.Unspecified -> {
        MeasureSpec.resolveSize(calculatedMaxHeight, heightMeasureSpec)
      }
      else -> error("Unknown height mode: $heightMode")
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
      else -> error("Invalid FlexDirection: $flexDirection")
    }
  }

  /**
   * Sub method for `onLayout` when the [FlexboxEngine.flexDirection] is either
   * [FlexDirection.Row] or [FlexDirection.RowReverse].
   *
   * @param isRtl `true` if the horizontal layout direction is right to left, `false` otherwise.
   * @param left the left position of this View
   * @param top the top position of this View
   * @param right the right position of this View
   * @param bottom the bottom position of this View
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
    // SPACE_BETWEEN or SPACE_AROUND
    var childLeft: Float
    val height = bottom - top
    val width = right - left
    // childBottom is used if the flexWrap is WRAP_REVERSE otherwise
    // childTop is used to align the vertical position of the children views.
    var childBottom = height - padding.bottom
    var childTop = padding.top

    // Used only for RTL layout
    // Use float to reduce the round error that may happen in when justifyContent ==
    // SPACE_BETWEEN or SPACE_AROUND
    var childRight: Float
    var i = 0
    val size = flexLines.size
    while (i < size) {
      val flexLine = flexLines[i]
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
        else -> error("Invalid JustifyContent: $justifyContent")
      }
      spaceBetweenItem = max(spaceBetweenItem, 0f)
      for (j in 0 until flexLine.itemCount) {
        val index = flexLine.firstIndex + j
        val child = getReorderedChildAt(index)
        if (child == null || !child.visible) {
          continue
        }
        childLeft += child.margin.start.toFloat()
        childRight -= child.margin.end.toFloat()
        if (flexWrap == FlexWrap.WrapReverse) {
          if (isRtl) {
            layoutSingleChildHorizontal(
              node = child,
              flexLine = flexLine,
              left = childRight.roundToInt() - child.measuredWidth,
              top = childBottom - child.measuredHeight,
              right = childRight.roundToInt(),
              bottom = childBottom,
            )
          } else {
            layoutSingleChildHorizontal(
              node = child,
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
              node = child,
              flexLine = flexLine,
              left = childRight.roundToInt() - child.measuredWidth,
              top = childTop,
              right = childRight.roundToInt(),
              bottom = childTop + child.measuredHeight,
            )
          } else {
            layoutSingleChildHorizontal(
              node = child,
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
      i++
    }
  }

  /**
   * Sub method for `onLayout` when the [FlexboxEngine.flexDirection] is either
   * [FlexDirection.Column] or [FlexDirection.ColumnReverse].
   *
   * @param isRtl `true` if the horizontal layout direction is right to left, `false` otherwise
   * @param fromBottomToTop `true` if the layout direction is bottom to top, `false` otherwise
   * @param left the left position of this View
   * @param top the top position of this View
   * @param right the right position of this View
   * @param bottom the bottom position of this View
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
    // childRight is used if the flexWrap is WRAP_REVERSE otherwise
    // childLeft is used to align the horizontal position of the children views.
    var childRight = width - paddingRight

    // Use float to reduce the round error that may happen in when justifyContent ==
    // SPACE_BETWEEN or SPACE_AROUND
    var childTop: Float

    // Used only for if the direction is from bottom to top
    var childBottom: Float
    var i = 0
    val size = flexLines.size
    while (i < size) {
      val flexLine = flexLines[i]
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
          val denominator = if (visibleCount != 1) (visibleCount - 1).toFloat() else 1f
          spaceBetweenItem = (height - flexLine.mainSize) / denominator
          childBottom = (height - paddingBottom).toFloat()
        }
        JustifyContent.SpaceEvenly -> {
          val visibleCount = flexLine.itemCountVisible
          if (visibleCount != 0) {
            spaceBetweenItem = ((height - flexLine.mainSize) / (visibleCount + 1).toFloat())
          }
          childTop = paddingTop + spaceBetweenItem
          childBottom = height - paddingBottom - spaceBetweenItem
        }
        else -> error("Invalid JustifyContent: $justifyContent")
      }
      spaceBetweenItem = max(spaceBetweenItem, 0f)
      for (j in 0 until flexLine.itemCount) {
        val index = flexLine.firstIndex + j
        val child = getReorderedChildAt(index)
        if (child == null || !child.visible) {
          continue
        }
        childTop += child.margin.top.toFloat()
        childBottom -= child.margin.bottom.toFloat()
        if (isRtl) {
          if (fromBottomToTop) {
            layoutSingleChildVertical(
              node = child,
              flexLine = flexLine,
              isRtl = true,
              left = childRight - child.measuredWidth,
              top = childBottom.roundToInt() - child.measuredHeight,
              right = childRight,
              bottom = childBottom.roundToInt(),
            )
          } else {
            layoutSingleChildVertical(
              node = child,
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
              node = child,
              flexLine = flexLine,
              isRtl = false,
              left = childLeft,
              top = childBottom.roundToInt() - child.measuredHeight,
              right = childLeft + child.measuredWidth,
              bottom = childBottom.roundToInt(),
            )
          } else {
            layoutSingleChildVertical(
              node = child,
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
      i++
    }
  }

  /**
   * The largest main size of all flex lines including decorator lengths.
   */
  private fun getLargestMainSize(): Int {
    return if (flexLines.isEmpty()) Int.MIN_VALUE else flexLines.maxOf { it.mainSize }
  }

  /**
   * The sum of the cross sizes of all flex lines including decorator lengths.
   */
  private fun getSumOfCrossSize(): Int {
    return flexLines.sumOf { it.crossSize }
  }

  /**
   * Returns a View, which is reordered by taking [Node.order] parameters into account.
   *
   * @param index the index of the view
   * @return the reordered view, which [Node.order] is taken into account.
   * If the index is negative or out of bounds of the number of contained views, returns `null`.
   */
  private fun getReorderedChildAt(index: Int): Node? {
    if (indexToReorderedIndex == null) {
      val sorted = nodes.withIndex().sortedWith(compareBy({ -it.value.order }, { it.index }))
      val indexes = IntArray(sorted.size)
      sorted.forEachIndexed { i, value ->
        indexes[value.index] = i
      }
      indexToReorderedIndex = indexes
    }
    return indexToReorderedIndex?.getOrNull(index)?.let(nodes::getOrNull)
  }
}
