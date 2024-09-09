/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
@file:Suppress("unused")

package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGEdge
import app.cash.redwood.yoga.internal.enums.YGUnit
import app.cash.redwood.yoga.internal.enums.YGAlign
import app.cash.redwood.yoga.internal.enums.YGNodeType
import app.cash.redwood.yoga.internal.enums.YGDimension
import app.cash.redwood.yoga.internal.enums.YGDirection
import app.cash.redwood.yoga.internal.enums.YGMeasureMode
import app.cash.redwood.yoga.internal.enums.YGPositionType
import app.cash.redwood.yoga.internal.enums.YGFlexDirection
import app.cash.redwood.yoga.internal.detail.CompactValue
import app.cash.redwood.yoga.internal.interfaces.YGDirtiedFunc
import app.cash.redwood.yoga.internal.interfaces.YGBaselineFunc
import app.cash.redwood.yoga.internal.interfaces.BaselineWithContextFn
import app.cash.redwood.yoga.internal.interfaces.YGPrintFunc
import app.cash.redwood.yoga.internal.interfaces.PrintWithContextFn
import app.cash.redwood.yoga.internal.interfaces.YGMeasureFunc
import app.cash.redwood.yoga.internal.interfaces.MeasureWithContextFn
import app.cash.redwood.yoga.internal.detail.Values
import app.cash.redwood.yoga.internal.detail.GlobalMembers

public class YGNode {
  internal var context: Any? = null
  internal var reserved: Byte = 0
  internal var measure = measure_Struct()
  internal var baseline = baseline_Struct()
  internal var print = print_Struct()
  internal var dirtied: YGDirtiedFunc? = null
  internal var style: YGStyle = YGStyle()
  internal var layout: YGLayout? = YGLayout()
  internal var lineIndex = 0
  internal var owner: YGNode? = null
  internal var children = mutableListOf<YGNode>()
  internal var config: YGConfig? = YGConfig()
  internal var resolvedDimensions = MutableList(2) { Yoga.YGValueUndefined }
  private var flags = mutableMapOf<Any?, Any>()

  internal constructor(node: YGNode) {
    context = node.context
    flags = node.flags
    measure = node.measure
    baseline = node.baseline
    print = node.print
    dirtied = node.dirtied
    style = node.style
    layout = node.layout
    lineIndex = node.lineIndex
    owner = node.owner
    children = node.children.toMutableList()
    config = node.config
    resolvedDimensions = node.resolvedDimensions
    for (c in children) {
      c.owner = this
    }
  }

  internal constructor(node: YGNode, config: YGConfig) : this(node) {
    this.config = config
    if (config.useWebDefaults) {
      useWebDefaults()
    }
  }

  internal constructor(config: YGConfig) {
    this.config = config
    if (config.useWebDefaults) {
      useWebDefaults()
    }
  }

  internal fun getResolvedDimension(index: Int): YGValue {
    return resolvedDimensions[index]
  }

