/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YogaValue
import app.cash.redwood.yoga.enums.YogaAlign
import app.cash.redwood.yoga.enums.YogaDirection
import app.cash.redwood.yoga.enums.YogaEdge
import app.cash.redwood.yoga.enums.YogaFlexDirection
import app.cash.redwood.yoga.enums.YogaJustify
import app.cash.redwood.yoga.enums.YogaPositionType
import app.cash.redwood.yoga.enums.YogaWrap

interface YogaProps {
    /* Width properties */
    fun setWidth(width: Float)
    fun setWidthPercent(percent: Float)
    fun setMinWidth(minWidth: Float)
    fun setMinWidthPercent(percent: Float)
    fun setMaxWidth(maxWidth: Float)
    fun setMaxWidthPercent(percent: Float)
    fun setWidthAuto()

    /* Height properties */
    fun setHeight(height: Float)
    fun setHeightPercent(percent: Float)
    fun setMinHeight(minHeight: Float)
    fun setMinHeightPercent(percent: Float)
    fun setMaxHeight(maxHeight: Float)
    fun setMaxHeightPercent(percent: Float)
    fun setHeightAuto()

    /* Margin properties */
    fun setMargin(edge: YogaEdge, margin: Float)
    fun setMarginPercent(edge: YogaEdge, percent: Float)
    fun setMarginAuto(edge: YogaEdge)

    /* Padding properties */
    fun setPadding(edge: YogaEdge, padding: Float)
    fun setPaddingPercent(edge: YogaEdge, percent: Float)

    /* Position properties */
    fun setPositionType(positionType: YogaPositionType)
    fun setPosition(edge: YogaEdge, position: Float)
    fun setPositionPercent(edge: YogaEdge, percent: Float)

    /* Alignment properties */
    fun setAlignContent(alignContent: YogaAlign)
    fun setAlignItems(alignItems: YogaAlign)
    fun setAlignSelf(alignSelf: YogaAlign)

    /* Flex properties */
    fun setFlex(flex: Float)
    fun setFlexBasisAuto()
    fun setFlexBasisPercent(percent: Float)
    fun setFlexBasis(flexBasis: Float)
    fun setFlexDirection(direction: YogaFlexDirection)
    fun setFlexGrow(flexGrow: Float)
    fun setFlexShrink(flexShrink: Float)

    /* Other properties */
    fun setJustifyContent(justifyContent: YogaJustify)
    fun setDirection(direction: YogaDirection)
    fun setBorder(edge: YogaEdge, value: Float)
    fun setWrap(wrap: YogaWrap)
    fun setAspectRatio(aspectRatio: Float)
    fun setIsReferenceBaseline(isReferenceBaseline: Boolean)
    fun setMeasureFunction(measureFunction: YogaMeasureFunction?)
    fun setBaselineFunction(baselineFunction: YogaBaselineFunction?)

    /* Getters */
    fun getWidth(): YogaValue?
    fun getMinWidth(): YogaValue?
    fun getMaxWidth(): YogaValue?
    fun getHeight(): YogaValue?
    fun getMinHeight(): YogaValue?
    fun getMaxHeight(): YogaValue?
    fun getStyleDirection(): YogaDirection?
    fun getFlexDirection(): YogaFlexDirection?
    fun getJustifyContent(): YogaJustify?
    fun getAlignItems(): YogaAlign?
    fun getAlignSelf(): YogaAlign?
    fun getAlignContent(): YogaAlign?
    fun getPositionType(): YogaPositionType?
    fun getFlexGrow(): Float
    fun getFlexShrink(): Float
    fun getFlexBasis(): YogaValue?
    fun getAspectRatio(): Float
    fun getMargin(edge: YogaEdge): YogaValue?
    fun getPadding(edge: YogaEdge): YogaValue?
    fun getPosition(edge: YogaEdge): YogaValue?
    fun getBorder(edge: YogaEdge): Float
}
