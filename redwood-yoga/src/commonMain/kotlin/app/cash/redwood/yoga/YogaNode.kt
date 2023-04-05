/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YogaAlign
import app.cash.redwood.yoga.enums.YogaDirection
import app.cash.redwood.yoga.enums.YogaDisplay
import app.cash.redwood.yoga.enums.YogaEdge
import app.cash.redwood.yoga.enums.YogaFlexDirection
import app.cash.redwood.yoga.enums.YogaJustify
import app.cash.redwood.yoga.enums.YogaOverflow
import app.cash.redwood.yoga.enums.YogaPositionType
import app.cash.redwood.yoga.enums.YogaWrap
import app.cash.redwood.yoga.interfaces.YogaBaselineFunction
import app.cash.redwood.yoga.interfaces.YogaMeasureFunction
import app.cash.redwood.yoga.interfaces.YogaProps

abstract class YogaNode : YogaProps {
    /** The interface the [getData] object can optionally implement.  */
    interface Inputs {
        /** Requests the data object to disable mutations of its inputs.  */
        fun freeze(node: YogaNode?, parent: YogaNode?)
    }

    abstract fun reset()
    abstract fun getChildCount(): Int
    abstract fun getChildAt(i: Int): YogaNode
    abstract fun addChildAt(child: YogaNode?, i: Int)
    abstract override fun setIsReferenceBaseline(isReferenceBaseline: Boolean)
    abstract fun isReferenceBaseline(): Boolean
    abstract fun removeChildAt(i: Int): YogaNode

    /**
     * @return the [YogaNode] that owns this [YogaNode]. The owner is used to identify
     * the YogaTree that a [YogaNode] belongs to. This method will return the parent of the
     * [YogaNode] when the [YogaNode] only belongs to one YogaTree or null when the
     * [YogaNode] is shared between two or more YogaTrees.
     */
    abstract fun getOwner(): YogaNode?

    abstract fun indexOf(child: YogaNode?): Int
    abstract fun calculateLayout(width: Float, height: Float)
    abstract fun hasNewLayout(): Boolean
    abstract fun dirty()
    abstract fun isDirty(): Boolean
    abstract fun copyStyle(srcNode: YogaNode?)
    abstract fun markLayoutSeen()
    abstract override fun getStyleDirection(): YogaDirection?
    abstract override fun setDirection(direction: YogaDirection)
    abstract override fun getFlexDirection(): YogaFlexDirection?
    abstract override fun setFlexDirection(direction: YogaFlexDirection)
    abstract override fun getJustifyContent(): YogaJustify?
    abstract override fun setJustifyContent(justifyContent: YogaJustify)
    abstract override fun getAlignItems(): YogaAlign?
    abstract override fun setAlignItems(alignItems: YogaAlign)
    abstract override fun getAlignSelf(): YogaAlign?
    abstract override fun setAlignSelf(alignSelf: YogaAlign)
    abstract override fun getAlignContent(): YogaAlign?
    abstract override fun setAlignContent(alignContent: YogaAlign)
    abstract override fun getPositionType(): YogaPositionType?
    abstract override fun setPositionType(positionType: YogaPositionType)
    abstract fun getWrap(): YogaWrap
    abstract override fun setWrap(wrap: YogaWrap)
    abstract fun getOverflow(): YogaOverflow
    abstract fun setOverflow(overflow: YogaOverflow)
    abstract fun getDisplay(): YogaDisplay
    abstract fun setDisplay(display: YogaDisplay)
    abstract fun getFlex(): Float
    abstract override fun setFlex(flex: Float)
    abstract override fun getFlexGrow(): Float
    abstract override fun setFlexGrow(flexGrow: Float)
    abstract override fun getFlexShrink(): Float
    abstract override fun setFlexShrink(flexShrink: Float)
    abstract override fun getFlexBasis(): YogaValue?
    abstract override fun setFlexBasis(flexBasis: Float)
    abstract override fun setFlexBasisPercent(percent: Float)
    abstract override fun setFlexBasisAuto()
    abstract override fun getMargin(edge: YogaEdge): YogaValue?
    abstract override fun setMargin(edge: YogaEdge, margin: Float)
    abstract override fun setMarginPercent(edge: YogaEdge, percent: Float)
    abstract override fun setMarginAuto(edge: YogaEdge)
    abstract override fun getPadding(edge: YogaEdge): YogaValue?
    abstract override fun setPadding(edge: YogaEdge, padding: Float)
    abstract override fun setPaddingPercent(edge: YogaEdge, percent: Float)
    abstract override fun getBorder(edge: YogaEdge): Float
    abstract override fun setBorder(edge: YogaEdge, value: Float)
    abstract override fun getPosition(edge: YogaEdge): YogaValue?
    abstract override fun setPosition(edge: YogaEdge, position: Float)
    abstract override fun setPositionPercent(edge: YogaEdge, percent: Float)
    abstract override fun getWidth(): YogaValue?
    abstract override fun setWidth(width: Float)
    abstract override fun setWidthPercent(percent: Float)
    abstract override fun setWidthAuto()
    abstract override fun getHeight(): YogaValue?
    abstract override fun setHeight(height: Float)
    abstract override fun setHeightPercent(percent: Float)
    abstract override fun setHeightAuto()
    abstract override fun getMinWidth(): YogaValue?
    abstract override fun setMinWidth(minWidth: Float)
    abstract override fun setMinWidthPercent(percent: Float)
    abstract override fun getMinHeight(): YogaValue?
    abstract override fun setMinHeight(minHeight: Float)
    abstract override fun setMinHeightPercent(percent: Float)
    abstract override fun getMaxWidth(): YogaValue?
    abstract override fun setMaxWidth(maxWidth: Float)
    abstract override fun setMaxWidthPercent(percent: Float)
    abstract override fun getMaxHeight(): YogaValue?
    abstract override fun setMaxHeight(maxHeight: Float)
    abstract override fun setMaxHeightPercent(percent: Float)
    abstract override fun getAspectRatio(): Float
    abstract override fun setAspectRatio(aspectRatio: Float)
    abstract fun getLayoutX(): Float
    abstract fun getLayoutY(): Float
    abstract fun getLayoutWidth(): Float
    abstract fun getLayoutHeight(): Float
    abstract fun getLayoutMargin(edge: YogaEdge): Float
    abstract fun getLayoutPadding(edge: YogaEdge): Float
    abstract fun getLayoutBorder(edge: YogaEdge): Float
    abstract fun getLayoutDirection(): YogaDirection
    abstract override fun setMeasureFunction(measureFunction: YogaMeasureFunction?)
    abstract override fun setBaselineFunction(baselineFunction: YogaBaselineFunction?)
    abstract fun isMeasureDefined(): Boolean
    abstract fun isBaselineDefined(): Boolean
    abstract fun setData(data: Any?)
    abstract fun getData(): Any?
    abstract fun print()
}
