/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
@file:Suppress("unused")

package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGUnit
import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGNodeType
import app.cash.redwood.yoga.enums.YGDimension
import app.cash.redwood.yoga.enums.YGDirection
import app.cash.redwood.yoga.enums.YGMeasureMode
import app.cash.redwood.yoga.enums.YGPositionType
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.detail.CompactValue
import app.cash.redwood.yoga.interfaces.YGDirtiedFunc
import app.cash.redwood.yoga.interfaces.YGBaselineFunc
import app.cash.redwood.yoga.interfaces.BaselineWithContextFn
import app.cash.redwood.yoga.interfaces.YGPrintFunc
import app.cash.redwood.yoga.interfaces.PrintWithContextFn
import app.cash.redwood.yoga.interfaces.YGMeasureFunc
import app.cash.redwood.yoga.interfaces.MeasureWithContextFn
import app.cash.redwood.yoga.detail.Values

class YGNode {
  var context: Any? = null
  var reserved: Byte = 0
  var measure = measure_Struct()
  var baseline = baseline_Struct()
  var print = print_Struct()
  var dirtied: YGDirtiedFunc? = null
  var style: YGStyle = YGStyle()
  var layout: YGLayout? = YGLayout()
  var lineIndex = 0
  var owner: YGNode? = null
  var children = mutableListOf<YGNode>()
  var config: YGConfig? = YGConfig()
  var resolvedDimensions = MutableList(2) { Yoga.YGValueUndefined }
  private var flags = mutableMapOf<Any?, Any>()

  constructor(node: YGNode) {
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

  constructor(node: YGNode, config: YGConfig) : this(node) {
    this.config = config
    if (config.useWebDefaults) {
      useWebDefaults()
    }
  }

  constructor(config: YGConfig) {
    this.config = config
    if (config.useWebDefaults) {
      useWebDefaults()
    }
  }

  fun getResolvedDimension(index: Int): YGValue {
    return resolvedDimensions[index]
  }

  fun isDirty(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      isDirty_,
    )
  }

