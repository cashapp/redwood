package io.github.orioncraftmc.meditate.internal;

import io.github.orioncraftmc.meditate.internal.detail.CompactValue;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.getBooleanData;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.getEnumData;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.setBooleanData;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.setEnumData;
import io.github.orioncraftmc.meditate.internal.detail.Values;
import io.github.orioncraftmc.meditate.internal.enums.*;
import io.github.orioncraftmc.meditate.internal.interfaces.*;
import java.util.*;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YGNode {

    private static final int hasNewLayout_ = 0;
    private static final int isReferenceBaseline_ = 1;
    private static final int isDirty_ = 2;
    private static final int nodeType_ = 3;
    private static final int measureUsesContext_ = 4;
    private static final int baselineUsesContext_ = 5;
    private static final int printUsesContext_ = 6;
    private static final int useWebDefaults_ = 7;
    private @Nullable Object context_ = null;
    private Map<Object, Object> flags = new HashMap<>();
    private byte reserved_ = 0;
    private measure_Struct measure_ = new measure_Struct();
    private baseline_Struct baseline_ = new baseline_Struct();
    private print_Struct print_ = new print_Struct();
    private @Nullable YGDirtiedFunc dirtied_ = null;
    private YGStyle style_ = new YGStyle();
    private YGLayout layout_ = new YGLayout();
    private int lineIndex_ = 0;
    private @Nullable YGNode owner_ = null;
    private ArrayList<YGNode> children_ = new ArrayList<>();
    private @Nullable YGConfig config_ = new YGConfig(null);
    private ArrayList<YGValue> resolvedDimensions_ = new ArrayList<>(
            Arrays.asList(GlobalMembers.YGValueUndefined, GlobalMembers.YGValueUndefined));

    public YGNode(@NotNull YGNode node) {
        context_ = node.context_;
        flags = node.flags;
        measure_ = node.measure_;
        baseline_ = node.baseline_;
        print_ = node.print_;
        dirtied_ = node.dirtied_;
        style_ = node.style_;
        layout_ = node.layout_;
        lineIndex_ = node.lineIndex_;
        owner_ = node.owner_;
        children_ = node.children_; //TODO: Make full copy
        config_ = node.config_;
        resolvedDimensions_ = node.resolvedDimensions_;
        for (@NotNull var c : children_) {
            c.setOwner(this);
        }
    }

    public YGNode(final @NotNull YGNode node, @NotNull YGConfig config) {
        this(node);
        config_ = config;
        if (config.useWebDefaults) {
            useWebDefaults();
        }
    }

    public YGNode(@NotNull YGConfig config) {
        config_ = config;
        if (config.useWebDefaults) {
            useWebDefaults();
        }
    }

    public static CompactValue computeEdgeValueForRow(final @NotNull Values<YGEdge> edges, @NotNull YGEdge rowEdge, @NotNull YGEdge edge, CompactValue defaultValue) //Method definition originates from: YGNode.cpp
    {
        if (!edges.getCompactValue(rowEdge).isUndefined()) {
            return edges.getCompactValue(rowEdge);
        } else if (!edges.getCompactValue(edge).isUndefined()) {
            return edges.getCompactValue(edge);
        } else if (!edges.getCompactValue(YGEdge.YGEdgeHorizontal).isUndefined()) {
            return edges.getCompactValue(YGEdge.YGEdgeHorizontal);
        } else if (!edges.getCompactValue(YGEdge.YGEdgeAll).isUndefined()) {
            return edges.getCompactValue(YGEdge.YGEdgeAll);
        } else {
            return defaultValue;
        }
    }

    public static CompactValue computeEdgeValueForColumn(final @NotNull Values<YGEdge> edges, @NotNull YGEdge edge, CompactValue defaultValue) //Method definition originates from: YGNode.cpp
    {
        if (!edges.getCompactValue(edge).isUndefined()) {
            return edges.getCompactValue(edge);
        } else if (!edges.getCompactValue(YGEdge.YGEdgeVertical).isUndefined()) {
            return edges.getCompactValue(YGEdge.YGEdgeVertical);
        } else if (!edges.getCompactValue(YGEdge.YGEdgeAll).isUndefined()) {
            return edges.getCompactValue(YGEdge.YGEdgeAll);
        } else {
            return defaultValue;
        }
    }

    public final YGValue getResolvedDimension(int index) {
        return resolvedDimensions_.get(index);
    }

    public final boolean isDirty() {
        return getBooleanData(flags, isDirty_);
    }

    public void setDirty(boolean isDirty) {
        if (isDirty == getBooleanData(flags, isDirty_)) {
            return;
        }
        setBooleanData(flags, isDirty_, isDirty);
        if (isDirty && dirtied_ != null) {
            dirtied_.invoke(this);
        }
    }

    public final boolean hasBaselineFunc() {
        if (baseline_ != null) {
            return baseline_.noContext != null;
        }
        return false;
    }

    public final void setBaselineFunc(YGBaselineFunc baseLineFunc) {
        setBooleanData(flags, baselineUsesContext_, false);
        if (baseline_ != null) {
            baseline_.noContext = baseLineFunc;
        }
    }

    public final void setBaselineFunc(BaselineWithContextFn baseLineFunc) {
        setBooleanData(flags, baselineUsesContext_, true);
        if (baseline_ != null) {
            baseline_.withContext = baseLineFunc;
        }
    }

    public final void resetBaselineFunc() {
        setBooleanData(flags, baselineUsesContext_, false);
        if (baseline_ != null) {
            baseline_.noContext = null;
        }
    }

    public final void setDirtiedFunc(YGDirtiedFunc dirtiedFunc) {
        dirtied_ = dirtiedFunc;
    }

    public final void setPrintFunc(YGPrintFunc printFunc) {
        if (print_ != null) {
            print_.noContext = printFunc;
        }
        setBooleanData(flags, printUsesContext_, false);
    }

    public final void setPrintFunc(PrintWithContextFn printFunc) {
        if (print_ != null) {
            print_.withContext = printFunc;
        }
        setBooleanData(flags, printUsesContext_, true);
    }

    public final void resetPrintFunc() {
        if (print_ != null) {
            print_.noContext = null;
        }
        setBooleanData(flags, printUsesContext_, false);
    }

    public final boolean getHasNewLayout() {
        return getBooleanData(flags, hasNewLayout_);
    }

    public final void setHasNewLayout(boolean hasNewLayout) {
        setBooleanData(flags, hasNewLayout_, hasNewLayout);
    }

    public final YGNodeType getNodeType() {
        return getEnumData(YGNodeType.class, flags, nodeType_);
    }

    public final void setNodeType(@NotNull YGNodeType nodeType) {
        setEnumData(YGNodeType.class, flags, nodeType_, nodeType);
    }

    public final void setIsReferenceBaseline(boolean isReferenceBaseline) {
        setBooleanData(flags, isReferenceBaseline_, isReferenceBaseline);
    }

    public final boolean isReferenceBaseline() {
        return getBooleanData(flags, isReferenceBaseline_);
    }

    public final YGNode getChild(Integer index) {
        return (children_.get(index));
    }

    public final boolean hasMeasureFunc() {
        if (measure_ != null) {
            return measure_.noContext != null;
        }
        return false;
    }

    private void useWebDefaults() {
        setBooleanData(flags, useWebDefaults_, true);
        style_.flexDirectionBitfieldRef().setValue(YGFlexDirection.YGFlexDirectionRow);
        style_.alignContentBitfieldRef().setValue(YGAlign.YGAlignStretch);
    }

    public void print(Object printContext) {
        if (print_.noContext != null) {
            if (getBooleanData(flags, printUsesContext_)) {
                print_.withContext.invoke(this, printContext);
            } else {
                print_.noContext.invoke(this);
            }
        }
    }

    public @NotNull YGFloatOptional getLeadingPosition(final @NotNull YGFlexDirection axis, final float axisSize) {
        var leadingPosition = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.position(), YGEdge.YGEdgeStart,
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.position(),
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGResolveValue(leadingPosition, axisSize);
    }

    public @NotNull YGFloatOptional getTrailingPosition(final @NotNull YGFlexDirection axis, final float axisSize) {
        var trailingPosition = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.position(), YGEdge.YGEdgeEnd,
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.position(),
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGResolveValue(trailingPosition, axisSize);
    }

    public boolean isLeadingPositionDefined(final @NotNull YGFlexDirection axis) {
        var leadingPosition = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.position(), YGEdge.YGEdgeStart,
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofUndefined()) : computeEdgeValueForColumn(style_.position(),
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofUndefined());
        return !leadingPosition.isUndefined();
    }

    public boolean isTrailingPosDefined(final @NotNull YGFlexDirection axis) {
        var trailingPosition = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.position(), YGEdge.YGEdgeEnd,
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofUndefined()) : computeEdgeValueForColumn(
                style_.position(),
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofUndefined());
        return !trailingPosition.isUndefined();
    }

    public @NotNull YGFloatOptional getLeadingMargin(final @NotNull YGFlexDirection axis, final float widthSize) {
        var leadingMargin = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.margin(), YGEdge.YGEdgeStart,
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.margin(),
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGResolveValueMargin(leadingMargin, widthSize);
    }

    public @NotNull YGFloatOptional getTrailingMargin(final @NotNull YGFlexDirection axis, final float widthSize) {
        var trailingMargin = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.margin(), YGEdge.YGEdgeEnd,
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.margin(),
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGResolveValueMargin(trailingMargin, widthSize);
    }

    public @NotNull YGFloatOptional getMarginForAxis(final @NotNull YGFlexDirection axis, final float widthSize) {
        return GlobalMembers.plus(getLeadingMargin(axis, widthSize), getTrailingMargin(axis, widthSize));
    }

    public YGSize measure(float width, YGMeasureMode widthMode, float height, YGMeasureMode heightMode, Object layoutContext) {
        return getBooleanData(flags, measureUsesContext_) ? measure_.withContext.invoke(this, width, widthMode, height,
                heightMode, layoutContext) : measure_.noContext.invoke(this, width, widthMode, height, heightMode);
    }

    public float baseline(float width, float height, Object layoutContext) {
        return getBooleanData(flags, baselineUsesContext_) ? baseline_.withContext.invoke(this, width, height,
                layoutContext) : baseline_.noContext.invoke(this, width, height);
    }

    public void setMeasureFunc(@NotNull measure_Struct measureFunc) {
        if (measureFunc.noContext == null) {
            setNodeType(YGNodeType.YGNodeTypeDefault);
        } else {
            GlobalMembers.YGAssertWithNode(this, children_.size() == 0,
                    "Cannot set measure function: Nodes with measure functions cannot have " + "children.");
            setNodeType(YGNodeType.YGNodeTypeText);
        }

        measure_ = measureFunc;
    }

    public void setMeasureFunc(YGMeasureFunc measureFunc) {
        setBooleanData(flags, measureUsesContext_, false);
        measure_.noContext = measureFunc;
        setMeasureFunc(measure_);
    }

    public void setMeasureFunc(MeasureWithContextFn measureFunc) {
        setBooleanData(flags, measureUsesContext_, true);
        measure_.withContext = measureFunc;
        setMeasureFunc(measure_);
    }

    public void replaceChild(YGNode child, Integer index) {
        children_.set(index, child);
    }

    public void replaceChild(YGNode oldChild, YGNode newChild) {
        children_.replaceAll(ygNode -> ygNode.equals(oldChild) ? newChild : ygNode);
    }

    public void insertChild(YGNode child, Integer index) {
        children_.add(index, child);
    }

    public boolean removeChild(YGNode child) {
        return children_.remove(child);
    }

    public void removeChild(int index) {
        children_.remove(index);
    }

    public void setLayoutDirection(YGDirection direction) {
        layout_.setDirection(direction);
    }

    public void setLayoutMargin(float margin, int index) {
        layout_.margin.set(index, margin);
    }

    public void setLayoutBorder(float border, int index) {
        layout_.border.set(index, border);
    }

    public void setLayoutPadding(float padding, int index) {
        layout_.padding.set(index, padding);
    }

    public void setLayoutLastOwnerDirection(YGDirection direction) {
        layout_.lastOwnerDirection = direction;
    }

    public void setLayoutComputedFlexBasis(final YGFloatOptional computedFlexBasis) {
        if (layout_ != null) {
            layout_.computedFlexBasis = computedFlexBasis;
        }
    }

    public void setLayoutPosition(float position, int index) {
        layout_.position.set(index, position);
    }

    public void setLayoutComputedFlexBasisGeneration(Integer computedFlexBasisGeneration) {
        layout_.computedFlexBasisGeneration = computedFlexBasisGeneration;
    }

    public void setLayoutMeasuredDimension(float measuredDimension, int index) {
        layout_.measuredDimensions.set(index, measuredDimension);
    }

    public void setLayoutHadOverflow(boolean hadOverflow) {
        layout_.setHadOverflow(hadOverflow);
    }

    public void setLayoutDimension(float dimension, int index) {
        layout_.dimensions.set(index, dimension);
    }

    public @NotNull YGFloatOptional relativePosition(final @NotNull YGFlexDirection axis, final float axisSize) {
        if (isLeadingPositionDefined(axis)) {
            return getLeadingPosition(axis, axisSize);
        }

        @NotNull YGFloatOptional trailingPosition = getTrailingPosition(axis, axisSize);
        if (!trailingPosition.isUndefined()) {
            trailingPosition = new YGFloatOptional((-1 * trailingPosition.unwrap()));
        }
        return trailingPosition;
    }

    public void setPosition(final YGDirection direction, final float mainSize, final float crossSize, final float ownerWidth) {


        final @NotNull YGDirection directionRespectingRoot = owner_ != null ? direction : YGDirection.YGDirectionLTR;
        final YGFlexDirection mainAxis = GlobalMembers.YGResolveFlexDirection(style_.flexDirection(), directionRespectingRoot);
        final YGFlexDirection crossAxis = GlobalMembers.YGFlexDirectionCross(mainAxis, directionRespectingRoot);


        final @NotNull YGFloatOptional relativePositionMain = relativePosition(mainAxis, mainSize);
        final @NotNull YGFloatOptional relativePositionCross = relativePosition(crossAxis, crossSize);

        setLayoutPosition(GlobalMembers.plus(getLeadingMargin(mainAxis, ownerWidth), relativePositionMain).unwrap(),
                GlobalMembers.leading.get(mainAxis.getValue()).getValue());
        setLayoutPosition(GlobalMembers.plus(getTrailingMargin(mainAxis, ownerWidth), relativePositionMain).unwrap(),
                GlobalMembers.trailing.get(mainAxis.getValue()).getValue());
        setLayoutPosition(GlobalMembers.plus(getLeadingMargin(crossAxis, ownerWidth), relativePositionCross).unwrap(),
                GlobalMembers.leading.get(crossAxis.getValue()).getValue());
        setLayoutPosition(GlobalMembers.plus(getTrailingMargin(crossAxis, ownerWidth), relativePositionCross).unwrap(),
                GlobalMembers.trailing.get(crossAxis.getValue()).getValue());
    }

    public YGValue marginLeadingValue(final @NotNull YGFlexDirection axis) {
        if (GlobalMembers.YGFlexDirectionIsRow(axis) && !style_.margin().getCompactValue(YGEdge.YGEdgeStart).isUndefined()) {
            return style_.margin().get(YGEdge.YGEdgeStart.getValue());
        } else {
            return style_.margin().get(GlobalMembers.leading.get(axis.getValue()).getValue());
        }
    }

    public YGValue marginTrailingValue(final @NotNull YGFlexDirection axis) {
        if (GlobalMembers.YGFlexDirectionIsRow(axis) && !style_.margin().getCompactValue(YGEdge.YGEdgeEnd).isUndefined()) {
            return style_.margin().get(YGEdge.YGEdgeEnd.getValue());
        } else {
            return style_.margin().get(GlobalMembers.trailing.get(axis.getValue()).getValue());
        }
    }

    public YGValue resolveFlexBasisPtr() {
        YGValue flexBasis = style_.flexBasis().convertToYgValue();
        if (flexBasis.unit != YGUnit.YGUnitAuto && flexBasis.unit != YGUnit.YGUnitUndefined) {
            return flexBasis;
        }
        if (!style_.flex().isUndefined() && style_.flex().unwrap() > 0.0f) {
            return getBooleanData(flags, useWebDefaults_) ? GlobalMembers.YGValueAuto : GlobalMembers.YGValueZero;
        }
        return GlobalMembers.YGValueAuto;
    }

    public void resolveDimension() {
        final YGStyle style = getStyle();
        YGDimension @NotNull [] dimensions = new YGDimension[]{YGDimension.YGDimensionWidth, YGDimension.YGDimensionHeight};
        for (@NotNull var dim : dimensions) {
            if (!style.maxDimensions().getCompactValue(dim.getValue()).isUndefined() && GlobalMembers.YGValueEqual(
                    style.maxDimensions().getCompactValue(dim.getValue()),
                    style.minDimensions().getCompactValue(dim.getValue()))) {
                resolvedDimensions_.set(dim.getValue(), style.maxDimensions().get(dim.getValue()));
            } else {
                resolvedDimensions_.set(dim.getValue(), style.dimensions().get(dim.getValue()));
            }
        }
    }

    public YGDirection resolveDirection(final @NotNull YGDirection ownerDirection) {
        if (style_.direction() == YGDirection.YGDirectionInherit) {
            return ownerDirection.getValue() > YGDirection.YGDirectionInherit.getValue() ? ownerDirection : YGDirection.YGDirectionLTR;
        } else {
            return style_.direction();
        }
    }

    public void clearChildren() {
        children_.clear();
    }

    public void cloneChildrenIfNeeded(Object cloneContext) {
        iterChildrenAfterCloningIfNeeded((YGNode UnnamedParameter, Object UnnamedParameter2) ->
        {
        }, cloneContext);
    }


    public final <T> void iterChildrenAfterCloningIfNeeded(@NotNull BiConsumer<YGNode, Object> callback, Object cloneContext) {
        int i = 0;
        for (@NotNull YGNode child : children_) {
            if (child.getOwner() != this) {
                child = config_.cloneNode(child, this, i, cloneContext);
                child.setOwner(this);
            }
            i += 1;

            callback.accept(child, cloneContext);
        }
    }

    public void markDirtyAndPropogate() {
        if (!getBooleanData(flags, isDirty_)) {
            setDirty(true);
            setLayoutComputedFlexBasis(new YGFloatOptional());
            if (owner_ != null) {
                owner_.markDirtyAndPropogate();
            }
        }
    }

    public void markDirtyAndPropogateDownwards() {
        setBooleanData(flags, isDirty_, true);
        children_.forEach(YGNode::markDirtyAndPropogateDownwards);
    }

    public float resolveFlexGrow() {

        if (owner_ == null) {
            return 0.0F;
        }
        if (!style_.flexGrow().isUndefined()) {
            return style_.flexGrow().unwrap();
        }
        if (!style_.flex().isUndefined() && style_.flex().unwrap() > 0.0f) {
            return style_.flex().unwrap();
        }
        return GlobalMembers.kDefaultFlexGrow;
    }

    public float resolveFlexShrink() {
        if (owner_ == null) {
            return 0.0F;
        }
        if (!style_.flexShrink().isUndefined()) {
            return style_.flexShrink().unwrap();
        }
        if (!getBooleanData(flags, useWebDefaults_) && !style_.flex().isUndefined() && style_.flex().unwrap() < 0.0f) {
            return -style_.flex().unwrap();
        }
        return getBooleanData(flags, useWebDefaults_) ? GlobalMembers.kWebDefaultFlexShrink : GlobalMembers.kDefaultFlexShrink;
    }

    public boolean isNodeFlexible() {
        return ((style_.positionType() != YGPositionType.YGPositionTypeAbsolute) && (resolveFlexGrow() != 0 || resolveFlexShrink() != 0));
    }

    public float getLeadingBorder(final @NotNull YGFlexDirection axis) {
        YGValue leadingBorder = (GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.border(),
                YGEdge.YGEdgeStart,
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.border(),
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero())).convertToYgValue();
        return Math.max(leadingBorder.value, 0.0f);
    }

    public float getTrailingBorder(final @NotNull YGFlexDirection axis) {
        YGValue trailingBorder = (GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.border(), YGEdge.YGEdgeEnd,
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.border(),
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero())).convertToYgValue();
        return Math.max(trailingBorder.value, 0.0f);
    }

    public YGFloatOptional getLeadingPadding(final @NotNull YGFlexDirection axis, final float widthSize) {
        var leadingPadding = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.padding(), YGEdge.YGEdgeStart,
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.padding(),
                GlobalMembers.leading.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGFloatOptionalMax(GlobalMembers.YGResolveValue(leadingPadding, widthSize), new YGFloatOptional(0.0f));
    }

    public YGFloatOptional getTrailingPadding(final @NotNull YGFlexDirection axis, final float widthSize) {
        var trailingPadding = GlobalMembers.YGFlexDirectionIsRow(axis) ? computeEdgeValueForRow(style_.padding(), YGEdge.YGEdgeEnd,
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero()) : computeEdgeValueForColumn(style_.padding(),
                GlobalMembers.trailing.get(axis.getValue()), CompactValue.ofZero());
        return GlobalMembers.YGFloatOptionalMax(GlobalMembers.YGResolveValue(trailingPadding, widthSize), new YGFloatOptional(0.0f));
    }

    public @NotNull YGFloatOptional getLeadingPaddingAndBorder(final @NotNull YGFlexDirection axis, final float widthSize) {
        return GlobalMembers.plus(getLeadingPadding(axis, widthSize), new YGFloatOptional(getLeadingBorder(axis)));
    }

    public @NotNull YGFloatOptional getTrailingPaddingAndBorder(final @NotNull YGFlexDirection axis, final float widthSize) {
        return GlobalMembers.plus(getTrailingPadding(axis, widthSize), new YGFloatOptional(getTrailingBorder(axis)));
    }

    public boolean didUseLegacyFlag() {
        boolean didUseLegacyFlag = layout_.didUseLegacyFlag();
        if (didUseLegacyFlag) {
            return true;
        }
        for (@NotNull var child : children_) {
            if (child.layout_.didUseLegacyFlag()) {
                didUseLegacyFlag = true;
                break;
            }
        }
        return didUseLegacyFlag;
    }

    public void setLayoutDoesLegacyFlagAffectsLayout(boolean doesLegacyFlagAffectsLayout) {
        layout_.setDoesLegacyStretchFlagAffectsLayout(doesLegacyFlagAffectsLayout);
    }

    public void setLayoutDidUseLegacyFlag(boolean didUseLegacyFlag) {
        layout_.setDidUseLegacyFlag(didUseLegacyFlag);
    }

    public boolean isLayoutTreeEqualToNode(final @NotNull YGNode node) {
        if (children_.size() != node.children_.size()) {
            return false;
        }
        if (layout_ != node.layout_) {
            return false;
        }
        if (children_.size() == 0) {
            return true;
        }

        boolean isLayoutTreeEqual = true;
        for (int i = 0, children_size = children_.size(); i < children_size; i++) {
            YGNode child = children_.get(i);
            YGNode otherNodeChildren = node.children_.get(i);
            isLayoutTreeEqual = child.isLayoutTreeEqualToNode(otherNodeChildren);
            if (!isLayoutTreeEqual)
                break;
        }
        return isLayoutTreeEqual;
    }

    public void reset() {
        GlobalMembers.YGAssertWithNode(this, children_.size() == 0, "Cannot reset a node which still has children attached");
        GlobalMembers.YGAssertWithNode(this, owner_ == null, "Cannot reset a node still attached to a owner");

        clearChildren();

        var webDefaults = getBooleanData(flags, useWebDefaults_);
        this.config_ = new YGConfig(null);
        if (webDefaults) {
            useWebDefaults();
        }
    }

    public @Nullable Object getContext() {
        return context_;
    }

    public void setContext(Object context_) {
        this.context_ = context_;
    }

    public Map<Object, Object> getFlags() {
        return flags;
    }

    public void setFlags(Map<Object, Object> flags) {
        this.flags = flags;
    }

    public byte getReserved() {
        return reserved_;
    }

    public void setReserved(byte reserved_) {
        this.reserved_ = reserved_;
    }

    public @NotNull measure_Struct getMeasure() {
        return measure_;
    }

    public void setMeasure(measure_Struct measure_) {
        this.measure_ = measure_;
    }

    public @NotNull baseline_Struct getBaseline() {
        return baseline_;
    }

    public void setBaseline(baseline_Struct baseline_) {
        this.baseline_ = baseline_;
    }

    public @NotNull print_Struct getPrint() {
        return print_;
    }

    public void setPrint(print_Struct print_) {
        this.print_ = print_;
    }

    public @NotNull YGDirtiedFunc getDirtied() {
        return dirtied_;
    }

    public void setDirtied(YGDirtiedFunc dirtied_) {
        this.dirtied_ = dirtied_;
    }

    public YGStyle getStyle() {
        return style_;
    }

    public void setStyle(YGStyle style_) {
        this.style_ = style_;
    }

    public YGLayout getLayout() {
        return layout_;
    }

    public void setLayout(YGLayout layout_) {
        if (layout_ == null) layout_ = new YGLayout();
        this.layout_ = layout_;
    }

    public int getLineIndex() {
        return lineIndex_;
    }

    public void setLineIndex(int lineIndex_) {
        this.lineIndex_ = lineIndex_;
    }

    public @Nullable YGNode getOwner() {
        return owner_;
    }

    public void setOwner(YGNode owner_) {
        this.owner_ = owner_;
    }

    public ArrayList<YGNode> getChildren() {
        return children_;
    }

    public void setChildren(ArrayList<YGNode> children_) {
        this.children_ = children_;
    }

    public @Nullable YGConfig getConfig() {
        return config_;
    }

    public void setConfig(YGConfig config_) {
        this.config_ = config_;
    }

    public ArrayList<YGValue> getResolvedDimensions() {
        return resolvedDimensions_;
    }

    public void setResolvedDimensions(ArrayList<YGValue> resolvedDimensions_) {
        this.resolvedDimensions_ = resolvedDimensions_;
    }

    public static class measure_Struct {

        public @Nullable YGMeasureFunc noContext = null;
        public MeasureWithContextFn withContext;

    }

    public static class baseline_Struct {

        public @Nullable YGBaselineFunc noContext = null;
        public BaselineWithContextFn withContext;

    }

    public static class print_Struct {

        public @Nullable YGPrintFunc noContext = null;
        public PrintWithContextFn withContext;

    }
}