  public fun isDirty(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      isDirty_,
    )
  }

  public fun setDirty(isDirty: Boolean) {
    if (isDirty == GlobalMembers.getBooleanData(
        flags,
        isDirty_,
      )
    ) {
      return
    }
    GlobalMembers.setBooleanData(
      flags,
      isDirty_,
      isDirty,
    )
    if (isDirty && dirtied != null) {
      dirtied!!.invoke(this)
    }
  }

  internal fun hasBaselineFunc(): Boolean {
    return baseline.noContext != null
  }

  internal fun setBaselineFunc(baseLineFunc: YGBaselineFunc?) {
    GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      false,
    )
    baseline.noContext = baseLineFunc
  }

  internal fun setBaselineFunc(baseLineFunc: BaselineWithContextFn?) {
    GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      true,
    )
    baseline.withContext = baseLineFunc
  }

  internal fun resetBaselineFunc() {
    GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      false,
    )
    baseline.noContext = null
  }

  internal fun setDirtiedFunc(dirtiedFunc: YGDirtiedFunc?) {
    dirtied = dirtiedFunc
  }

  internal fun setPrintFunc(printFunc: YGPrintFunc?) {
    print.noContext = printFunc
    GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      false,
    )
  }

  internal fun setPrintFunc(printFunc: PrintWithContextFn?) {
    print.withContext = printFunc
    GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      true,
    )
  }

  internal fun resetPrintFunc() {
    print.noContext = null
    GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      false,
    )
  }

  internal fun getHasNewLayout(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      hasNewLayout_,
    )
  }

  internal fun setHasNewLayout(hasNewLayout: Boolean) {
    GlobalMembers.setBooleanData(
      flags,
      hasNewLayout_,
      hasNewLayout,
    )
  }

  internal fun getNodeType(): YGNodeType {
    return GlobalMembers.getEnumData(
      YGNodeType::class, flags, nodeType_,
    )
  }

  internal fun setNodeType(nodeType: YGNodeType) {
    GlobalMembers.setEnumData(
      YGNodeType::class, flags, nodeType_, nodeType,
    )
  }

  internal fun setIsReferenceBaseline(isReferenceBaseline: Boolean) {
    GlobalMembers.setBooleanData(
      flags,
      isReferenceBaseline_,
      isReferenceBaseline,
    )
  }

  internal fun isReferenceBaseline(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      isReferenceBaseline_,
    )
  }

  internal fun getChild(index: Int): YGNode {
    return children[index]
  }

  internal fun hasMeasureFunc(): Boolean {
    return measure.noContext != null
  }

  private fun useWebDefaults() {
    GlobalMembers.setBooleanData(
      flags,
      useWebDefaults_,
      true,
    )
    style.flexDirectionBitfieldRef().setValue(YGFlexDirection.YGFlexDirectionRow)
    style.alignContentBitfieldRef().setValue(YGAlign.YGAlignStretch)
  }

  internal fun print(printContext: Any?) {
    if (print.noContext != null) {
      if (GlobalMembers.getBooleanData(
          flags,
          printUsesContext_,
        )
      ) {
        print.withContext!!.invoke(this, printContext)
      } else {
        print.noContext!!.invoke(this)
      }
    }
  }

  internal fun getLeadingPosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    val leadingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValue(leadingPosition, axisSize)
  }

  internal fun getTrailingPosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    val trailingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValue(trailingPosition, axisSize)
  }

  internal fun isLeadingPositionDefined(axis: YGFlexDirection): Boolean {
    val leadingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofUndefined(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.leading[axis.ordinal], CompactValue.ofUndefined(),
    )
    return !leadingPosition.isUndefined()
  }

  internal fun isTrailingPosDefined(axis: YGFlexDirection): Boolean {
    val trailingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofUndefined(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.trailing[axis.ordinal], CompactValue.ofUndefined(),
    )
    return !trailingPosition.isUndefined()
  }

  internal fun getLeadingMargin(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val leadingMargin = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.margin, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.margin,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValueMargin(leadingMargin, widthSize)
  }

  internal fun getTrailingMargin(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val trailingMargin = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.margin, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.margin,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValueMargin(trailingMargin, widthSize)
  }

  internal fun getMarginForAxis(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getLeadingMargin(axis, widthSize),
      getTrailingMargin(axis, widthSize),
    )
  }

  internal fun measure(
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
    layoutContext: Any?,
  ): YGSize {
    return if (GlobalMembers.getBooleanData(
        flags,
        measureUsesContext_,
      )
    ) {
      measure.withContext!!.invoke(this, width, widthMode, height, heightMode, layoutContext)
    } else {
      measure.noContext!!.invoke(this, width, widthMode, height, heightMode)
    }
  }

  internal fun baseline(width: Float, height: Float, layoutContext: Any?): Float {
    return if (GlobalMembers.getBooleanData(
        flags,
        baselineUsesContext_,
      )
    ) {
      baseline.withContext!!.invoke(this, width, height, layoutContext)
    } else {
      baseline.noContext!!.invoke(this, width, height)
    }
  }

  internal fun setMeasureFunc(measureFunc: measure_Struct) {
    if (measureFunc.noContext == null) {
      setNodeType(YGNodeType.YGNodeTypeDefault)
    } else {
      Yoga.YGAssertWithNode(
        this, children.size == 0,
        "Cannot set measure function: Nodes with measure functions cannot have " + "children.",
      )
      setNodeType(YGNodeType.YGNodeTypeText)
    }
    measure = measureFunc
  }

  internal fun setMeasureFunc(measureFunc: YGMeasureFunc?) {
    GlobalMembers.setBooleanData(
      flags,
      measureUsesContext_,
      false,
    )
    measure.noContext = measureFunc
    setMeasureFunc(measure)
  }

  internal fun setMeasureFunc(measureFunc: MeasureWithContextFn?) {
    GlobalMembers.setBooleanData(
      flags,
      measureUsesContext_,
      true,
    )
    measure.withContext = measureFunc
    setMeasureFunc(measure)
  }

  internal fun replaceChild(child: YGNode, index: Int) {
    children[index] = child
  }

  internal fun insertChild(child: YGNode, index: Int) {
    children.add(index, child)
  }

  internal fun removeChild(child: YGNode): Boolean {
    return children.remove(child)
  }

  internal fun removeChild(index: Int) {
    children.removeAt(index)
  }

  internal fun removeAllChildren() {
    children.clear()
  }

  internal fun setLayoutDirection(direction: YGDirection?) {
    layout!!.setDirection(direction!!)
  }

  internal fun setLayoutMargin(margin: Float, index: Int) {
    layout!!.margin[index] = margin
  }

  internal fun setLayoutBorder(border: Float, index: Int) {
    layout!!.border[index] = border
  }

  internal fun setLayoutPadding(padding: Float, index: Int) {
    layout!!.padding[index] = padding
  }

  internal fun setLayoutLastOwnerDirection(direction: YGDirection?) {
    layout!!.lastOwnerDirection = direction!!
  }

  internal fun setLayoutComputedFlexBasis(computedFlexBasis: YGFloatOptional?) {
    if (layout != null) {
      layout!!.computedFlexBasis = computedFlexBasis!!
    }
  }

  internal fun setLayoutPosition(position: Float, index: Int) {
    layout!!.position[index] = position
  }

  internal fun setLayoutComputedFlexBasisGeneration(computedFlexBasisGeneration: Int) {
    layout!!.computedFlexBasisGeneration = computedFlexBasisGeneration
  }

  internal fun setLayoutMeasuredDimension(measuredDimension: Float, index: Int) {
    layout!!.measuredDimensions[index] = measuredDimension
  }

  internal fun setLayoutHadOverflow(hadOverflow: Boolean) {
    layout!!.setHadOverflow(hadOverflow)
  }

  internal fun setLayoutDimension(dimension: Float, index: Int) {
    layout!!.dimensions[index] = dimension
  }

  internal fun relativePosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    if (isLeadingPositionDefined(axis)) {
      return getLeadingPosition(axis, axisSize)
    }
    var trailingPosition = getTrailingPosition(axis, axisSize)
    if (!trailingPosition.isUndefined()) {
      trailingPosition = YGFloatOptional(-1 * trailingPosition.unwrap())
    }
    return trailingPosition
  }

  internal fun setPosition(direction: YGDirection, mainSize: Float, crossSize: Float, ownerWidth: Float) {
    val directionRespectingRoot = if (owner != null) direction else YGDirection.YGDirectionLTR
    val mainAxis = Yoga.YGResolveFlexDirection(
      style.flexDirection(), directionRespectingRoot,
    )
    val crossAxis = Yoga.YGFlexDirectionCross(mainAxis, directionRespectingRoot)
    val relativePositionMain = relativePosition(mainAxis, mainSize)
    val relativePositionCross = relativePosition(crossAxis, crossSize)
    setLayoutPosition(
      Yoga.plus(
        getLeadingMargin(
          mainAxis, ownerWidth,
        ),
        relativePositionMain,
      ).unwrap(),
      Yoga.leading[mainAxis.ordinal].ordinal,
    )
    setLayoutPosition(
      Yoga.plus(
        getTrailingMargin(
          mainAxis, ownerWidth,
        ),
        relativePositionMain,
      ).unwrap(),
      Yoga.trailing[mainAxis.ordinal].ordinal,
    )
    setLayoutPosition(
      Yoga.plus(getLeadingMargin(crossAxis, ownerWidth), relativePositionCross)
        .unwrap(),
      Yoga.leading[crossAxis.ordinal].ordinal,
    )
    setLayoutPosition(
      Yoga.plus(getTrailingMargin(crossAxis, ownerWidth), relativePositionCross)
        .unwrap(),
      Yoga.trailing[crossAxis.ordinal].ordinal,
    )
  }

  internal fun marginLeadingValue(axis: YGFlexDirection): YGValue {
    return if (Yoga.YGFlexDirectionIsRow(axis) && !style.margin
        .getCompactValue(YGEdge.YGEdgeStart).isUndefined()
    ) {
      style.margin[YGEdge.YGEdgeStart.ordinal]
    } else {
      style.margin[Yoga.leading[axis.ordinal].ordinal]
    }
  }

  internal fun marginTrailingValue(axis: YGFlexDirection): YGValue {
    return if (Yoga.YGFlexDirectionIsRow(axis) && !style.margin
        .getCompactValue(YGEdge.YGEdgeEnd).isUndefined()
    ) {
      style.margin[YGEdge.YGEdgeEnd.ordinal]
    } else {
      style.margin[Yoga.trailing[axis.ordinal].ordinal]
    }
  }

  internal fun resolveFlexBasisPtr(): YGValue {
    val flexBasis = style.flexBasis.convertToYgValue()
    if (flexBasis.unit != YGUnit.YGUnitAuto && flexBasis.unit != YGUnit.YGUnitUndefined) {
      return flexBasis
    }
    return if (!style.flex.isUndefined() && style.flex.unwrap() > 0.0f) {
      if (GlobalMembers.getBooleanData(
          flags,
          useWebDefaults_,
        )
      ) Yoga.YGValueAuto else Yoga.YGValueZero
    } else {
      Yoga.YGValueAuto
    }
  }

  internal fun resolveDimension() {
    val style = style
    val dimensions = arrayOf(YGDimension.YGDimensionWidth, YGDimension.YGDimensionHeight)
    for (dim in dimensions) {
      if (!style.maxDimensions.getCompactValue(dim.ordinal).isUndefined() && Yoga.YGValueEqual(
          style.maxDimensions.getCompactValue(dim.ordinal),
          style.minDimensions.getCompactValue(dim.ordinal),
        )
      ) {
        resolvedDimensions[dim.ordinal] = style.maxDimensions[dim.ordinal]
      } else {
        resolvedDimensions[dim.ordinal] = style.dimensions[dim.ordinal]
      }
    }
  }

  internal fun resolveDirection(ownerDirection: YGDirection): YGDirection {
    return if (style.direction() == YGDirection.YGDirectionInherit) {
      if (ownerDirection.ordinal > YGDirection.YGDirectionInherit.ordinal) {
        ownerDirection
      } else {
        YGDirection.YGDirectionLTR
      }
    } else {
      style.direction()
    }
  }

  internal fun clearChildren() {
    children.clear()
  }

  internal fun cloneChildrenIfNeeded(cloneContext: Any?) {
    iterChildrenAfterCloningIfNeeded<Any>(
      { _: YGNode?, _: Any? -> },
      cloneContext,
    )
  }

  internal fun <T> iterChildrenAfterCloningIfNeeded(
    callback: (YGNode, Any?) -> Unit,
    cloneContext: Any?,
  ) {
    var i = 0
    for (child in children) {
      var c = child
      if (child.owner !== this) {
        c = config!!.cloneNode(child, this, i, cloneContext)
        child.owner = this
      }
      i += 1
      callback(c, cloneContext)
    }
  }

  internal fun markDirtyAndPropogate() {
    if (!GlobalMembers.getBooleanData(flags, isDirty_)) {
      setDirty(true)
      setLayoutComputedFlexBasis(YGFloatOptional())
      if (owner != null) {
        owner!!.markDirtyAndPropogate()
      }
    }
  }

  internal fun markDirtyAndPropogateDownwards() {
    GlobalMembers.setBooleanData(
      flags,
      isDirty_,
      true,
    )
    children.forEach { obj: YGNode -> obj.markDirtyAndPropogateDownwards() }
  }

  internal fun resolveFlexGrow(): Float {
    if (owner == null) {
      return 0.0f
    }
    if (!style.flexGrow.isUndefined()) {
      return style.flexGrow.unwrap()
    }
    return if (!style.flex.isUndefined() && style.flex.unwrap() > 0.0f) {
      style.flex.unwrap()
    } else {
      Yoga.DefaultFlexGrow
    }
  }

  internal fun resolveFlexShrink(): Float {
    if (owner == null) {
      return 0.0f
    }
    if (!style.flexShrink.isUndefined()) {
      return style.flexShrink.unwrap()
    }
    if (!GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      ) && !style.flex.isUndefined() && style.flex.unwrap() < 0.0f
    ) {
      return -style.flex.unwrap()
    }
    return if (GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      )
    ) Yoga.WebDefaultFlexShrink else Yoga.DefaultFlexShrink
  }

  internal fun isNodeFlexible(): Boolean {
    return style.positionType() != YGPositionType.YGPositionTypeAbsolute && (resolveFlexGrow() != 0f || resolveFlexShrink() != 0f)
  }

  internal fun getLeadingBorder(axis: YGFlexDirection): Float {
    val leadingBorder = (if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.border,
      YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.border,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )).convertToYgValue()
    return maxOf(leadingBorder.value, 0.0f)
  }

  internal fun getTrailingBorder(axis: YGFlexDirection): Float {
    val trailingBorder = (if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.border, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.border,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )).convertToYgValue()
    return maxOf(trailingBorder.value, 0.0f)
  }

  internal fun getLeadingPadding(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val leadingPadding = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.padding, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.padding,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGFloatOptionalMax(
      Yoga.YGResolveValue(
        leadingPadding,
        widthSize,
      ),
      YGFloatOptional(0.0f),
    )
  }

  internal fun getTrailingPadding(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val trailingPadding = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.padding, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.padding,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGFloatOptionalMax(
      Yoga.YGResolveValue(
        trailingPadding,
        widthSize,
      ),
      YGFloatOptional(0.0f),
    )
  }

  internal fun getLeadingPaddingAndBorder(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getLeadingPadding(axis, widthSize),
      YGFloatOptional(getLeadingBorder(axis)),
    )
  }

  internal fun getTrailingPaddingAndBorder(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getTrailingPadding(axis, widthSize),
      YGFloatOptional(getTrailingBorder(axis)),
    )
  }

  internal fun didUseLegacyFlag(): Boolean {
    var didUseLegacyFlag = layout!!.didUseLegacyFlag()
    if (didUseLegacyFlag) {
      return true
    }
    for (child in children) {
      if (child.layout!!.didUseLegacyFlag()) {
        didUseLegacyFlag = true
        break
      }
    }
    return didUseLegacyFlag
  }

  internal fun setLayoutDoesLegacyFlagAffectsLayout(doesLegacyFlagAffectsLayout: Boolean) {
    layout!!.setDoesLegacyStretchFlagAffectsLayout(doesLegacyFlagAffectsLayout)
  }

  internal fun setLayoutDidUseLegacyFlag(didUseLegacyFlag: Boolean) {
    layout!!.setDidUseLegacyFlag(didUseLegacyFlag)
  }

  internal fun isLayoutTreeEqualToNode(node: YGNode): Boolean {
    if (children.size != node.children.size) {
      return false
    }
    if (layout !== node.layout) {
      return false
    }
    if (children.size == 0) {
      return true
    }
    var isLayoutTreeEqual = true
    var i = 0
    val children_size = children.size
    while (i < children_size) {
      val child = children[i]
      val otherNodeChildren = node.children[i]
      isLayoutTreeEqual = child.isLayoutTreeEqualToNode(otherNodeChildren)
      if (!isLayoutTreeEqual) break
      i++
    }
    return isLayoutTreeEqual
  }

  internal fun reset() {
    Yoga.YGAssertWithNode(
      this,
      children.size == 0,
      "Cannot reset a node which still has children attached",
    )
    Yoga.YGAssertWithNode(
      this,
      owner == null,
      "Cannot reset a node still attached to a owner",
    )
    clearChildren()
    val webDefaults: Boolean =
      GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      )
    config = YGConfig()
    if (webDefaults) {
      useWebDefaults()
    }
  }

  internal class measure_Struct {
    var noContext: YGMeasureFunc? = null
    var withContext: MeasureWithContextFn? = null
  }

  internal class baseline_Struct {
    var noContext: YGBaselineFunc? = null
    var withContext: BaselineWithContextFn? = null
  }

  internal class print_Struct {
    var noContext: YGPrintFunc? = null
    var withContext: PrintWithContextFn? = null
  }

  internal companion object {
    private const val hasNewLayout_ = 0
    private const val isReferenceBaseline_ = 1
    private const val isDirty_ = 2
    private const val nodeType_ = 3
    private const val measureUsesContext_ = 4
    private const val baselineUsesContext_ = 5
    private const val printUsesContext_ = 6
    private const val useWebDefaults_ = 7
    internal fun computeEdgeValueForRow(
      edges: Values<YGEdge>,
      rowEdge: YGEdge,
      edge: YGEdge,
      defaultValue: CompactValue,
    ): CompactValue {
      return if (!edges.getCompactValue(rowEdge).isUndefined()) {
        edges.getCompactValue(rowEdge)
      } else if (!edges.getCompactValue(edge).isUndefined()) {
        edges.getCompactValue(edge)
      } else if (!edges.getCompactValue(YGEdge.YGEdgeHorizontal).isUndefined()) {
        edges.getCompactValue(YGEdge.YGEdgeHorizontal)
      } else if (!edges.getCompactValue(YGEdge.YGEdgeAll).isUndefined()) {
        edges.getCompactValue(YGEdge.YGEdgeAll)
      } else {
        defaultValue
      }
    }

    internal fun computeEdgeValueForColumn(
      edges: Values<YGEdge>,
      edge: YGEdge,
      defaultValue: CompactValue,
    ): CompactValue {
      return if (!edges.getCompactValue(edge).isUndefined()) {
        edges.getCompactValue(edge)
      } else if (!edges.getCompactValue(YGEdge.YGEdgeVertical).isUndefined()) {
        edges.getCompactValue(YGEdge.YGEdgeVertical)
      } else if (!edges.getCompactValue(YGEdge.YGEdgeAll).isUndefined()) {
        edges.getCompactValue(YGEdge.YGEdgeAll)
      } else {
        defaultValue
      }
    }
  }
}