  fun setDirty(isDirty: Boolean) {
    if (isDirty == app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        isDirty_,
      )
    ) {
      return
    }
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      isDirty_,
      isDirty,
    )
    if (isDirty && dirtied != null) {
      dirtied!!.invoke(this)
    }
  }

  fun hasBaselineFunc(): Boolean {
    return baseline.noContext != null
  }

  fun setBaselineFunc(baseLineFunc: YGBaselineFunc?) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      false,
    )
    baseline.noContext = baseLineFunc
  }

  fun setBaselineFunc(baseLineFunc: BaselineWithContextFn?) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      true,
    )
    baseline.withContext = baseLineFunc
  }

  fun resetBaselineFunc() {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      baselineUsesContext_,
      false,
    )
    baseline.noContext = null
  }

  fun setDirtiedFunc(dirtiedFunc: YGDirtiedFunc?) {
    dirtied = dirtiedFunc
  }

  fun setPrintFunc(printFunc: YGPrintFunc?) {
    print.noContext = printFunc
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      false,
    )
  }

  fun setPrintFunc(printFunc: PrintWithContextFn?) {
    print.withContext = printFunc
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      true,
    )
  }

  fun resetPrintFunc() {
    print.noContext = null
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      printUsesContext_,
      false,
    )
  }

  fun getHasNewLayout(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      hasNewLayout_,
    )
  }

  fun setHasNewLayout(hasNewLayout: Boolean) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      hasNewLayout_,
      hasNewLayout,
    )
  }

  fun getNodeType(): YGNodeType {
    return app.cash.redwood.yoga.detail.GlobalMembers.getEnumData(
      YGNodeType::class, flags, nodeType_,
    )
  }

  fun setNodeType(nodeType: YGNodeType) {
    app.cash.redwood.yoga.detail.GlobalMembers.setEnumData(
      YGNodeType::class, flags, nodeType_, nodeType,
    )
  }

  fun setIsReferenceBaseline(isReferenceBaseline: Boolean) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      isReferenceBaseline_,
      isReferenceBaseline,
    )
  }

  fun isReferenceBaseline(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      isReferenceBaseline_,
    )
  }

  fun getChild(index: Int): YGNode {
    return children[index]
  }

  fun hasMeasureFunc(): Boolean {
    return measure.noContext != null
  }

  private fun useWebDefaults() {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      useWebDefaults_,
      true,
    )
    style.flexDirectionBitfieldRef().setValue(YGFlexDirection.YGFlexDirectionRow)
    style.alignContentBitfieldRef().setValue(YGAlign.YGAlignStretch)
  }

  fun print(printContext: Any?) {
    if (print.noContext != null) {
      if (app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
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

  fun getLeadingPosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    val leadingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValue(leadingPosition, axisSize)
  }

  fun getTrailingPosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    val trailingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValue(trailingPosition, axisSize)
  }

  fun isLeadingPositionDefined(axis: YGFlexDirection): Boolean {
    val leadingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofUndefined(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.leading[axis.ordinal], CompactValue.ofUndefined(),
    )
    return !leadingPosition.isUndefined()
  }

  fun isTrailingPosDefined(axis: YGFlexDirection): Boolean {
    val trailingPosition = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.position, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofUndefined(),
    ) else computeEdgeValueForColumn(
      style.position,
      Yoga.trailing[axis.ordinal], CompactValue.ofUndefined(),
    )
    return !trailingPosition.isUndefined()
  }

  fun getLeadingMargin(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val leadingMargin = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.margin, YGEdge.YGEdgeStart,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.margin,
      Yoga.leading[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValueMargin(leadingMargin, widthSize)
  }

  fun getTrailingMargin(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    val trailingMargin = if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.margin, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.margin,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )
    return Yoga.YGResolveValueMargin(trailingMargin, widthSize)
  }

  fun getMarginForAxis(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getLeadingMargin(axis, widthSize),
      getTrailingMargin(axis, widthSize),
    )
  }

  fun measure(
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
    layoutContext: Any?,
  ): YGSize {
    return if (app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        measureUsesContext_,
      )
    ) {
      measure.withContext!!.invoke(this, width, widthMode, height, heightMode, layoutContext)
    } else {
      measure.noContext!!.invoke(this, width, widthMode, height, heightMode)
    }
  }

  fun baseline(width: Float, height: Float, layoutContext: Any?): Float {
    return if (app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        baselineUsesContext_,
      )
    ) {
      baseline.withContext!!.invoke(this, width, height, layoutContext)
    } else {
      baseline.noContext!!.invoke(this, width, height)
    }
  }

  fun setMeasureFunc(measureFunc: measure_Struct) {
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

  fun setMeasureFunc(measureFunc: YGMeasureFunc?) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      measureUsesContext_,
      false,
    )
    measure.noContext = measureFunc
    setMeasureFunc(measure)
  }

  fun setMeasureFunc(measureFunc: MeasureWithContextFn?) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      measureUsesContext_,
      true,
    )
    measure.withContext = measureFunc
    setMeasureFunc(measure)
  }

  fun replaceChild(child: YGNode, index: Int) {
    children[index] = child
  }

  fun insertChild(child: YGNode, index: Int) {
    children.add(index, child)
  }

  fun removeChild(child: YGNode): Boolean {
    return children.remove(child)
  }

  fun removeChild(index: Int) {
    children.removeAt(index)
  }

  fun removeAllChildren() {
    children.clear()
  }

  fun setLayoutDirection(direction: YGDirection?) {
    layout!!.setDirection(direction!!)
  }

  fun setLayoutMargin(margin: Float, index: Int) {
    layout!!.margin[index] = margin
  }

  fun setLayoutBorder(border: Float, index: Int) {
    layout!!.border[index] = border
  }

  fun setLayoutPadding(padding: Float, index: Int) {
    layout!!.padding[index] = padding
  }

  fun setLayoutLastOwnerDirection(direction: YGDirection?) {
    layout!!.lastOwnerDirection = direction!!
  }

  fun setLayoutComputedFlexBasis(computedFlexBasis: YGFloatOptional?) {
    if (layout != null) {
      layout!!.computedFlexBasis = computedFlexBasis!!
    }
  }

  fun setLayoutPosition(position: Float, index: Int) {
    layout!!.position[index] = position
  }

  fun setLayoutComputedFlexBasisGeneration(computedFlexBasisGeneration: Int) {
    layout!!.computedFlexBasisGeneration = computedFlexBasisGeneration
  }

  fun setLayoutMeasuredDimension(measuredDimension: Float, index: Int) {
    layout!!.measuredDimensions[index] = measuredDimension
  }

  fun setLayoutHadOverflow(hadOverflow: Boolean) {
    layout!!.setHadOverflow(hadOverflow)
  }

  fun setLayoutDimension(dimension: Float, index: Int) {
    layout!!.dimensions[index] = dimension
  }

  fun relativePosition(axis: YGFlexDirection, axisSize: Float): YGFloatOptional {
    if (isLeadingPositionDefined(axis)) {
      return getLeadingPosition(axis, axisSize)
    }
    var trailingPosition = getTrailingPosition(axis, axisSize)
    if (!trailingPosition.isUndefined()) {
      trailingPosition = YGFloatOptional(-1 * trailingPosition.unwrap())
    }
    return trailingPosition
  }

  fun setPosition(direction: YGDirection, mainSize: Float, crossSize: Float, ownerWidth: Float) {
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

  fun marginLeadingValue(axis: YGFlexDirection): YGValue {
    return if (Yoga.YGFlexDirectionIsRow(axis) && !style.margin
        .getCompactValue(YGEdge.YGEdgeStart).isUndefined()
    ) {
      style.margin[YGEdge.YGEdgeStart.ordinal]
    } else {
      style.margin[Yoga.leading[axis.ordinal].ordinal]
    }
  }

  fun marginTrailingValue(axis: YGFlexDirection): YGValue {
    return if (Yoga.YGFlexDirectionIsRow(axis) && !style.margin
        .getCompactValue(YGEdge.YGEdgeEnd).isUndefined()
    ) {
      style.margin[YGEdge.YGEdgeEnd.ordinal]
    } else {
      style.margin[Yoga.trailing[axis.ordinal].ordinal]
    }
  }

  fun resolveFlexBasisPtr(): YGValue {
    val flexBasis = style.flexBasis.convertToYgValue()
    if (flexBasis.unit != YGUnit.YGUnitAuto && flexBasis.unit != YGUnit.YGUnitUndefined) {
      return flexBasis
    }
    return if (!style.flex.isUndefined() && style.flex.unwrap() > 0.0f) {
      if (app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
          flags,
          useWebDefaults_,
        )
      ) Yoga.YGValueAuto else Yoga.YGValueZero
    } else {
      Yoga.YGValueAuto
    }
  }

  fun resolveDimension() {
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

  fun resolveDirection(ownerDirection: YGDirection): YGDirection {
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

  fun clearChildren() {
    children.clear()
  }

  fun cloneChildrenIfNeeded(cloneContext: Any?) {
    iterChildrenAfterCloningIfNeeded<Any>(
      { _: YGNode?, _: Any? -> },
      cloneContext,
    )
  }

  fun <T> iterChildrenAfterCloningIfNeeded(
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

  fun markDirtyAndPropogate() {
    if (!app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(flags, isDirty_)) {
      setDirty(true)
      setLayoutComputedFlexBasis(YGFloatOptional())
      if (owner != null) {
        owner!!.markDirtyAndPropogate()
      }
    }
  }

  fun markDirtyAndPropogateDownwards() {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      isDirty_,
      true,
    )
    children.forEach { obj: YGNode -> obj.markDirtyAndPropogateDownwards() }
  }

  fun resolveFlexGrow(): Float {
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

  fun resolveFlexShrink(): Float {
    if (owner == null) {
      return 0.0f
    }
    if (!style.flexShrink.isUndefined()) {
      return style.flexShrink.unwrap()
    }
    if (!app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      ) && !style.flex.isUndefined() && style.flex.unwrap() < 0.0f
    ) {
      return -style.flex.unwrap()
    }
    return if (app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      )
    ) Yoga.WebDefaultFlexShrink else Yoga.DefaultFlexShrink
  }

  fun isNodeFlexible(): Boolean {
    return style.positionType() != YGPositionType.YGPositionTypeAbsolute && (resolveFlexGrow() != 0f || resolveFlexShrink() != 0f)
  }

  fun getLeadingBorder(axis: YGFlexDirection): Float {
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

  fun getTrailingBorder(axis: YGFlexDirection): Float {
    val trailingBorder = (if (Yoga.YGFlexDirectionIsRow(axis)) computeEdgeValueForRow(
      style.border, YGEdge.YGEdgeEnd,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    ) else computeEdgeValueForColumn(
      style.border,
      Yoga.trailing[axis.ordinal], CompactValue.ofZero(),
    )).convertToYgValue()
    return maxOf(trailingBorder.value, 0.0f)
  }

  fun getLeadingPadding(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
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

  fun getTrailingPadding(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
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

  fun getLeadingPaddingAndBorder(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getLeadingPadding(axis, widthSize),
      YGFloatOptional(getLeadingBorder(axis)),
    )
  }

  fun getTrailingPaddingAndBorder(axis: YGFlexDirection, widthSize: Float): YGFloatOptional {
    return Yoga.plus(
      getTrailingPadding(axis, widthSize),
      YGFloatOptional(getTrailingBorder(axis)),
    )
  }

  fun didUseLegacyFlag(): Boolean {
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

  fun setLayoutDoesLegacyFlagAffectsLayout(doesLegacyFlagAffectsLayout: Boolean) {
    layout!!.setDoesLegacyStretchFlagAffectsLayout(doesLegacyFlagAffectsLayout)
  }

  fun setLayoutDidUseLegacyFlag(didUseLegacyFlag: Boolean) {
    layout!!.setDidUseLegacyFlag(didUseLegacyFlag)
  }

  fun isLayoutTreeEqualToNode(node: YGNode): Boolean {
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

  fun reset() {
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
      app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
        flags,
        useWebDefaults_,
      )
    config = YGConfig()
    if (webDefaults) {
      useWebDefaults()
    }
  }

  class measure_Struct {
    var noContext: YGMeasureFunc? = null
    var withContext: MeasureWithContextFn? = null
  }

  class baseline_Struct {
    var noContext: YGBaselineFunc? = null
    var withContext: BaselineWithContextFn? = null
  }

  class print_Struct {
    var noContext: YGPrintFunc? = null
    var withContext: PrintWithContextFn? = null
  }

  companion object {
    private const val hasNewLayout_ = 0
    private const val isReferenceBaseline_ = 1
    private const val isDirty_ = 2
    private const val nodeType_ = 3
    private const val measureUsesContext_ = 4
    private const val baselineUsesContext_ = 5
    private const val printUsesContext_ = 6
    private const val useWebDefaults_ = 7
    fun computeEdgeValueForRow(
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

    fun computeEdgeValueForColumn(
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
