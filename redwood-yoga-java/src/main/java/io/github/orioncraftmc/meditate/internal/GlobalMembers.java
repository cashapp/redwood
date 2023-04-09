package io.github.orioncraftmc.meditate.internal;

import static io.github.orioncraftmc.meditate.internal.YGLayout.YG_MAX_CACHED_RESULT_COUNT;
import io.github.orioncraftmc.meditate.internal.detail.*;
import io.github.orioncraftmc.meditate.internal.enums.*;
import static io.github.orioncraftmc.meditate.internal.enums.YGAlign.YGAlignBaseline;
import static io.github.orioncraftmc.meditate.internal.enums.YGAlign.YGAlignCenter;
import static io.github.orioncraftmc.meditate.internal.enums.YGAlign.YGAlignFlexStart;
import static io.github.orioncraftmc.meditate.internal.enums.YGAlign.YGAlignStretch;
import static io.github.orioncraftmc.meditate.internal.enums.YGDimension.YGDimensionHeight;
import static io.github.orioncraftmc.meditate.internal.enums.YGDimension.YGDimensionWidth;
import static io.github.orioncraftmc.meditate.internal.enums.YGDirection.YGDirectionLTR;
import static io.github.orioncraftmc.meditate.internal.enums.YGDirection.YGDirectionRTL;
import static io.github.orioncraftmc.meditate.internal.enums.YGDisplay.YGDisplayNone;
import static io.github.orioncraftmc.meditate.internal.enums.YGEdge.*;
import static io.github.orioncraftmc.meditate.internal.enums.YGFlexDirection.YGFlexDirectionColumn;
import static io.github.orioncraftmc.meditate.internal.enums.YGFlexDirection.YGFlexDirectionColumnReverse;
import static io.github.orioncraftmc.meditate.internal.enums.YGFlexDirection.YGFlexDirectionRow;
import static io.github.orioncraftmc.meditate.internal.enums.YGFlexDirection.YGFlexDirectionRowReverse;
import static io.github.orioncraftmc.meditate.internal.enums.YGLogLevel.YGLogLevelFatal;
import static io.github.orioncraftmc.meditate.internal.enums.YGLogLevel.YGLogLevelVerbose;
import static io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode.YGMeasureModeAtMost;
import static io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode.YGMeasureModeExactly;
import static io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode.YGMeasureModeUndefined;
import static io.github.orioncraftmc.meditate.internal.enums.YGOverflow.YGOverflowScroll;
import static io.github.orioncraftmc.meditate.internal.enums.YGPositionType.YGPositionTypeAbsolute;
import static io.github.orioncraftmc.meditate.internal.enums.YGUnit.YGUnitAuto;
import static io.github.orioncraftmc.meditate.internal.enums.YGWrap.YGWrapNoWrap;
import static io.github.orioncraftmc.meditate.internal.enums.YGWrap.YGWrapWrapReverse;
import io.github.orioncraftmc.meditate.internal.event.*;
import io.github.orioncraftmc.meditate.internal.interfaces.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import org.jetbrains.annotations.*;

public class GlobalMembers {
    public static final float YGUndefined = Float.NaN;
    public static final YGValue YGValueAuto = new YGValue(YGUndefined, YGUnitAuto);
    public static final YGValue YGValueUndefined = new YGValue(YGUndefined, YGUnit.YGUnitUndefined);
    public static final YGValue YGValueZero = new YGValue(0, YGUnit.YGUnitPoint);
    public static final ArrayList<YGEdge> leading = new ArrayList<>(
            List.of(YGEdgeTop, YGEdgeBottom, YGEdgeLeft, YGEdgeRight));
    public static final ArrayList<YGEdge> trailing = new ArrayList<>(
            List.of(YGEdgeBottom, YGEdgeTop, YGEdgeRight, YGEdgeLeft));
    public static final ArrayList<YGEdge> pos = new ArrayList<>(
            List.of(YGEdgeTop, YGEdgeBottom, YGEdgeLeft, YGEdgeRight));
    public static final ArrayList<YGDimension> dim = new ArrayList<>(
            List.of(YGDimensionHeight, YGDimensionHeight, YGDimensionWidth,
                    YGDimensionWidth));
    public static final String spacer = "                                                            ";
    public static final float kDefaultFlexGrow = 0.0f;
    public static final float kDefaultFlexShrink = 0.0f;
    public static final float kWebDefaultFlexShrink = 1.0f;
    public static final @NotNull YGConfig defaultConfig = YGConfigNew();
    public static final @NotNull AtomicInteger gCurrentGenerationCount = new AtomicInteger();
    public static final boolean gPrintChanges = false;
    public static final boolean gPrintSkips = false;
    public static int gConfigInstanceCount = 0;

    public static boolean isUndefined(float value) {
        return Float.isNaN(value);
    }

    public static boolean isUndefined(double value) {
        return Double.isNaN(value);
    }

    public static boolean YGValueEqual(final @NotNull YGValue a, final @NotNull YGValue b) //Method definition originates from: Utils.cpp
    {
        if (a.unit != b.unit) {
            return false;
        }

        if (a.unit == YGUnit.YGUnitUndefined || (isUndefined(a.value) && isUndefined(b.value))) {
            return true;
        }

        return Math.abs(a.value - b.value) < 0.0001f;
    }

    public static boolean YGValueEqual(@NotNull CompactValue a, @NotNull CompactValue b) {
        return YGValueEqual(a.convertToYgValue(), b.convertToYgValue());
    }

    public static boolean YGFloatsEqual(final float a, final float b) //Method definition originates from: Utils.cpp
    {
        if (!isUndefined(a) && !isUndefined(b)) {
            return Math.abs(a - b) < 0.0001f;
        }
        return isUndefined(a) && isUndefined(b);
    }

    public static boolean YGDoubleEqual(final double a, final double b) //Method definition originates from: Utils.cpp
    {
        if (!isUndefined(a) && !isUndefined(b)) {
            return Math.abs(a - b) < 0.0001;
        }
        return isUndefined(a) && isUndefined(b);
    }

    public static float YGFloatMax(final float a, final float b) //Method definition originates from: Utils.cpp
    {
        if (!isUndefined(a) && !isUndefined(b)) {
            return Math.max(a, b);
        }
        return isUndefined(a) ? b : a;
    }

    public static @NotNull YGFloatOptional YGFloatOptionalMax(@NotNull YGFloatOptional op1, @NotNull YGFloatOptional op2) //Method definition originates from: Utils.cpp
    {
        if (greaterThanOrEqualTo(op1, op2)) {
            return op1;
        }
        if (greaterThan(op2, op1)) {
            return op2;
        }
        return op1.isUndefined() ? op2 : op1;
    }

    public static float YGFloatMin(final float a, final float b) //Method definition originates from: Utils.cpp
    {
        if (!isUndefined(a) && !isUndefined(b)) {
            return Math.min(a, b);
        }

        return isUndefined(a) ? b : a;
    }

    public static boolean YGFloatArrayEqual(final @NotNull ArrayList<Float> val1, final @NotNull ArrayList<Float> val2) {
        boolean areEqual = true;
        for (int i = 0; i < val1.size() && areEqual; ++i) {
            areEqual = YGFloatsEqual(val1.get(i), val2.get(i));
        }
        return areEqual;
    }

    public static float YGFloatSanitize(final float val) //Method definition originates from: Utils.cpp
    {
        return isUndefined(val) ? 0F : val;
    }

    public static @NotNull YGFlexDirection YGFlexDirectionCross(final YGFlexDirection flexDirection, final YGDirection direction) //Method definition originates from: Utils.cpp
    {
        return YGFlexDirectionIsColumn(flexDirection) ? YGResolveFlexDirection(YGFlexDirectionRow,
                direction) : YGFlexDirectionColumn;
    }

    public static boolean YGFlexDirectionIsRow(final YGFlexDirection flexDirection) {
        return flexDirection == YGFlexDirectionRow || flexDirection == YGFlexDirectionRowReverse;
    }

    public static @NotNull YGFloatOptional YGResolveValue(final @NotNull YGValue value, final float ownerSize) {
        return switch (value.unit) {
            case YGUnitPoint -> new YGFloatOptional(value.value);
            case YGUnitPercent -> new YGFloatOptional(value.value * ownerSize * 0.01f);
            default -> new YGFloatOptional();
        };
    }

    public static @NotNull YGFloatOptional YGResolveValue(@NotNull CompactValue value, float ownerSize) {
        return YGResolveValue(value.convertToYgValue(), ownerSize);
    }

    public static boolean YGFlexDirectionIsColumn(final YGFlexDirection flexDirection) {
        return flexDirection == YGFlexDirectionColumn || flexDirection == YGFlexDirectionColumnReverse;
    }

    public static YGFlexDirection YGResolveFlexDirection(final YGFlexDirection flexDirection, final YGDirection direction) {
        if (direction == YGDirectionRTL) {
            if (flexDirection == YGFlexDirectionRow) {
                return YGFlexDirectionRowReverse;
            } else if (flexDirection == YGFlexDirectionRowReverse) {
                return YGFlexDirectionRow;
            }
        }

        return flexDirection;
    }

    public static @NotNull YGFloatOptional YGResolveValueMargin(@NotNull CompactValue value, final float ownerSize) {
        return value.isAuto() ? new YGFloatOptional(0) : YGResolveValue(value, ownerSize);
    }

    public static @NotNull String YGAlignToString(final @NotNull YGAlign value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGAlignAuto -> "auto";
            case YGAlignFlexStart -> "flex-start";
            case YGAlignCenter -> "center";
            case YGAlignFlexEnd -> "flex-end";
            case YGAlignStretch -> "stretch";
            case YGAlignBaseline -> "baseline";
            case YGAlignSpaceBetween -> "space-between";
            case YGAlignSpaceAround -> "space-around";
        };
    }

    public static @NotNull String LayoutPassReasonToString(final LayoutPassReason value) {
        if (value == LayoutPassReason.kInitial) {
            return "initial";
        } else if (value == LayoutPassReason.kAbsLayout) {
            return "abs_layout";
        } else if (value == LayoutPassReason.kStretch) {
            return "stretch";
        } else if (value == LayoutPassReason.kMultilineStretch) {
            return "multiline_stretch";
        } else if (value == LayoutPassReason.kFlexLayout) {
            return "flex_layout";
        } else if (value == LayoutPassReason.kMeasureChild) {
            return "measure";
        } else if (value == LayoutPassReason.kAbsMeasureChild) {
            return "abs_measure";
        } else if (value == LayoutPassReason.kFlexMeasure) {
            return "flex_measure";
        }
        return "unknown";
    }

    //					   ;
    public static @NotNull String YGDimensionToString(final @NotNull YGDimension value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGDimensionWidth -> "width";
            case YGDimensionHeight -> "height";
        };
    }

    public static @NotNull String YGDirectionToString(final @NotNull YGDirection value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGDirectionInherit -> "inherit";
            case YGDirectionLTR -> "ltr";
            case YGDirectionRTL -> "rtl";
        };
    }

    public static @NotNull String YGDisplayToString(final @NotNull YGDisplay value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGDisplayFlex -> "flex";
            case YGDisplayNone -> "none";
        };
    }

    public static @NotNull String YGEdgeToString(final @NotNull YGEdge value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGEdgeLeft -> "left";
            case YGEdgeTop -> "top";
            case YGEdgeRight -> "right";
            case YGEdgeBottom -> "bottom";
            case YGEdgeStart -> "start";
            case YGEdgeEnd -> "end";
            case YGEdgeHorizontal -> "horizontal";
            case YGEdgeVertical -> "vertical";
            case YGEdgeAll -> "all";
        };
    }

    public static @NotNull String YGExperimentalFeatureToString(final @NotNull YGExperimentalFeature value) //Method definition originates from: YGEnums.cpp
    {
        if (value == YGExperimentalFeature.YGExperimentalFeatureWebFlexBasis) {
            return "web-flex-basis";
        }
        return "unknown";
    }

    public static @NotNull String YGFlexDirectionToString(final @NotNull YGFlexDirection value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGFlexDirectionColumn -> "column";
            case YGFlexDirectionColumnReverse -> "column-reverse";
            case YGFlexDirectionRow -> "row";
            case YGFlexDirectionRowReverse -> "row-reverse";
        };
    }

    public static @NotNull String YGJustifyToString(final @NotNull YGJustify value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGJustifyFlexStart -> "flex-start";
            case YGJustifyCenter -> "center";
            case YGJustifyFlexEnd -> "flex-end";
            case YGJustifySpaceBetween -> "space-between";
            case YGJustifySpaceAround -> "space-around";
            case YGJustifySpaceEvenly -> "space-evenly";
        };
    }

    public static @NotNull String YGLogLevelToString(final @NotNull YGLogLevel value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGLogLevelError -> "error";
            case YGLogLevelWarn -> "warn";
            case YGLogLevelInfo -> "info";
            case YGLogLevelDebug -> "debug";
            case YGLogLevelVerbose -> "verbose";
            case YGLogLevelFatal -> "fatal";
        };
    }

    public static @NotNull String YGMeasureModeToString(final @NotNull YGMeasureMode value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGMeasureModeUndefined -> "undefined";
            case YGMeasureModeExactly -> "exactly";
            case YGMeasureModeAtMost -> "at-most";
        };
    }

    public static @NotNull String YGNodeTypeToString(final @NotNull YGNodeType value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGNodeTypeDefault -> "default";
            case YGNodeTypeText -> "text";
        };
    }

    public static @NotNull String YGOverflowToString(final @NotNull YGOverflow value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGOverflowVisible -> "visible";
            case YGOverflowHidden -> "hidden";
            case YGOverflowScroll -> "scroll";
        };
    }

    public static @NotNull String YGPositionTypeToString(final @NotNull YGPositionType value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGPositionTypeStatic -> "static";
            case YGPositionTypeRelative -> "relative";
            case YGPositionTypeAbsolute -> "absolute";
        };
    }

    public static @NotNull String YGPrintOptionsToString(final @NotNull YGPrintOptions value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGPrintOptionsLayout -> "layout";
            case YGPrintOptionsStyle -> "style";
            case YGPrintOptionsChildren -> "children";
        };
    }

    public static @NotNull String YGUnitToString(final @NotNull YGUnit value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGUnitUndefined -> "undefined";
            case YGUnitPoint -> "point";
            case YGUnitPercent -> "percent";
            case YGUnitAuto -> "auto";
        };
    }

    public static @NotNull String YGWrapToString(final @NotNull YGWrap value) //Method definition originates from: YGEnums.cpp
    {
        return switch (value) {
            case YGWrapNoWrap -> "no-wrap";
            case YGWrapWrap -> "wrap";
            case YGWrapWrapReverse -> "wrap-reverse";
        };
    }

    public static @NotNull YGNode YGNodeNew() //Method definition originates from: Yoga.cpp
    {
        return YGNodeNewWithConfig(YGConfigGetDefault());
    }

    public static @NotNull YGNode YGNodeNewWithConfig(YGConfig config) //Method definition originates from: Yoga.cpp
    {
        @NotNull YGNode node = new YGNode((config));
        YGAssertWithConfig(config, node != null, "Could not allocate memory for node");
        Event.publish(node, new NodeAllocationEventData(config));

        return node;
    }

    public static @NotNull YGNode YGNodeClone(@NotNull YGNode oldNode) //Method definition originates from: Yoga.cpp
    {
        @NotNull YGNode node = new YGNode(oldNode);
        YGAssertWithConfig(oldNode.getConfig(), node != null, "Could not allocate memory for node");
        Event.publish(node, new NodeAllocationEventData(node.getConfig()));
        node.setOwner(null);
        return node;
    }

    public static void YGNodeFree(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        @Nullable YGNode owner = node.getOwner();
        if (owner != null) {
            owner.removeChild(node);
            node.setOwner(null);
        }

        final @NotNull Integer childCount = YGNodeGetChildCount(node);
        for (@NotNull Integer i = 0; i < childCount; i++) {
            @Nullable YGNode child = YGNodeGetChild(node, i);
            if (child != null) {
                child.setOwner(null);
            }
        }

        node.clearChildren();
        Event.publish(node, new NodeDeallocationEventData(node.getConfig()));
    }

    public static void YGNodeFreeRecursiveWithCleanupFunc(@NotNull YGNode root, @Nullable YGNodeCleanupFunc cleanup) //Method definition originates from: Yoga.cpp
    {
        int skipped = 0;
        while (YGNodeGetChildCount(root) > skipped) {
            @Nullable YGNode child = YGNodeGetChild(root, skipped);
            if (child != null) {
                if (child.getOwner() != root) {
                    skipped += 1;
                } else {
                    YGNodeRemoveChild(root, child);
                    YGNodeFreeRecursive(child);
                }
            }
        }
        if (cleanup != null) {
            cleanup.invoke(root);
        }
        YGNodeFree(root);
    }

    public static void YGNodeFreeRecursive(@NotNull YGNode root) //Method definition originates from: Yoga.cpp
    {
        YGNodeFreeRecursiveWithCleanupFunc(root, null);
    }

    public static void YGNodeReset(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        node.reset();
    }

    public static void YGNodeInsertChild(@NotNull YGNode owner, @NotNull YGNode child, final Integer index) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithNode(owner, child.getOwner() == null, "Child already has a owner, it must be removed first.");

        YGAssertWithNode(owner, !owner.hasMeasureFunc(),
                "Cannot add child: Nodes with measure functions cannot have children.");

        owner.insertChild(child, index);
        child.setOwner(owner);
        owner.markDirtyAndPropogate();
    }

    public static void YGNodeSwapChild(@NotNull YGNode owner, @NotNull YGNode child, final Integer index) //Method definition originates from: Yoga.cpp
    {
        owner.replaceChild(child, index);
        child.setOwner(owner);
    }

    public static void YGNodeRemoveChild(@NotNull YGNode owner, @NotNull YGNode excludedChild) //Method definition originates from: Yoga.cpp
    {
        if (YGNodeGetChildCount(owner) == 0) {

            return;
        }

        @Nullable var childOwner = excludedChild.getOwner();
        if (owner.removeChild(excludedChild)) {
            if (owner == childOwner) {
                excludedChild.setLayout(null);
                excludedChild.setOwner(null);
            }
            owner.markDirtyAndPropogate();
        }
    }

    public static void YGNodeRemoveAllChildren(@NotNull YGNode owner) //Method definition originates from: Yoga.cpp
    {
        final @NotNull Integer childCount = YGNodeGetChildCount(owner);
        if (childCount == 0) {

            return;
        }
        @Nullable YGNode firstChild = YGNodeGetChild(owner, 0);
        if (firstChild != null && firstChild.getOwner() == owner) {

            for (@NotNull Integer i = 0; i < childCount; i++) {
                @Nullable YGNode oldChild = YGNodeGetChild(owner, i);
                oldChild.setLayout(null);
                oldChild.setOwner(null);
            }
            owner.clearChildren();
            owner.markDirtyAndPropogate();
            return;
        }


        owner.getChildren().clear();
        owner.markDirtyAndPropogate();
    }

    public static @Nullable YGNode YGNodeGetChild(@NotNull YGNode node, final Integer index) //Method definition originates from: Yoga.cpp
    {
        if (index < node.getChildren().size()) {
            return node.getChild(index);
        }
        return null;
    }

    public static @Nullable YGNode YGNodeGetOwner(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getOwner();
    }

    public static @Nullable YGNode YGNodeGetParent(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getOwner();
    }

    public static Integer YGNodeGetChildCount(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getChildren().size();
    }

    public static void YGNodeSetChildren(YGNode owner, YGNode[] c, final Integer count) //Method definition originates from: Yoga.cpp
    {
        @NotNull ArrayList<YGNode> children = new ArrayList<>(Arrays.asList(c));
        YGNodeSetChildrenInternal(owner, children);
    }

    public static void YGNodeSetIsReferenceBaseline(@NotNull YGNode node, boolean isReferenceBaseline) //Method definition originates from: Yoga.cpp
    {
        if (node.isReferenceBaseline() != isReferenceBaseline) {
            node.setIsReferenceBaseline(isReferenceBaseline);
            node.markDirtyAndPropogate();
        }
    }

    public static boolean YGNodeIsReferenceBaseline(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.isReferenceBaseline();
    }

    public static void YGNodeCalculateLayout(@NotNull YGNode node, final float ownerWidth, final float ownerHeight, final @NotNull YGDirection ownerDirection) //Method definition originates from: Yoga.cpp
    {
        YGNodeCalculateLayoutWithContext(node, ownerWidth, ownerHeight, ownerDirection, null);
    }

    public static void YGNodeMarkDirty(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithNode(node, node.hasMeasureFunc(),
                "Only leaf nodes with custom measure functions" + "should manually mark themselves as dirty");

        node.markDirtyAndPropogate();
    }

    public static void YGNodeMarkDirtyAndPropogateToDescendants(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        node.markDirtyAndPropogateDownwards();
    }

    public static boolean YGFloatIsUndefined(final float value) //Method definition originates from: Yoga.cpp
    {
        return isUndefined(value);
    }

    public static boolean YGNodeCanUseCachedMeasurement(final YGMeasureMode widthMode, final float width, final YGMeasureMode heightMode, final float height, final YGMeasureMode lastWidthMode, final float lastWidth, final YGMeasureMode lastHeightMode, final float lastHeight, final float lastComputedWidth, final float lastComputedHeight, final float marginRow, final float marginColumn, @Nullable YGConfig config) //Method definition originates from: Yoga.cpp
    {
        if ((!YGFloatIsUndefined(lastComputedHeight) && lastComputedHeight < 0F) || (!YGFloatIsUndefined(
                lastComputedWidth) && lastComputedWidth < 0F)) {
            return false;
        }
        boolean useRoundedComparison = config != null && config.pointScaleFactor != 0;
        final float effectiveWidth = useRoundedComparison ? YGRoundValueToPixelGrid(width, config.pointScaleFactor,
                false, false) : width;
        final float effectiveHeight = useRoundedComparison ? YGRoundValueToPixelGrid(height, config.pointScaleFactor,
                false, false) : height;
        final float effectiveLastWidth = useRoundedComparison ? YGRoundValueToPixelGrid(lastWidth,
                config.pointScaleFactor, false, false) : lastWidth;
        final float effectiveLastHeight = useRoundedComparison ? YGRoundValueToPixelGrid(lastHeight,
                config.pointScaleFactor, false, false) : lastHeight;

        final boolean hasSameWidthSpec = lastWidthMode == widthMode && YGFloatsEqual(effectiveLastWidth,
                effectiveWidth);
        final boolean hasSameHeightSpec = lastHeightMode == heightMode && YGFloatsEqual(effectiveLastHeight,
                effectiveHeight);

        final boolean widthIsCompatible = hasSameWidthSpec || YGMeasureModeSizeIsExactAndMatchesOldMeasuredSize(
                widthMode, width - marginRow, lastComputedWidth) || YGMeasureModeOldSizeIsUnspecifiedAndStillFits(
                widthMode, width - marginRow, lastWidthMode,
                lastComputedWidth) || YGMeasureModeNewMeasureSizeIsStricterAndStillValid(widthMode, width - marginRow,
                lastWidthMode, lastWidth, lastComputedWidth);

        final boolean heightIsCompatible = hasSameHeightSpec || YGMeasureModeSizeIsExactAndMatchesOldMeasuredSize(
                heightMode, height - marginColumn, lastComputedHeight) || YGMeasureModeOldSizeIsUnspecifiedAndStillFits(
                heightMode, height - marginColumn, lastHeightMode,
                lastComputedHeight) || YGMeasureModeNewMeasureSizeIsStricterAndStillValid(heightMode,
                height - marginColumn, lastHeightMode, lastHeight, lastComputedHeight);

        return widthIsCompatible && heightIsCompatible;
    }

    public static void YGNodeCopyStyle(@NotNull YGNode dstNode, @NotNull YGNode srcNode) //Method definition originates from: Yoga.cpp
    {
        if (!(dstNode.getStyle() == srcNode.getStyle())) {
            dstNode.setStyle(srcNode.getStyle());
            dstNode.markDirtyAndPropogate();
        }
    }

    public static @Nullable Object YGNodeGetContext(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getContext();
    }

    public static void YGNodeSetContext(@NotNull YGNode node, Object context) //Method definition originates from: Yoga.cpp
    {
        node.setContext(context);
    }

    public static void YGConfigSetPrintTreeFlag(@NotNull YGConfig config, boolean enabled) //Method definition originates from: Yoga.cpp
    {
        config.printTree = enabled;
    }

    public static boolean YGNodeHasMeasureFunc(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.hasMeasureFunc();
    }

    public static void YGNodeSetMeasureFunc(@NotNull YGNode node, YGMeasureFunc measureFunc) //Method definition originates from: Yoga.cpp
    {
        node.setMeasureFunc(measureFunc);
    }

    public static boolean YGNodeHasBaselineFunc(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.hasBaselineFunc();
    }

    public static void YGNodeSetBaselineFunc(@NotNull YGNode node, YGBaselineFunc baselineFunc) //Method definition originates from: Yoga.cpp
    {
        node.setBaselineFunc(baselineFunc);
    }

    public static @Nullable YGDirtiedFunc YGNodeGetDirtiedFunc(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getDirtied();
    }

    public static void YGNodeSetDirtiedFunc(@NotNull YGNode node, YGDirtiedFunc dirtiedFunc) //Method definition originates from: Yoga.cpp
    {
        node.setDirtiedFunc(dirtiedFunc);
    }

    public static void YGNodeSetPrintFunc(@NotNull YGNode node, YGPrintFunc printFunc) //Method definition originates from: Yoga.cpp
    {
        node.setPrintFunc(printFunc);
    }

    public static boolean YGNodeGetHasNewLayout(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getHasNewLayout();
    }

    public static void YGNodeSetHasNewLayout(@NotNull YGNode node, boolean hasNewLayout) //Method definition originates from: Yoga.cpp
    {
        node.setHasNewLayout(hasNewLayout);
    }

    public static YGNodeType YGNodeGetNodeType(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getNodeType();
    }

    public static void YGNodeSetNodeType(@NotNull YGNode node, @NotNull YGNodeType nodeType) //Method definition originates from: Yoga.cpp
    {
        node.setNodeType(nodeType);
    }

    public static boolean YGNodeIsDirty(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.isDirty();
    }

    public static boolean YGNodeLayoutGetDidUseLegacyFlag(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.didUseLegacyFlag();
    }

    public static void YGNodeStyleSetDirection(@NotNull YGNode node, final YGDirection value) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGDirection.class, value, YGStyle::directionBitfieldRef);
    }

    public static void YGNodeStyleSetFlexDirection(@NotNull YGNode node, final YGFlexDirection flexDirection) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGFlexDirection.class, flexDirection, YGStyle::flexDirectionBitfieldRef);
    }

    public static void YGNodeStyleSetJustifyContent(@NotNull YGNode node, final YGJustify justifyContent) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGJustify.class, justifyContent, YGStyle::justifyContentBitfieldRef);

    }

    public static void YGNodeStyleSetAlignContent(@NotNull YGNode node, final YGAlign alignContent) //Method definition originates from: Yoga.cpp
    {

        updateStyle(node, YGAlign.class, alignContent, YGStyle::alignContentBitfieldRef);
    }

    public static void YGNodeStyleSetAlignItems(@NotNull YGNode node, final YGAlign alignItems) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGAlign.class, alignItems, YGStyle::alignItemsBitfieldRef);
    }

    public static void YGNodeStyleSetAlignSelf(@NotNull YGNode node, final YGAlign alignSelf) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGAlign.class, alignSelf, YGStyle::alignSelfBitfieldRef);
    }

    public static void YGNodeStyleSetPositionType(@NotNull YGNode node, final YGPositionType positionType) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGPositionType.class, positionType, YGStyle::positionTypeBitfieldRef);
    }

    public static void YGNodeStyleSetFlexWrap(@NotNull YGNode node, final YGWrap flexWrap) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGWrap.class, flexWrap, YGStyle::flexWrapBitfieldRef);
    }

    public static void YGNodeStyleSetOverflow(@NotNull YGNode node, final YGOverflow overflow) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGOverflow.class, overflow, YGStyle::overflowBitfieldRef);
    }

    public static void YGNodeStyleSetDisplay(@NotNull YGNode node, final YGDisplay display) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, YGDisplay.class, display, YGStyle::displayBitfieldRef);
    }

    public static void YGNodeStyleSetFlex(@NotNull YGNode node, final float flex) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, flex, (ygStyle, val) -> val != ygStyle.flex().unwrap(), (ygStyle, val) -> ygStyle.setFlex(new YGFloatOptional(val)));
    }

    public static void YGNodeStyleSetFlexGrow(@NotNull YGNode node, final float flexGrow) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, flexGrow, (ygStyle, val) -> val != ygStyle.flexGrow().unwrap(), (ygStyle, val) -> ygStyle.setFlexGrow(new YGFloatOptional(val)));

    }

    public static void YGNodeStyleSetFlexShrink(@NotNull YGNode node, final float flexShrink) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, flexShrink, (ygStyle, val) -> val != ygStyle.flexShrink().unwrap(), (ygStyle, val) -> ygStyle.setFlexShrink(new YGFloatOptional(val)));
    }

    public static void YGNodeStyleSetFlexBasis(@NotNull YGNode node, final float flexBasis) //Method definition originates from: Yoga.cpp
    {
        @NotNull var value = CompactValue.ofMaybe(flexBasis, YGUnit.YGUnitPoint);
        updateStyle(node, value, (ygStyle, val) -> !CompactValue.equalsTo(ygStyle.flexBasis(), val), (ygStyle, val) -> ygStyle.setFlexBasis(value));

    }

    public static void YGNodeStyleSetFlexBasisPercent(@NotNull YGNode node, final float flexBasisPercent) //Method definition originates from: Yoga.cpp
    {
        @NotNull var value = CompactValue.ofMaybe(flexBasisPercent, YGUnit.YGUnitPercent);
        updateStyle(node, value, (ygStyle, val) -> !CompactValue.equalsTo(ygStyle.flexBasis(), val), (ygStyle, val) -> ygStyle.setFlexBasis(value));
    }

    public static void YGNodeStyleSetFlexBasisAuto(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        @NotNull var value = CompactValue.ofAuto();
        updateStyle(node, value, (ygStyle, val) -> !CompactValue.equalsTo(ygStyle.flexBasis(), val), (ygStyle, val) -> ygStyle.setFlexBasis(value));
    }

    public static void YGNodeStyleSetPosition(@NotNull YGNode node, @NotNull YGEdge edge, float points) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, points, YGUnit.YGUnitPoint, YGStyle::position);
    }

    public static void YGNodeStyleSetPositionPercent(@NotNull YGNode node, @NotNull YGEdge edge, float percent) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, percent, YGUnit.YGUnitPercent, YGStyle::position);
    }

    public static YGValue YGNodeStyleGetPosition(@NotNull YGNode node, @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        return node.getStyle().position().get(edge.getValue());
    }

    public static void YGNodeStyleSetMargin(@NotNull YGNode node, @NotNull YGEdge edge, float points) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, points, YGUnit.YGUnitPoint, YGStyle::margin);
    }

    private static <T extends Enum<T>> void updateStyleIndexed(@NotNull YGNode node, @NotNull T edge, float value, YGUnit unit, @NotNull Function<YGStyle, Values<T>> values) {
        updateStyleIndexed(node, edge, CompactValue.ofMaybe(value, unit), values);
    }

    private static <T extends Enum<T>> void updateStyleIndexed(@NotNull YGNode node, @NotNull T edge, CompactValue value, @NotNull Function<YGStyle, Values<T>> values) {
        updateStyle(node, value,
                (ygStyle, val) -> !CompactValue.equalsTo(values.apply(node.getStyle()).getCompactValue(edge.ordinal()),
                        val), (ygStyle, val) -> values.apply(node.getStyle()).set(edge.ordinal(), value));
    }

    public static void YGNodeStyleSetMarginPercent(@NotNull YGNode node, @NotNull YGEdge edge, float percent) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, percent, YGUnit.YGUnitPercent, YGStyle::margin);
    }

    public static void YGNodeStyleSetMarginAuto(@NotNull YGNode node, @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, CompactValue.ofAuto(), YGStyle::margin);
    }

    public static YGValue YGNodeStyleGetMargin(@NotNull YGNode node, @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        return node.getStyle().margin().get(edge.getValue());
    }

    public static void YGNodeStyleSetPadding(@NotNull YGNode node, @NotNull YGEdge edge, float points) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, points, YGUnit.YGUnitPoint, YGStyle::padding);
    }

    public static void YGNodeStyleSetPaddingPercent(@NotNull YGNode node, @NotNull YGEdge edge, float percent) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, percent, YGUnit.YGUnitPercent, YGStyle::padding);
    }

    public static YGValue YGNodeStyleGetPadding(@NotNull YGNode node, @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        return node.getStyle().padding().get(edge.getValue());
    }

    public static void YGNodeStyleSetBorder(@NotNull YGNode node, final @NotNull YGEdge edge, final float border) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, edge, border, YGUnit.YGUnitPoint, YGStyle::border);
    }

    public static void YGNodeStyleSetWidth(@NotNull YGNode node, float points) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, points, YGUnit.YGUnitPoint, YGStyle::dimensions);
    }

    public static void YGNodeStyleSetWidthPercent(@NotNull YGNode node, float percent) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, percent, YGUnit.YGUnitPercent, YGStyle::dimensions);
    }

    public static void YGNodeStyleSetWidthAuto(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, CompactValue.ofAuto(), YGStyle::dimensions);
    }

    public static YGValue YGNodeStyleGetWidth(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getStyle().dimensions().get(YGDimensionWidth.getValue());
    }

    public static void YGNodeStyleSetHeight(@NotNull YGNode node, float points) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, points, YGUnit.YGUnitPoint, YGStyle::dimensions);
    }

    public static void YGNodeStyleSetHeightPercent(@NotNull YGNode node, float percent) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, percent, YGUnit.YGUnitPercent, YGStyle::dimensions);
    }

    public static void YGNodeStyleSetHeightAuto(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, CompactValue.ofAuto(), YGStyle::dimensions);
    }

    public static YGValue YGNodeStyleGetHeight(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getStyle().dimensions().get(YGDimensionHeight.getValue());
    }

    public static void YGNodeStyleSetMinWidth(@NotNull YGNode node, final float minWidth) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, minWidth, YGUnit.YGUnitPoint, YGStyle::minDimensions);
    }

    public static void YGNodeStyleSetMinWidthPercent(@NotNull YGNode node, final float minWidth) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, minWidth, YGUnit.YGUnitPercent, YGStyle::minDimensions);
    }

    public static void YGNodeStyleSetMinHeight(@NotNull YGNode node, final float minHeight) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, minHeight, YGUnit.YGUnitPoint, YGStyle::minDimensions);
    }

    public static void YGNodeStyleSetMinHeightPercent(@NotNull YGNode node, final float minHeight) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, minHeight, YGUnit.YGUnitPercent,
                YGStyle::minDimensions);
    }

    public static void YGNodeStyleSetMaxWidth(@NotNull YGNode node, final float maxWidth) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, maxWidth, YGUnit.YGUnitPoint, YGStyle::maxDimensions);
    }

    public static void YGNodeStyleSetMaxWidthPercent(@NotNull YGNode node, final float maxWidth) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionWidth, maxWidth, YGUnit.YGUnitPercent, YGStyle::maxDimensions);
    }

    public static void YGNodeStyleSetMaxHeight(@NotNull YGNode node, final float maxHeight) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, maxHeight, YGUnit.YGUnitPoint, YGStyle::maxDimensions);
    }

    public static void YGNodeStyleSetMaxHeightPercent(@NotNull YGNode node, final float maxHeight) //Method definition originates from: Yoga.cpp
    {
        updateStyleIndexed(node, YGDimensionHeight, maxHeight, YGUnit.YGUnitPercent,
                YGStyle::maxDimensions);
    }

    // YGValue YGNodeStyleGetMaxHeight(YGNode node);
    public static void YGNodeStyleSetAspectRatio(@NotNull YGNode node, final float aspectRatio) //Method definition originates from: Yoga.cpp
    {
        updateStyle(node, aspectRatio, (ygStyle, val) -> val != ygStyle.aspectRatio().unwrap(), (ygStyle, val) -> ygStyle.setAspectRatio(new YGFloatOptional(val)));
    }

    public static float YGNodeLayoutGetLeft(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().position.get(YGEdgeLeft.getValue());
    }

    public static float YGNodeLayoutGetTop(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().position.get(YGEdgeTop.getValue());
    }

    public static float YGNodeLayoutGetRight(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().position.get(YGEdgeRight.getValue());
    }

    public static float YGNodeLayoutGetBottom(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().position.get(YGEdgeBottom.getValue());
    }

    public static float YGNodeLayoutGetWidth(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().dimensions.get(YGDimensionWidth.getValue());
    }

    public static float YGNodeLayoutGetHeight(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().dimensions.get(YGDimensionHeight.getValue());
    }

    public static YGDirection YGNodeLayoutGetDirection(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().direction();
    }

    public static boolean YGNodeLayoutGetHadOverflow(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().hadOverflow();
    }

    public static boolean YGNodeLayoutGetDidLegacyStretchFlagAffectLayout(@NotNull YGNode node) //Method definition originates from: Yoga.cpp
    {
        return node.getLayout().doesLegacyStretchFlagAffectsLayout();
    }

    public static float YGNodeLayoutGetMargin(@NotNull YGNode node, final @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithNode(node, edge.getValue() <= YGEdgeEnd.getValue(),
                "Cannot get layout properties of multi-edge shorthands");
        if (edge == YGEdgeStart) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().margin.get(YGEdgeRight.getValue());
            } else {
                return node.getLayout().margin.get(YGEdgeLeft.getValue());
            }
        }
        if (edge == YGEdgeEnd) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().margin.get(YGEdgeLeft.getValue());
            } else {
                return node.getLayout().margin.get(YGEdgeRight.getValue());
            }
        }
        return node.getLayout().margin.get(edge.getValue());
    }

    public static float YGNodeLayoutGetBorder(@NotNull YGNode node, final @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithNode(node, edge.getValue() <= YGEdgeEnd.getValue(),
                "Cannot get layout properties of multi-edge shorthands");
        if (edge == YGEdgeStart) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().border.get(YGEdgeRight.getValue());
            } else {
                return node.getLayout().border.get(YGEdgeLeft.getValue());
            }
        }
        if (edge == YGEdgeEnd) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().border.get(YGEdgeLeft.getValue());
            } else {
                return node.getLayout().border.get(YGEdgeRight.getValue());
            }
        }
        return node.getLayout().border.get(edge.getValue());
    }

    public static float YGNodeLayoutGetPadding(@NotNull YGNode node, final @NotNull YGEdge edge) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithNode(node, edge.getValue() <= YGEdgeEnd.getValue(),
                "Cannot get layout properties of multi-edge shorthands");
        if (edge == YGEdgeStart) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().padding.get(YGEdgeRight.getValue());
            } else {
                return node.getLayout().padding.get(YGEdgeLeft.getValue());
            }
        }
        if (edge == YGEdgeEnd) {
            if (node.getLayout().direction() == YGDirectionRTL) {
                return node.getLayout().padding.get(YGEdgeLeft.getValue());
            } else {
                return node.getLayout().padding.get(YGEdgeRight.getValue());
            }
        }
        return node.getLayout().padding.get(edge.getValue());
    }

    public static void YGConfigSetLogger(@NotNull YGConfig config, @Nullable YGLogger logger) //Method definition originates from: Yoga.cpp
    {
        config.setLogger(Objects.requireNonNullElseGet(logger, () -> GlobalMembers::YGDefaultLog));
    }

    @Contract("false, _ -> fail")
    public static void YGAssert(final boolean condition, String message) //Method definition originates from: Yoga.cpp
    {
        if (!condition) {
            Log.log((YGNode) null, YGLogLevelFatal, null, "%s\n", message);
            throw new RuntimeException(message);
        }
    }

    @Contract("_, false, _ -> fail")
    public static void YGAssertWithNode(YGNode node, final boolean condition, String message) //Method definition originates from: Yoga.cpp
    {
        if (!condition) {
            Log.log(node, YGLogLevelFatal, null, "%s\n", message);
            throw new RuntimeException(message);
        }
    }

    @Contract("_, false, _ -> fail")
    public static void YGAssertWithConfig(YGConfig config, final boolean condition, String message) //Method definition originates from: Yoga.cpp
    {
        if (!condition) {
            Log.log(config, YGLogLevelFatal, null, "%s\n", message);
            throw new RuntimeException(message);
        }
    }

    public static void YGConfigSetPointScaleFactor(@NotNull YGConfig config, final float pixelsInPoint) //Method definition originates from: Yoga.cpp
    {
        YGAssertWithConfig(config, pixelsInPoint >= 0.0f, "Scale factor should not be less than zero");

        if (pixelsInPoint == 0.0f) {

            config.pointScaleFactor = 0.0f;
        } else {
            config.pointScaleFactor = pixelsInPoint;
        }
    }

    public static void YGConfigSetShouldDiffLayoutWithoutLegacyStretchBehaviour(@NotNull YGConfig config, final boolean shouldDiffLayout) //Method definition originates from: Yoga.cpp
    {
        config.shouldDiffLayoutWithoutLegacyStretchBehaviour = shouldDiffLayout;
    }

    public static void YGConfigSetUseLegacyStretchBehaviour(@NotNull YGConfig config, final boolean useLegacyStretchBehaviour) //Method definition originates from: Yoga.cpp
    {
        config.useLegacyStretchBehaviour = useLegacyStretchBehaviour;
    }

    public static @NotNull YGConfig YGConfigNew() //Method definition originates from: Yoga.cpp
    {


        @NotNull YGConfig config = new YGConfig(GlobalMembers::YGDefaultLog);

        gConfigInstanceCount++;
        return config;
    }

    public static void YGConfigFree(YGConfig config) //Method definition originates from: Yoga.cpp
    {
        gConfigInstanceCount--;
    }

    public static void YGConfigCopy(YGConfig dest, YGConfig src) //Method definition originates from: Yoga.cpp
    {
        //memcpy(dest, src, sizeof(YGConfig));
        //TODO: Implement copy
        throw new UnsupportedOperationException("Not implemented");

    }

    public static int YGConfigGetInstanceCount() //Method definition originates from: Yoga.cpp
    {
        return gConfigInstanceCount;
    }

    public static void YGConfigSetExperimentalFeatureEnabled(@NotNull YGConfig config, final @NotNull YGExperimentalFeature feature, final boolean enabled) //Method definition originates from: Yoga.cpp
    {
        config.experimentalFeatures.set(feature.getValue(), enabled);
    }

    public static boolean YGConfigIsExperimentalFeatureEnabled(@NotNull YGConfig config, final @NotNull YGExperimentalFeature feature) //Method definition originates from: Yoga.cpp
    {
        return config.experimentalFeatures.get(feature.getValue());
    }

    public static void YGConfigSetUseWebDefaults(@NotNull YGConfig config, final boolean enabled) //Method definition originates from: Yoga.cpp
    {
        config.useWebDefaults = enabled;
    }

    public static boolean YGConfigGetUseWebDefaults(@NotNull YGConfig config) //Method definition originates from: Yoga.cpp
    {
        return config.useWebDefaults;
    }

    public static void YGConfigSetCloneNodeFunc(@NotNull YGConfig config, final YGCloneNodeFunc callback) //Method definition originates from: Yoga.cpp
    {
        config.setCloneNodeCallback(callback);
    }

    public static @NotNull YGConfig YGConfigGetDefault() //Method definition originates from: Yoga.cpp
    {
        //   static struct YGConfig* defaultConfig = YGConfigNew();
        return defaultConfig;
    }

    public static void YGConfigSetContext(@NotNull YGConfig config, Object context) //Method definition originates from: Yoga.cpp
    {
        config.context = context;
    }

    public static @Nullable Object YGConfigGetContext(@NotNull YGConfig config) //Method definition originates from: Yoga.cpp
    {
        return config.context;
    }

    public static float YGRoundValueToPixelGrid(final double value, final double pointScaleFactor, final boolean forceCeil, final boolean forceFloor) //Method definition originates from: Yoga.cpp
    {
        double scaledValue = value * pointScaleFactor;


        double fractial = scaledValue % 1.0;
        if (fractial < 0) {
            ++fractial;
        }
        if (YGDoubleEqual(fractial, 0)) {

            scaledValue = scaledValue - fractial;
        } else if (YGDoubleEqual(fractial, 1.0)) {
            scaledValue = scaledValue - fractial + 1.0;
        } else if (forceCeil) {

            scaledValue = scaledValue - fractial + 1.0;
        } else if (forceFloor) {
            scaledValue = scaledValue - fractial;
        } else {

            scaledValue = scaledValue - fractial + (!YGDoubleIsUndefined(fractial) && (fractial > 0.5 || YGDoubleEqual(
                    fractial, 0.5)) ? 1.0 : 0.0);
        }
        return (YGDoubleIsUndefined(scaledValue) || YGDoubleIsUndefined(
                pointScaleFactor)) ? YGUndefined : (float) (scaledValue / pointScaleFactor);
    }

    public static int YGDefaultLog(YGConfig ygConfig, YGNode ygNode, @NotNull YGLogLevel ygLogLevel, String s, Object... objects) //Method definition originates from: Yoga.cpp
    {
        switch (ygLogLevel) {
            case YGLogLevelError:
            case YGLogLevelFatal:
                System.err.printf((s) + "%n", objects);
            case YGLogLevelWarn:
            case YGLogLevelInfo:
            case YGLogLevelDebug:
            case YGLogLevelVerbose:
            default:
                System.out.printf((s) + "%n", objects);
        }
        return 0;
    }

    public static boolean YGDoubleIsUndefined(final double value) {
        return isUndefined(value);
    }

    public static @NotNull YGConfig YGConfigClone(final @NotNull YGConfig oldConfig) {
        @NotNull YGConfig config = oldConfig.clone();
        YGAssert(config != null, "Could not allocate memory for config");
        gConfigInstanceCount++;
        return config;
    }

    public static @NotNull YGNode YGNodeDeepClone(@NotNull YGNode oldNode) {
        @NotNull YGConfig config = null;
        if (oldNode.getConfig() != null) {
            config = YGConfigClone(oldNode.getConfig());
        }
        @NotNull var node = new YGNode(oldNode, config);
        node.setOwner(null);
        Event.publish(node, new NodeAllocationEventData(node.getConfig()));

        @NotNull ArrayList<YGNode> vec = new ArrayList<>();
        vec.ensureCapacity(oldNode.getChildren().size());
        @Nullable YGNode childNode;
        for (@NotNull var item : oldNode.getChildren()) {
            childNode = YGNodeDeepClone(item);
            childNode.setOwner(node);
            vec.add(childNode);
        }
        node.setChildren(vec);

        return new YGNode(node);
    }

    public static void YGConfigFreeRecursive(@NotNull YGNode root) {
        if (root.getConfig() != null) {
            gConfigInstanceCount--;
            root.setConfig(null);
        }

        for (@NotNull var child : root.getChildren()) {
            YGConfigFreeRecursive(child);
        }
    }

    public static void YGNodeSetChildrenInternal(final @Nullable YGNode owner, final @NotNull ArrayList<YGNode> children) {
        if (owner == null) {
            return;
        }
        if (children.size() == 0) {
            if (YGNodeGetChildCount(owner) > 0) {
                for (@NotNull YGNode child : owner.getChildren()) {
                    child.setLayout(null);
                    child.setOwner(null);
                }
                owner.getChildren().clear();
                owner.markDirtyAndPropogate();
            }
        } else {
            if (YGNodeGetChildCount(owner) > 0) {
                for (@NotNull YGNode oldChild : owner.getChildren()) {

//TODO: What is this??
                    //if (std::find (children.iterator(), children.end(), oldChild) ==children.end())
                    //{
                    //oldChild.setLayout(YGLayout());
                    oldChild.setLayout(null);
                    oldChild.setOwner(null);
                    //}
                }
            }
            owner.setChildren(children);
            for (@NotNull YGNode child : children) {
                child.setOwner(owner);
            }
            owner.markDirtyAndPropogate();
        }
    }

    public static void YGNodeSetChildren(final YGNode owner, final @NotNull ArrayList<YGNode> children) {
        YGNodeSetChildrenInternal(owner, children);
    }

    public static float YGNodeStyleGetFlexGrow(@NotNull YGNode node) {
        return node.getStyle().flexGrow().isUndefined() ? kDefaultFlexGrow : node.getStyle().flexGrow().unwrap();
    }

    public static float YGNodeStyleGetFlexShrink(@NotNull YGNode node) {
        return node.getStyle().flexShrink()
                .isUndefined() ? ((node.getConfig() != null && node.getConfig().useWebDefaults) ? kWebDefaultFlexShrink : kDefaultFlexShrink) : node.getStyle()
                .flexShrink().unwrap();
    }

    public static <T extends Enum<T>> void updateStyle(@NotNull YGNode node, @NotNull Class<T> enumClazz, T value, @NotNull Function<YGStyle, YGStyle.BitfieldRef<T>> fieldRef) {
        updateStyle(node, value, (ygStyle, newVal) -> fieldRef.apply(node.getStyle()).getValue(enumClazz) != newVal,
                (ygStyle, newVal) -> fieldRef.apply(node.getStyle()).setValue(newVal));
    }

    public static <T> void updateStyle(@NotNull YGNode node, T value, @NotNull BiFunction<YGStyle, T, Boolean> needsUpdate, @NotNull BiConsumer<YGStyle, T> update) {
        if (needsUpdate.apply(node.getStyle(), value)) {
            update.accept(node.getStyle(), value);
            node.markDirtyAndPropogate();
        }
    }

    public static YGDirection YGNodeStyleGetDirection(@NotNull YGNode node) {
        return node.getStyle().direction();
    }

    public static YGFlexDirection YGNodeStyleGetFlexDirection(@NotNull YGNode node) {
        return node.getStyle().flexDirection();
    }

    public static YGJustify YGNodeStyleGetJustifyContent(@NotNull YGNode node) {
        return node.getStyle().justifyContent();
    }

    public static YGAlign YGNodeStyleGetAlignContent(@NotNull YGNode node) {
        return node.getStyle().alignContent();
    }

    public static YGAlign YGNodeStyleGetAlignItems(@NotNull YGNode node) {
        return node.getStyle().alignItems();
    }

    public static YGAlign YGNodeStyleGetAlignSelf(@NotNull YGNode node) {
        return node.getStyle().alignSelf();
    }

    public static YGPositionType YGNodeStyleGetPositionType(@NotNull YGNode node) {
        return node.getStyle().positionType();
    }

    public static YGWrap YGNodeStyleGetFlexWrap(@NotNull YGNode node) {
        return node.getStyle().flexWrap();
    }

    public static YGOverflow YGNodeStyleGetOverflow(@NotNull YGNode node) {
        return node.getStyle().overflow();
    }

    public static YGDisplay YGNodeStyleGetDisplay(@NotNull YGNode node) {
        return node.getStyle().display();
    }

    public static float YGNodeStyleGetFlex(@NotNull YGNode node) {
        return node.getStyle().flex().isUndefined() ? YGUndefined : node.getStyle().flex().unwrap();
    }

    public static YGValue YGNodeStyleGetFlexBasis(@NotNull YGNode node) {
        YGValue flexBasis = node.getStyle().flexBasis().convertToYgValue();
        if (flexBasis.unit == YGUnit.YGUnitUndefined || flexBasis.unit == YGUnitAuto) {

            flexBasis.value = YGUndefined;
        }
        return flexBasis;
    }

    public static float YGNodeStyleGetBorder(@NotNull YGNode node, final @NotNull YGEdge edge) {
        @NotNull var border = node.getStyle().border().getCompactValue(edge.getValue());
        if (border.isUndefined() || border.isAuto()) {


            return YGUndefined;
        }

        return border.convertToYgValue().value;
    }

    public static float YGNodeStyleGetAspectRatio(@NotNull YGNode node) {
        final YGFloatOptional op = node.getStyle().aspectRatio();
        return op.isUndefined() ? YGUndefined : op.unwrap();
    }

    public static YGValue YGNodeStyleGetMinWidth(@NotNull YGNode node) {
        return node.getStyle().minDimensions().get(YGDimensionWidth.getValue());
    }

    public static YGValue YGNodeStyleGetMinHeight(@NotNull YGNode node) {
        return node.getStyle().minDimensions().get(YGDimensionHeight.getValue());
    }

    public static YGValue YGNodeStyleGetMaxWidth(@NotNull YGNode node) {
        return node.getStyle().maxDimensions().get(YGDimensionWidth.getValue());
    }

    public static YGValue YGNodeStyleGetMaxHeight(@NotNull YGNode node) {
        return node.getStyle().maxDimensions().get(YGDimensionHeight.getValue());
    }

    public static boolean YGLayoutNodeInternal(@NotNull YGNode node, final float availableWidth, final float availableHeight, final @NotNull YGDirection ownerDirection, final @NotNull YGMeasureMode widthMeasureMode, final @NotNull YGMeasureMode heightMeasureMode, final float ownerWidth, final float ownerHeight, final boolean performLayout, final @NotNull LayoutPassReason reason, YGConfig config, @NotNull LayoutData layoutMarkerData, final Object layoutContext, Integer depth, final Integer generationCount) //Method definition originates from: Yoga.cpp
    {
        YGLayout layout = node.getLayout();

        depth++;

        final boolean needToVisitNode = (node.isDirty() && layout.generationCount != generationCount) || layout.lastOwnerDirection != ownerDirection;

        if (needToVisitNode) {

            layout.nextCachedMeasurementsIndex = 0;
            layout.cachedLayout.availableWidth = -1;
            layout.cachedLayout.availableHeight = -1;
            layout.cachedLayout.widthMeasureMode = YGMeasureModeUndefined;
            layout.cachedLayout.heightMeasureMode = YGMeasureModeUndefined;
            layout.cachedLayout.computedWidth = -1;
            layout.cachedLayout.computedHeight = -1;
        }

        @Nullable YGCachedMeasurement cachedResults = null;
        if (node.hasMeasureFunc()) {
            final float marginAxisRow = node.getMarginForAxis(YGFlexDirectionRow, ownerWidth).unwrap();
            final float marginAxisColumn = node.getMarginForAxis(YGFlexDirectionColumn, ownerWidth)
                    .unwrap();


            if (YGNodeCanUseCachedMeasurement(widthMeasureMode, availableWidth, heightMeasureMode, availableHeight,
                    layout.cachedLayout.widthMeasureMode, layout.cachedLayout.availableWidth,
                    layout.cachedLayout.heightMeasureMode, layout.cachedLayout.availableHeight,
                    layout.cachedLayout.computedWidth, layout.cachedLayout.computedHeight, marginAxisRow,
                    marginAxisColumn, config)) {
                cachedResults = layout.cachedLayout;
            } else {

                for (int i = 0; i < layout.nextCachedMeasurementsIndex; i++) {
                    if (YGNodeCanUseCachedMeasurement(widthMeasureMode, availableWidth, heightMeasureMode,
                            availableHeight, layout.cachedMeasurements.get(i).widthMeasureMode,
                            layout.cachedMeasurements.get(i).availableWidth,
                            layout.cachedMeasurements.get(i).heightMeasureMode,
                            layout.cachedMeasurements.get(i).availableHeight,
                            layout.cachedMeasurements.get(i).computedWidth,
                            layout.cachedMeasurements.get(i).computedHeight, marginAxisRow, marginAxisColumn, config)) {
                        cachedResults = layout.cachedMeasurements.get(i);
                        break;
                    }
                }
            }
        } else if (performLayout) {
            if (YGFloatsEqual(layout.cachedLayout.availableWidth, availableWidth) && YGFloatsEqual(
                    layout.cachedLayout.availableHeight,
                    availableHeight) && layout.cachedLayout.widthMeasureMode == widthMeasureMode && layout.cachedLayout.heightMeasureMode == heightMeasureMode) {
                cachedResults = layout.cachedLayout;
            }
        } else {
            for (int i = 0; i < layout.nextCachedMeasurementsIndex; i++) {
                if (YGFloatsEqual(layout.cachedMeasurements.get(i).availableWidth, availableWidth) && YGFloatsEqual(
                        layout.cachedMeasurements.get(i).availableHeight,
                        availableHeight) && layout.cachedMeasurements.get(
                        i).widthMeasureMode == widthMeasureMode && layout.cachedMeasurements.get(
                        i).heightMeasureMode == heightMeasureMode) {
                    cachedResults = layout.cachedMeasurements.get(i);
                    break;
                }
            }
        }

        if (!needToVisitNode && cachedResults != null) {
            layout.measuredDimensions.set(YGDimensionWidth.getValue(), cachedResults.computedWidth);
            layout.measuredDimensions.set(YGDimensionHeight.getValue(), cachedResults.computedHeight);

            if (performLayout) layoutMarkerData.cachedLayouts += 1;
            else layoutMarkerData.cachedMeasures += 1;

            if (gPrintChanges && gPrintSkips) {
                Log.log(node, YGLogLevelVerbose, null, "%s%d.{[skipped] ", YGSpacer(depth), depth);
                node.print(layoutContext);
                Log.log(node, YGLogLevelVerbose, null, "wm: %s, hm: %s, aw: %f ah: %f => d: (%f, %f) %s\n",
                        YGMeasureModeName(widthMeasureMode, performLayout),
                        YGMeasureModeName(heightMeasureMode, performLayout), availableWidth, availableHeight,
                        cachedResults.computedWidth, cachedResults.computedHeight, LayoutPassReasonToString(reason));
            }
        } else {
            if (gPrintChanges) {
                Log.log(node, YGLogLevelVerbose, null, "%s%d.{%s", YGSpacer(depth), depth, needToVisitNode ? "*" : "");
                node.print(layoutContext);
                Log.log(node, YGLogLevelVerbose, null, "wm: %s, hm: %s, aw: %f ah: %f %s\n",
                        YGMeasureModeName(widthMeasureMode, performLayout),
                        YGMeasureModeName(heightMeasureMode, performLayout), availableWidth, availableHeight,
                        LayoutPassReasonToString(reason));
            }

            YGNodelayoutImpl(node, availableWidth, availableHeight, ownerDirection, widthMeasureMode, heightMeasureMode,
                    ownerWidth, ownerHeight, performLayout, config, layoutMarkerData, layoutContext, depth,
                    generationCount, reason);

            if (gPrintChanges) {
                Log.log(node, YGLogLevelVerbose, null, "%s%d.}%s", YGSpacer(depth), depth, needToVisitNode ? "*" : "");
                node.print(layoutContext);
                Log.log(node, YGLogLevelVerbose, null, "wm: %s, hm: %s, d: (%f, %f) %s\n",
                        YGMeasureModeName(widthMeasureMode, performLayout),
                        YGMeasureModeName(heightMeasureMode, performLayout),
                        layout.measuredDimensions.get(YGDimensionWidth.getValue()),
                        layout.measuredDimensions.get(YGDimensionHeight.getValue()),
                        LayoutPassReasonToString(reason));
            }

            layout.lastOwnerDirection = ownerDirection;

            if (cachedResults == null) {
                if (layout.nextCachedMeasurementsIndex + 1 > layoutMarkerData.maxMeasureCache) {
                    layoutMarkerData.maxMeasureCache = layout.nextCachedMeasurementsIndex + 1;
                }
                if (layout.nextCachedMeasurementsIndex == YG_MAX_CACHED_RESULT_COUNT) {
                    if (gPrintChanges) {
                        Log.log(node, YGLogLevelVerbose, null, "Out of cache entries!\n");
                    }
                    layout.nextCachedMeasurementsIndex = 0;
                }

                YGCachedMeasurement newCacheEntry;
                if (performLayout) {

                    newCacheEntry = layout.cachedLayout;
                } else {

                    newCacheEntry = layout.cachedMeasurements.get(layout.nextCachedMeasurementsIndex);
                    layout.nextCachedMeasurementsIndex++;
                }

                newCacheEntry.availableWidth = availableWidth;
                newCacheEntry.availableHeight = availableHeight;
                newCacheEntry.widthMeasureMode = widthMeasureMode;
                newCacheEntry.heightMeasureMode = heightMeasureMode;
                newCacheEntry.computedWidth = layout.measuredDimensions.get(YGDimensionWidth.getValue());
                newCacheEntry.computedHeight = layout.measuredDimensions.get(YGDimensionHeight.getValue());
            }
        }

        if (performLayout) {
            node.setLayoutDimension(node.getLayout().measuredDimensions.get(YGDimensionWidth.getValue()),
                    YGDimensionWidth.getValue());
            node.setLayoutDimension(node.getLayout().measuredDimensions.get(YGDimensionHeight.getValue()),
                    YGDimensionHeight.getValue());

            node.setHasNewLayout(true);
            node.setDirty(false);
        }

        layout.generationCount = generationCount;

        LayoutType layoutType;
        if (performLayout) {
            layoutType = !needToVisitNode && cachedResults == layout.cachedLayout ? LayoutType.kCachedLayout : LayoutType.kLayout;
        } else {
            layoutType = cachedResults != null ? LayoutType.kCachedMeasure : LayoutType.kMeasure;
        }
        /* Event.NodeLayout */
        Event.publish(node, new NodeLayoutEventData(layoutType, layoutContext));

        return (needToVisitNode || cachedResults == null);
    }

    public static float YGNodePaddingAndBorderForAxis(@NotNull YGNode node, final @NotNull YGFlexDirection axis, final float widthSize) {
        return plus(node.getLeadingPaddingAndBorder(axis, widthSize),
                node.getTrailingPaddingAndBorder(axis, widthSize)).unwrap();
    }

    public static YGAlign YGNodeAlignItem(@NotNull YGNode node, @NotNull YGNode child) {
        final YGAlign align = child.getStyle().alignSelf() == YGAlign.YGAlignAuto ? node.getStyle()
                .alignItems() : child.getStyle().alignSelf();
        if (align == YGAlignBaseline && YGFlexDirectionIsColumn(node.getStyle().flexDirection())) {
            return YGAlignFlexStart;
        }
        return align;
    }

    public static float YGBaseline(@NotNull YGNode node, Object layoutContext) {
        if (node.hasBaselineFunc()) {

            /* Event.NodeBaselineStart */
            Event.publish(node);

            final float baseline = node.baseline(
                    node.getLayout().measuredDimensions.get(YGDimensionWidth.getValue()),
                    node.getLayout().measuredDimensions.get(YGDimensionHeight.getValue()), layoutContext);

            /* Event.NodeBaselineEnd */
            Event.publish(node);

            YGAssertWithNode(node, !YGFloatIsUndefined(baseline), "Expect custom baseline function to not return NaN");
            return baseline;
        }

        @Nullable YGNode baselineChild = null;
        final @NotNull Integer childCount = YGNodeGetChildCount(node);
        for (@NotNull Integer i = 0; i < childCount; i++) {
            @Nullable YGNode child = YGNodeGetChild(node, i);
            if (child != null && child.getLineIndex() > 0) {
                break;
            }
            if (child.getStyle().positionType() == YGPositionTypeAbsolute) {
                continue;
            }
            if (YGNodeAlignItem(node, child) == YGAlignBaseline || child.isReferenceBaseline()) {
                baselineChild = child;
                break;
            }

            if (baselineChild == null) {
                baselineChild = child;
            }
        }

        if (baselineChild == null) {
            return node.getLayout().measuredDimensions.get(YGDimensionHeight.getValue());
        }

        final float baseline = YGBaseline(baselineChild, layoutContext);
        return baseline + baselineChild.getLayout().position.get(YGEdgeTop.getValue());
    }

    public static boolean YGIsBaselineLayout(@NotNull YGNode node) {
        if (YGFlexDirectionIsColumn(node.getStyle().flexDirection())) {
            return false;
        }
        if (node.getStyle().alignItems() == YGAlignBaseline) {
            return true;
        }
        final @NotNull Integer childCount = YGNodeGetChildCount(node);
        for (@NotNull Integer i = 0; i < childCount; i++) {
            @Nullable YGNode child = YGNodeGetChild(node, i);
            if (child != null && child.getStyle()
                    .positionType() != YGPositionTypeAbsolute && child.getStyle()
                    .alignSelf() == YGAlignBaseline) {
                return true;
            }
        }

        return false;
    }

    public static float YGNodeDimWithMargin(@NotNull YGNode node, final @NotNull YGFlexDirection axis, final float widthSize) {
        return plus(new YGFloatOptional(node.getLayout().measuredDimensions.get(dim.get(axis.getValue()).getValue())),
                plus(node.getLeadingMargin(axis, widthSize), node.getTrailingMargin(axis, widthSize))).unwrap();
    }

    public static boolean YGNodeIsStyleDimDefined(@NotNull YGNode node, final @NotNull YGFlexDirection axis, final float ownerSize) {
        YGValue resolvedDimension = node.getResolvedDimension(dim.get(axis.getValue()).getValue());
        boolean isUndefined = YGFloatIsUndefined(resolvedDimension.value);
        boolean isAutoUnit = resolvedDimension.unit == YGUnitAuto;
        boolean isUndefinedUnit = resolvedDimension.unit == YGUnit.YGUnitUndefined;
        boolean isPointUnit = resolvedDimension.unit == YGUnit.YGUnitPoint;
        boolean isDefined = !isUndefined;
        boolean isResolvedValueLessThanZero = resolvedDimension.value < 0.0f;
        boolean isResolvedPointValueNegative = isPointUnit && isDefined && isResolvedValueLessThanZero;
        boolean isPercentUnit = resolvedDimension.unit == YGUnit.YGUnitPercent;
        boolean isOwnerSizeUndefined = YGFloatIsUndefined(ownerSize);
        return !(isAutoUnit || isUndefinedUnit || isResolvedPointValueNegative || (isPercentUnit && isDefined && (isResolvedValueLessThanZero || isOwnerSizeUndefined)));
    }

    public static boolean YGNodeIsLayoutDimDefined(@NotNull YGNode node, final @NotNull YGFlexDirection axis) {
        final float value = node.getLayout().measuredDimensions.get(dim.get(axis.getValue()).getValue());
        return !YGFloatIsUndefined(value) && value >= 0.0f;
    }

    public static @NotNull YGFloatOptional YGNodeBoundAxisWithinMinAndMax(@NotNull YGNode node, final YGFlexDirection axis, final @NotNull YGFloatOptional value, final float axisSize) {
        @NotNull YGFloatOptional min = new YGFloatOptional();
        @NotNull YGFloatOptional max = new YGFloatOptional();

        if (YGFlexDirectionIsColumn(axis)) {
            min = (YGResolveValue(node.getStyle().minDimensions().get(YGDimensionHeight.getValue()),
                    axisSize));
            max = (YGResolveValue(node.getStyle().maxDimensions().get(YGDimensionHeight.getValue()),
                    axisSize));
        } else if (YGFlexDirectionIsRow(axis)) {
            min = (YGResolveValue(node.getStyle().minDimensions().get(YGDimensionWidth.getValue()),
                    axisSize));
            max = (YGResolveValue(node.getStyle().maxDimensions().get(YGDimensionWidth.getValue()),
                    axisSize));
        }

        if (greaterThanOrEqualTo(max, new YGFloatOptional((0))) && (greaterThan(value, max))) {
            return max;
        }

        if (greaterThanOrEqualTo(min, new YGFloatOptional((0))) && lessThan(value, min)) {
            return min;
        }

        return value;
    }

    public static float YGNodeBoundAxis(@NotNull YGNode node, final @NotNull YGFlexDirection axis, final float value, final float axisSize, final float widthSize) {
        return YGFloatMax(YGNodeBoundAxisWithinMinAndMax(node, axis, new YGFloatOptional((value)), axisSize).unwrap(),
                YGNodePaddingAndBorderForAxis(node, axis, widthSize));
    }

    public static void YGNodeSetChildTrailingPosition(@NotNull YGNode node, @NotNull YGNode child, final @NotNull YGFlexDirection axis) {
        final float size = child.getLayout().measuredDimensions.get(dim.get(axis.getValue()).getValue());
        child.setLayoutPosition(node.getLayout().measuredDimensions.get(
                dim.get(axis.getValue()).getValue()) - size - child.getLayout().position.get(
                pos.get(axis.getValue()).getValue()), trailing.get(axis.getValue()).getValue());
    }

    public static void YGConstrainMaxSizeForMode(@NotNull YGNode node, final @NotNull YGFlexDirection axis, final float ownerAxisSize, final float ownerWidth, @NotNull YGMeasureMode mode, @NotNull RefObject<Float> size) {
        final @NotNull YGFloatOptional maxSize = plus(
                YGResolveValue(node.getStyle().maxDimensions().getCompactValue(dim.get(axis.getValue()).getValue()),
                        ownerAxisSize), node.getMarginForAxis(axis, ownerWidth));
        switch (mode) {
            case YGMeasureModeExactly:
            case YGMeasureModeAtMost:
                size.argValue = (maxSize.isUndefined() || size.argValue < maxSize.unwrap()) ? size.argValue : maxSize.unwrap();
                break;
            case YGMeasureModeUndefined:
                if (!maxSize.isUndefined()) {
                    //TODO: What is this???
                    size.argValue = maxSize.unwrap();
                }
                break;
        }
    }

    public static void YGNodeComputeFlexBasisForChild(@NotNull YGNode node, @NotNull YGNode child, final float width, final YGMeasureMode widthMode, final float height, final float ownerWidth, final float ownerHeight, final YGMeasureMode heightMode, final @NotNull YGDirection direction, YGConfig config, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final Integer depth, final Integer generationCount) {
        final YGFlexDirection mainAxis = YGResolveFlexDirection(node.getStyle().flexDirection(), direction);
        final boolean isMainAxisRow = YGFlexDirectionIsRow(mainAxis);
        final float mainAxisSize = isMainAxisRow ? width : height;
        final float mainAxisownerSize = isMainAxisRow ? ownerWidth : ownerHeight;

        float childWidth;
        float childHeight;
        YGMeasureMode childWidthMeasureMode;
        YGMeasureMode childHeightMeasureMode;

        final @NotNull YGFloatOptional resolvedFlexBasis = YGResolveValue(child.resolveFlexBasisPtr(),
                mainAxisownerSize);
        final boolean isRowStyleDimDefined = YGNodeIsStyleDimDefined(child, YGFlexDirectionRow,
                ownerWidth);
        final boolean isColumnStyleDimDefined = YGNodeIsStyleDimDefined(child, YGFlexDirectionColumn,
                ownerHeight);

        if (!resolvedFlexBasis.isUndefined() && !YGFloatIsUndefined(mainAxisSize)) {
            if (child.getConfig() != null && (child.getLayout().computedFlexBasis.isUndefined() || (YGConfigIsExperimentalFeatureEnabled(
                    child.getConfig(),
                    YGExperimentalFeature.YGExperimentalFeatureWebFlexBasis) && child.getLayout().computedFlexBasisGeneration != generationCount))) {
                final @NotNull YGFloatOptional paddingAndBorder = new YGFloatOptional(
                        YGNodePaddingAndBorderForAxis(child, mainAxis, ownerWidth));
                child.setLayoutComputedFlexBasis(YGFloatOptionalMax(resolvedFlexBasis, paddingAndBorder));
            }
        } else if (isMainAxisRow && isRowStyleDimDefined) {

            final @NotNull YGFloatOptional paddingAndBorder = new YGFloatOptional(
                    YGNodePaddingAndBorderForAxis(child, YGFlexDirectionRow, ownerWidth));

            child.setLayoutComputedFlexBasis(YGFloatOptionalMax(
                    YGResolveValue(child.getResolvedDimensions().get(YGDimensionWidth.getValue()),
                            ownerWidth), paddingAndBorder));
        } else if (!isMainAxisRow && isColumnStyleDimDefined) {

            final @NotNull YGFloatOptional paddingAndBorder = new YGFloatOptional(
                    YGNodePaddingAndBorderForAxis(child, YGFlexDirectionColumn, ownerWidth));
            child.setLayoutComputedFlexBasis(YGFloatOptionalMax(
                    YGResolveValue(child.getResolvedDimensions().get(YGDimensionHeight.getValue()),
                            ownerHeight), paddingAndBorder));
        } else {


            childWidth = YGUndefined;
            childHeight = YGUndefined;
            childWidthMeasureMode = YGMeasureModeUndefined;
            childHeightMeasureMode = YGMeasureModeUndefined;

            var marginRow = child.getMarginForAxis(YGFlexDirectionRow, ownerWidth).unwrap();
            var marginColumn = child.getMarginForAxis(YGFlexDirectionColumn, ownerWidth).unwrap();

            if (isRowStyleDimDefined) {
                childWidth = YGResolveValue(child.getResolvedDimensions().get(YGDimensionWidth.getValue()),
                        ownerWidth).unwrap() + marginRow;
                childWidthMeasureMode = YGMeasureModeExactly;
            }
            if (isColumnStyleDimDefined) {
                childHeight = YGResolveValue(
                        child.getResolvedDimensions().get(YGDimensionHeight.getValue()),
                        ownerHeight).unwrap() + marginColumn;
                childHeightMeasureMode = YGMeasureModeExactly;
            }


            if ((!isMainAxisRow && node.getStyle().overflow() == YGOverflowScroll) || node.getStyle()
                    .overflow() != YGOverflowScroll) {
                if (YGFloatIsUndefined(childWidth) && !YGFloatIsUndefined(width)) {
                    childWidth = width;
                    childWidthMeasureMode = YGMeasureModeAtMost;
                }
            }

            if ((isMainAxisRow && node.getStyle().overflow() == YGOverflowScroll) || node.getStyle()
                    .overflow() != YGOverflowScroll) {
                if (YGFloatIsUndefined(childHeight) && !YGFloatIsUndefined(height)) {
                    childHeight = height;
                    childHeightMeasureMode = YGMeasureModeAtMost;
                }
            }

            final var childStyle = child.getStyle();
            if (!childStyle.aspectRatio().isUndefined()) {
                if (!isMainAxisRow && childWidthMeasureMode == YGMeasureModeExactly) {
                    childHeight = marginColumn + (childWidth - marginRow) / childStyle.aspectRatio().unwrap();
                    childHeightMeasureMode = YGMeasureModeExactly;
                } else if (isMainAxisRow && childHeightMeasureMode == YGMeasureModeExactly) {
                    childWidth = marginRow + (childHeight - marginColumn) * childStyle.aspectRatio().unwrap();
                    childWidthMeasureMode = YGMeasureModeExactly;
                }
            }


            final boolean hasExactWidth = !YGFloatIsUndefined(width) && widthMode == YGMeasureModeExactly;
            final boolean childWidthStretch = YGNodeAlignItem(node,
                    child) == YGAlignStretch && childWidthMeasureMode != YGMeasureModeExactly;
            if (!isMainAxisRow && !isRowStyleDimDefined && hasExactWidth && childWidthStretch) {
                childWidth = width;
                childWidthMeasureMode = YGMeasureModeExactly;
                if (!childStyle.aspectRatio().isUndefined()) {
                    childHeight = (childWidth - marginRow) / childStyle.aspectRatio().unwrap();
                    childHeightMeasureMode = YGMeasureModeExactly;
                }
            }

            final boolean hasExactHeight = !YGFloatIsUndefined(
                    height) && heightMode == YGMeasureModeExactly;
            final boolean childHeightStretch = YGNodeAlignItem(node,
                    child) == YGAlignStretch && childHeightMeasureMode != YGMeasureModeExactly;
            if (isMainAxisRow && !isColumnStyleDimDefined && hasExactHeight && childHeightStretch) {
                childHeight = height;
                childHeightMeasureMode = YGMeasureModeExactly;

                if (!childStyle.aspectRatio().isUndefined()) {
                    childWidth = (childHeight - marginColumn) * childStyle.aspectRatio().unwrap();
                    childWidthMeasureMode = YGMeasureModeExactly;
                }
            }


            @NotNull RefObject<Float> childWidthRef = new RefObject<>(childWidth);
            YGConstrainMaxSizeForMode(child, YGFlexDirectionRow, ownerWidth, ownerWidth,
                    childWidthMeasureMode, childWidthRef);
            YGConstrainMaxSizeForMode(child, YGFlexDirectionColumn, ownerHeight, ownerWidth,
                    childHeightMeasureMode, childWidthRef);
            childWidth = childWidthRef.argValue;


            YGLayoutNodeInternal(child, childWidth, childHeight, direction, childWidthMeasureMode,
                    childHeightMeasureMode, ownerWidth, ownerHeight, false, LayoutPassReason.kMeasureChild, config,
                    layoutMarkerData, layoutContext, depth, generationCount);

            child.setLayoutComputedFlexBasis(new YGFloatOptional(
                    YGFloatMax(child.getLayout().measuredDimensions.get(dim.get(mainAxis.getValue()).getValue()),
                            YGNodePaddingAndBorderForAxis(child, mainAxis, ownerWidth))));
        }
        child.setLayoutComputedFlexBasisGeneration(generationCount);
    }

    public static void YGNodeAbsoluteLayoutChild(@NotNull YGNode node, @NotNull YGNode child, final float width, final YGMeasureMode widthMode, final float height, final @NotNull YGDirection direction, YGConfig config, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final Integer depth, final Integer generationCount) {
        final YGFlexDirection mainAxis = YGResolveFlexDirection(node.getStyle().flexDirection(), direction);
        final @NotNull YGFlexDirection crossAxis = YGFlexDirectionCross(mainAxis, direction);
        final boolean isMainAxisRow = YGFlexDirectionIsRow(mainAxis);

        float childWidth = YGUndefined;
        float childHeight = YGUndefined;
        @NotNull YGMeasureMode childWidthMeasureMode;
        @NotNull YGMeasureMode childHeightMeasureMode;

        var marginRow = child.getMarginForAxis(YGFlexDirectionRow, width).unwrap();
        var marginColumn = child.getMarginForAxis(YGFlexDirectionColumn, width).unwrap();

        if (YGNodeIsStyleDimDefined(child, YGFlexDirectionRow, width)) {
            childWidth = YGResolveValue(child.getResolvedDimensions().get(YGDimensionWidth.getValue()),
                    width).unwrap() + marginRow;
        } else {


            if (child.isLeadingPositionDefined(YGFlexDirectionRow) && child.isTrailingPosDefined(
                    YGFlexDirectionRow)) {
                childWidth = node.getLayout().measuredDimensions.get(
                        YGDimensionWidth.getValue()) - (node.getLeadingBorder(
                        YGFlexDirectionRow) + node.getTrailingBorder(
                        YGFlexDirectionRow)) - (child.getLeadingPosition(
                        YGFlexDirectionRow, width).unwrap() + child.getTrailingPosition(
                        YGFlexDirectionRow, width).unwrap());
                childWidth = YGNodeBoundAxis(child, YGFlexDirectionRow, childWidth, width, width);
            }
        }

        if (YGNodeIsStyleDimDefined(child, YGFlexDirectionColumn, height)) {
            childHeight = YGResolveValue(child.getResolvedDimensions().get(YGDimensionHeight.getValue()),
                    height).unwrap() + marginColumn;
        } else {


            if (child.isLeadingPositionDefined(YGFlexDirectionColumn) && child.isTrailingPosDefined(
                    YGFlexDirectionColumn)) {
                childHeight = node.getLayout().measuredDimensions.get(
                        YGDimensionHeight.getValue()) - (node.getLeadingBorder(
                        YGFlexDirectionColumn) + node.getTrailingBorder(
                        YGFlexDirectionColumn)) - (child.getLeadingPosition(
                        YGFlexDirectionColumn, height).unwrap() + child.getTrailingPosition(
                        YGFlexDirectionColumn, height).unwrap());
                childHeight = YGNodeBoundAxis(child, YGFlexDirectionColumn, childHeight, height, width);
            }
        }


        final var childStyle = child.getStyle();
        if (YGFloatIsUndefined(childWidth) ^ YGFloatIsUndefined(childHeight)) {
            if (!childStyle.aspectRatio().isUndefined()) {
                if (YGFloatIsUndefined(childWidth)) {
                    childWidth = marginRow + (childHeight - marginColumn) * childStyle.aspectRatio().unwrap();
                } else if (YGFloatIsUndefined(childHeight)) {
                    childHeight = marginColumn + (childWidth - marginRow) / childStyle.aspectRatio().unwrap();
                }
            }
        }


        if (YGFloatIsUndefined(childWidth) || YGFloatIsUndefined(childHeight)) {
            childWidthMeasureMode = YGFloatIsUndefined(
                    childWidth) ? YGMeasureModeUndefined : YGMeasureModeExactly;
            childHeightMeasureMode = YGFloatIsUndefined(
                    childHeight) ? YGMeasureModeUndefined : YGMeasureModeExactly;


            if (!isMainAxisRow && YGFloatIsUndefined(
                    childWidth) && widthMode != YGMeasureModeUndefined && !YGFloatIsUndefined(
                    width) && width > 0F) {
                childWidth = width;
                childWidthMeasureMode = YGMeasureModeAtMost;
            }

            YGLayoutNodeInternal(child, childWidth, childHeight, direction, childWidthMeasureMode,
                    childHeightMeasureMode, childWidth, childHeight, false, LayoutPassReason.kAbsMeasureChild, config,
                    layoutMarkerData, layoutContext, depth, generationCount);
            childWidth = child.getLayout().measuredDimensions.get(
                    YGDimensionWidth.getValue()) + child.getMarginForAxis(
                    YGFlexDirectionRow, width).unwrap();
            childHeight = child.getLayout().measuredDimensions.get(
                    YGDimensionHeight.getValue()) + child.getMarginForAxis(
                    YGFlexDirectionColumn, width).unwrap();
        }

        YGLayoutNodeInternal(child, childWidth, childHeight, direction, YGMeasureModeExactly,
                YGMeasureModeExactly, childWidth, childHeight, true, LayoutPassReason.kAbsLayout, config,
                layoutMarkerData, layoutContext, depth, generationCount);

        if (child.isTrailingPosDefined(mainAxis) && !child.isLeadingPositionDefined(mainAxis)) {
            child.setLayoutPosition(node.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue()) - node.getTrailingBorder(
                    mainAxis) - child.getTrailingMargin(mainAxis, width).unwrap() - child.getTrailingPosition(mainAxis,
                    isMainAxisRow ? width : height).unwrap(), leading.get(mainAxis.getValue()).getValue());
        } else if (!child.isLeadingPositionDefined(mainAxis) && node.getStyle()
                .justifyContent() == YGJustify.YGJustifyCenter) {
            child.setLayoutPosition((node.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue())) / 2.0f, leading.get(mainAxis.getValue()).getValue());
        } else if (!child.isLeadingPositionDefined(mainAxis) && node.getStyle()
                .justifyContent() == YGJustify.YGJustifyFlexEnd) {
            child.setLayoutPosition((node.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(mainAxis.getValue()).getValue())), leading.get(mainAxis.getValue()).getValue());
        }

        if (child.isTrailingPosDefined(crossAxis) && !child.isLeadingPositionDefined(crossAxis)) {
            child.setLayoutPosition(node.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue()) - node.getTrailingBorder(
                    crossAxis) - child.getTrailingMargin(crossAxis, width).unwrap() - child.getTrailingPosition(
                    crossAxis, isMainAxisRow ? height : width).unwrap(), leading.get(crossAxis.getValue()).getValue());

        } else if (!child.isLeadingPositionDefined(crossAxis) && YGNodeAlignItem(node,
                child) == YGAlignCenter) {
            child.setLayoutPosition((node.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue())) / 2.0f, leading.get(crossAxis.getValue()).getValue());
        } else if (!child.isLeadingPositionDefined(crossAxis) && ((YGNodeAlignItem(node,
                child) == YGAlign.YGAlignFlexEnd) ^ (node.getStyle().flexWrap() == YGWrapWrapReverse))) {
            child.setLayoutPosition((node.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue()) - child.getLayout().measuredDimensions.get(
                    dim.get(crossAxis.getValue()).getValue())), leading.get(crossAxis.getValue()).getValue());
        }
    }

    public static void YGNodeWithMeasureFuncSetMeasuredDimensions(@NotNull YGNode node, float availableWidth, float availableHeight, final YGMeasureMode widthMeasureMode, final YGMeasureMode heightMeasureMode, final float ownerWidth, final float ownerHeight, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final @NotNull LayoutPassReason reason) {
        YGAssertWithNode(node, node.hasMeasureFunc(), "Expected node to have custom measure function");

        if (widthMeasureMode == YGMeasureModeUndefined) {
            availableWidth = YGUndefined;
        }
        if (heightMeasureMode == YGMeasureModeUndefined) {
            availableHeight = YGUndefined;
        }

        @NotNull final var padding = node.getLayout().padding;
        @NotNull final var border = node.getLayout().border;
        final float paddingAndBorderAxisRow = padding.get(YGEdgeLeft.getValue()) + padding.get(
                YGEdgeRight.getValue()) + border.get(YGEdgeLeft.getValue()) + border.get(
                YGEdgeRight.getValue());
        final float paddingAndBorderAxisColumn = padding.get(YGEdgeTop.getValue()) + padding.get(
                YGEdgeBottom.getValue()) + border.get(YGEdgeTop.getValue()) + border.get(
                YGEdgeBottom.getValue());


        final float innerWidth = YGFloatIsUndefined(availableWidth) ? availableWidth : YGFloatMax(0F,
                availableWidth - paddingAndBorderAxisRow);
        final float innerHeight = YGFloatIsUndefined(availableHeight) ? availableHeight : YGFloatMax(0F,
                availableHeight - paddingAndBorderAxisColumn);

        if (widthMeasureMode == YGMeasureModeExactly && heightMeasureMode == YGMeasureModeExactly) {

            node.setLayoutMeasuredDimension(
                    YGNodeBoundAxis(node, YGFlexDirectionRow, availableWidth, ownerWidth, ownerWidth),
                    YGDimensionWidth.getValue());
            node.setLayoutMeasuredDimension(
                    YGNodeBoundAxis(node, YGFlexDirectionColumn, availableHeight, ownerHeight,
                            ownerWidth), YGDimensionHeight.getValue());
        } else {
            /* Event.MeasureCallbackStart */
            Event.publish(node);


            final YGSize measuredSize = node.measure(innerWidth, widthMeasureMode, innerHeight, heightMeasureMode,
                    layoutContext);

            layoutMarkerData.measureCallbacks += 1;
            layoutMarkerData.measureCallbackReasonsCount.set(reason.getValue(), 1);

            /* Event.MeasureCallbackEnd */
            Event.publish(node,
                    new MeasureCallbackEndEventData(layoutContext, innerWidth, widthMeasureMode, innerHeight,
                            heightMeasureMode, measuredSize.width, measuredSize.height, reason));

            node.setLayoutMeasuredDimension(YGNodeBoundAxis(node, YGFlexDirectionRow,
                    (widthMeasureMode == YGMeasureModeUndefined || widthMeasureMode == YGMeasureModeAtMost) ? measuredSize.width + paddingAndBorderAxisRow : availableWidth,
                    ownerWidth, ownerWidth), YGDimensionWidth.getValue());

            node.setLayoutMeasuredDimension(YGNodeBoundAxis(node, YGFlexDirectionColumn,
                    (heightMeasureMode == YGMeasureModeUndefined || heightMeasureMode == YGMeasureModeAtMost) ? measuredSize.height + paddingAndBorderAxisColumn : availableHeight,
                    ownerHeight, ownerWidth), YGDimensionHeight.getValue());
        }
    }

    public static void YGNodeEmptyContainerSetMeasuredDimensions(@NotNull YGNode node, final float availableWidth, final float availableHeight, final YGMeasureMode widthMeasureMode, final YGMeasureMode heightMeasureMode, final float ownerWidth, final float ownerHeight) {
        @NotNull final var padding = node.getLayout().padding;
        @NotNull final var border = node.getLayout().border;

        float width = availableWidth;
        if (widthMeasureMode == YGMeasureModeUndefined || widthMeasureMode == YGMeasureModeAtMost) {
            width = padding.get(YGEdgeLeft.getValue()) + padding.get(YGEdgeRight.getValue()) + border.get(
                    YGEdgeLeft.getValue()) + border.get(YGEdgeRight.getValue());
        }
        node.setLayoutMeasuredDimension(
                YGNodeBoundAxis(node, YGFlexDirectionRow, width, ownerWidth, ownerWidth),
                YGDimensionWidth.getValue());

        float height = availableHeight;
        if (heightMeasureMode == YGMeasureModeUndefined || heightMeasureMode == YGMeasureModeAtMost) {
            height = padding.get(YGEdgeTop.getValue()) + padding.get(
                    YGEdgeBottom.getValue()) + border.get(YGEdgeTop.getValue()) + border.get(
                    YGEdgeBottom.getValue());
        }
        node.setLayoutMeasuredDimension(
                YGNodeBoundAxis(node, YGFlexDirectionColumn, height, ownerHeight, ownerWidth),
                YGDimensionHeight.getValue());
    }

    public static boolean YGNodeFixedSizeSetMeasuredDimensions(@NotNull YGNode node, final float availableWidth, final float availableHeight, final YGMeasureMode widthMeasureMode, final YGMeasureMode heightMeasureMode, final float ownerWidth, final float ownerHeight) {
        if ((!YGFloatIsUndefined(
                availableWidth) && widthMeasureMode == YGMeasureModeAtMost && availableWidth <= 0.0f) || (!YGFloatIsUndefined(
                availableHeight) && heightMeasureMode == YGMeasureModeAtMost && availableHeight <= 0.0f) || (widthMeasureMode == YGMeasureModeExactly && heightMeasureMode == YGMeasureModeExactly)) {
            node.setLayoutMeasuredDimension(YGNodeBoundAxis(node, YGFlexDirectionRow,
                    YGFloatIsUndefined(
                            availableWidth) || (widthMeasureMode == YGMeasureModeAtMost && availableWidth < 0.0f) ? 0.0f : availableWidth,
                    ownerWidth, ownerWidth), YGDimensionWidth.getValue());

            node.setLayoutMeasuredDimension(YGNodeBoundAxis(node, YGFlexDirectionColumn,
                    YGFloatIsUndefined(
                            availableHeight) || (heightMeasureMode == YGMeasureModeAtMost && availableHeight < 0.0f) ? 0.0f : availableHeight,
                    ownerHeight, ownerWidth), YGDimensionHeight.getValue());
            return true;
        }

        return false;
    }

    public static void YGZeroOutLayoutRecursivly(@NotNull YGNode node, Object layoutContext) {
        node.setLayout(null);
        node.setLayoutDimension(0, 0);
        node.setLayoutDimension(0, 1);
        node.setHasNewLayout(true);

        node.iterChildrenAfterCloningIfNeeded(GlobalMembers::YGZeroOutLayoutRecursivly, layoutContext);
    }

    public static float YGNodeCalculateAvailableInnerDim(@NotNull YGNode node, final @NotNull YGDimension dimension, final float availableDim, final float paddingAndBorder, final float ownerDim) {
        float availableInnerDim = availableDim - paddingAndBorder;


        if (!YGFloatIsUndefined(availableInnerDim)) {


            final @NotNull YGFloatOptional minDimensionOptional = YGResolveValue(
                    node.getStyle().minDimensions().get(dimension.getValue()), ownerDim);
            final float minInnerDim = minDimensionOptional.isUndefined() ? 0.0f : minDimensionOptional.unwrap() - paddingAndBorder;

            final @NotNull YGFloatOptional maxDimensionOptional = YGResolveValue(
                    node.getStyle().maxDimensions().get(dimension.getValue()), ownerDim);

            final float maxInnerDim = maxDimensionOptional.isUndefined() ? Float.MAX_VALUE : maxDimensionOptional.unwrap() - paddingAndBorder;
            availableInnerDim = YGFloatMax(YGFloatMin(availableInnerDim, maxInnerDim), minInnerDim);
        }

        return availableInnerDim;
    }

    public static float YGNodeComputeFlexBasisForChildren(@NotNull YGNode node, final float availableInnerWidth, final float availableInnerHeight, YGMeasureMode widthMeasureMode, YGMeasureMode heightMeasureMode, @NotNull YGDirection direction, @NotNull YGFlexDirection mainAxis, YGConfig config, boolean performLayout, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final Integer depth, final Integer generationCount) {
        float totalOuterFlexBasis = 0.0f;
        @Nullable YGNode singleFlexChild = null;
        ArrayList<YGNode> children = node.getChildren();
        YGMeasureMode measureModeMainDim = YGFlexDirectionIsRow(mainAxis) ? widthMeasureMode : heightMeasureMode;


        if (measureModeMainDim == YGMeasureModeExactly) {
            for (@NotNull var child : children) {
                if (child.isNodeFlexible()) {
                    if (singleFlexChild != null || YGFloatsEqual(child.resolveFlexGrow(), 0.0f) || YGFloatsEqual(
                            child.resolveFlexShrink(), 0.0f)) {


                        singleFlexChild = null;
                        break;
                    } else {
                        singleFlexChild = child;
                    }
                }
            }
        }

        for (@NotNull var child : children) {
            child.resolveDimension();
            if (child.getStyle().display() == YGDisplayNone) {
                YGZeroOutLayoutRecursivly(child, layoutContext);
                child.setHasNewLayout(true);
                child.setDirty(false);
                continue;
            }
            if (performLayout) {

                final YGDirection childDirection = child.resolveDirection(direction);
                final float mainDim = YGFlexDirectionIsRow(mainAxis) ? availableInnerWidth : availableInnerHeight;
                final float crossDim = YGFlexDirectionIsRow(mainAxis) ? availableInnerHeight : availableInnerWidth;
                child.setPosition(childDirection, mainDim, crossDim, availableInnerWidth);
            }

            if (child.getStyle().positionType() == YGPositionTypeAbsolute) {
                continue;
            }
            if (child == singleFlexChild) {
                child.setLayoutComputedFlexBasisGeneration(generationCount);
                child.setLayoutComputedFlexBasis(new YGFloatOptional(0));
            } else {
                YGNodeComputeFlexBasisForChild(node, child, availableInnerWidth, widthMeasureMode, availableInnerHeight,
                        availableInnerWidth, availableInnerHeight, heightMeasureMode, direction, config,
                        layoutMarkerData, layoutContext, depth, generationCount);
            }

            totalOuterFlexBasis += (child.getLayout().computedFlexBasis.unwrap() + child.getMarginForAxis(mainAxis,
                    availableInnerWidth).unwrap());
        }

        return totalOuterFlexBasis;
    }

    public static @NotNull YGCollectFlexItemsRowValues YGCalculateCollectFlexItemsRowValues(@NotNull YGNode node, final @NotNull YGDirection ownerDirection, final float mainAxisownerSize, final float availableInnerWidth, final float availableInnerMainDim, final Integer startOfLineIndex, final Integer lineCount) {
        @NotNull YGCollectFlexItemsRowValues flexAlgoRowMeasurement = new YGCollectFlexItemsRowValues();
        flexAlgoRowMeasurement.relativeChildren.ensureCapacity(node.getChildren().size());

        float sizeConsumedOnCurrentLineIncludingMinConstraint = 0F;
        final YGFlexDirection mainAxis = YGResolveFlexDirection(node.getStyle().flexDirection(),
                node.resolveDirection(ownerDirection));
        final boolean isNodeFlexWrap = node.getStyle().flexWrap() != YGWrapNoWrap;


        Integer endOfLineIndex = startOfLineIndex;
        for (; endOfLineIndex < node.getChildren().size(); endOfLineIndex++) {
            YGNode child = node.getChild(endOfLineIndex);
            if (child.getStyle().display() == YGDisplayNone || child.getStyle()
                    .positionType() == YGPositionTypeAbsolute) {
                continue;
            }
            child.setLineIndex(lineCount);
            final float childMarginMainAxis = child.getMarginForAxis(mainAxis, availableInnerWidth).unwrap();
            final float flexBasisWithMinAndMaxConstraints = YGNodeBoundAxisWithinMinAndMax(child, mainAxis,
                    child.getLayout().computedFlexBasis, mainAxisownerSize).unwrap();


            if (sizeConsumedOnCurrentLineIncludingMinConstraint + flexBasisWithMinAndMaxConstraints + childMarginMainAxis > availableInnerMainDim && isNodeFlexWrap && flexAlgoRowMeasurement.itemsOnLine > 0) {
                break;
            }

            sizeConsumedOnCurrentLineIncludingMinConstraint += flexBasisWithMinAndMaxConstraints + childMarginMainAxis;
            flexAlgoRowMeasurement.sizeConsumedOnCurrentLine += flexBasisWithMinAndMaxConstraints + childMarginMainAxis;
            flexAlgoRowMeasurement.itemsOnLine++;

            if (child.isNodeFlexible()) {
                flexAlgoRowMeasurement.totalFlexGrowFactors += child.resolveFlexGrow();


                flexAlgoRowMeasurement.totalFlexShrinkScaledFactors += -child.resolveFlexShrink() * child.getLayout().computedFlexBasis.unwrap();
            }

            flexAlgoRowMeasurement.relativeChildren.add(child);
        }


        if (flexAlgoRowMeasurement.totalFlexGrowFactors > 0F && flexAlgoRowMeasurement.totalFlexGrowFactors < 1F) {
            flexAlgoRowMeasurement.totalFlexGrowFactors = 1F;
        }


        if (flexAlgoRowMeasurement.totalFlexShrinkScaledFactors > 0F && flexAlgoRowMeasurement.totalFlexShrinkScaledFactors < 1F) {
            flexAlgoRowMeasurement.totalFlexShrinkScaledFactors = 1F;
        }
        flexAlgoRowMeasurement.endOfLineIndex = endOfLineIndex;
        return flexAlgoRowMeasurement;
    }

    public static float YGDistributeFreeSpaceSecondPass(@NotNull YGCollectFlexItemsRowValues collectedFlexItemsValues, @NotNull YGNode node, final @NotNull YGFlexDirection mainAxis, final @NotNull YGFlexDirection crossAxis, final float mainAxisownerSize, final float availableInnerMainDim, final float availableInnerCrossDim, final float availableInnerWidth, final float availableInnerHeight, final boolean flexBasisOverflows, final YGMeasureMode measureModeCrossDim, final boolean performLayout, YGConfig config, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final Integer depth, final Integer generationCount) {
        float childFlexBasis;
        float flexShrinkScaledFactor;
        float flexGrowFactor;
        float deltaFreeSpace = 0F;
        final boolean isMainAxisRow = YGFlexDirectionIsRow(mainAxis);
        final boolean isNodeFlexWrap = node.getStyle().flexWrap() != YGWrapNoWrap;

        for (@NotNull var currentRelativeChild : collectedFlexItemsValues.relativeChildren) {
            childFlexBasis = YGNodeBoundAxisWithinMinAndMax(currentRelativeChild, mainAxis,
                    currentRelativeChild.getLayout().computedFlexBasis, mainAxisownerSize).unwrap();
            float updatedMainSize = childFlexBasis;

            if (!YGFloatIsUndefined(
                    collectedFlexItemsValues.remainingFreeSpace) && collectedFlexItemsValues.remainingFreeSpace < 0F) {
                flexShrinkScaledFactor = -currentRelativeChild.resolveFlexShrink() * childFlexBasis;

                if (flexShrinkScaledFactor != 0F) {
                    float childSize;

                    if (!YGFloatIsUndefined(
                            collectedFlexItemsValues.totalFlexShrinkScaledFactors) && collectedFlexItemsValues.totalFlexShrinkScaledFactors == 0F) {
                        childSize = childFlexBasis + flexShrinkScaledFactor;
                    } else {
                        childSize = childFlexBasis + (collectedFlexItemsValues.remainingFreeSpace / collectedFlexItemsValues.totalFlexShrinkScaledFactors) * flexShrinkScaledFactor;
                    }

                    updatedMainSize = YGNodeBoundAxis(currentRelativeChild, mainAxis, childSize, availableInnerMainDim,
                            availableInnerWidth);
                }
            } else if (!YGFloatIsUndefined(
                    collectedFlexItemsValues.remainingFreeSpace) && collectedFlexItemsValues.remainingFreeSpace > 0F) {
                flexGrowFactor = currentRelativeChild.resolveFlexGrow();


                if (!YGFloatIsUndefined(flexGrowFactor) && flexGrowFactor != 0F) {
                    updatedMainSize = YGNodeBoundAxis(currentRelativeChild, mainAxis,
                            childFlexBasis + collectedFlexItemsValues.remainingFreeSpace / collectedFlexItemsValues.totalFlexGrowFactors * flexGrowFactor,
                            availableInnerMainDim, availableInnerWidth);
                }
            }

            deltaFreeSpace += updatedMainSize - childFlexBasis;

            final float marginMain = currentRelativeChild.getMarginForAxis(mainAxis, availableInnerWidth).unwrap();
            final float marginCross = currentRelativeChild.getMarginForAxis(crossAxis, availableInnerWidth).unwrap();

            @NotNull RefObject<Float> childCrossSize = new RefObject<>(0f);
            @NotNull RefObject<Float> childMainSize = new RefObject<>(updatedMainSize + marginMain);
            YGMeasureMode childCrossMeasureMode;
            @NotNull YGMeasureMode childMainMeasureMode = YGMeasureModeExactly;

            final var childStyle = currentRelativeChild.getStyle();
            if (!childStyle.aspectRatio().isUndefined()) {
                childCrossSize.argValue = isMainAxisRow ? (childMainSize.argValue - marginMain) / childStyle.aspectRatio()
                        .unwrap() : (childMainSize.argValue - marginMain) * childStyle.aspectRatio().unwrap();
                childCrossMeasureMode = YGMeasureModeExactly;

                childCrossSize.argValue += marginCross;
            } else if (!YGFloatIsUndefined(availableInnerCrossDim) && !YGNodeIsStyleDimDefined(currentRelativeChild,
                    crossAxis,
                    availableInnerCrossDim) && measureModeCrossDim == YGMeasureModeExactly && !(isNodeFlexWrap && flexBasisOverflows) && YGNodeAlignItem(
                    node, currentRelativeChild) == YGAlignStretch && currentRelativeChild.marginLeadingValue(
                    crossAxis).unit != YGUnitAuto && currentRelativeChild.marginTrailingValue(
                    crossAxis).unit != YGUnitAuto) {
                childCrossSize.argValue = availableInnerCrossDim;
                childCrossMeasureMode = YGMeasureModeExactly;
            } else if (!YGNodeIsStyleDimDefined(currentRelativeChild, crossAxis, availableInnerCrossDim)) {
                childCrossSize.argValue = availableInnerCrossDim;
                childCrossMeasureMode = YGFloatIsUndefined(
                        childCrossSize.argValue) ? YGMeasureModeUndefined : YGMeasureModeAtMost;
            } else {
                childCrossSize.argValue = YGResolveValue(
                        currentRelativeChild.getResolvedDimension(dim.get(crossAxis.getValue()).getValue()),
                        availableInnerCrossDim).unwrap() + marginCross;
                final boolean isLoosePercentageMeasurement = currentRelativeChild.getResolvedDimension(
                        dim.get(crossAxis.getValue())
                                .getValue()).unit == YGUnit.YGUnitPercent && measureModeCrossDim != YGMeasureModeExactly;
                childCrossMeasureMode = YGFloatIsUndefined(
                        childCrossSize.argValue) || isLoosePercentageMeasurement ? YGMeasureModeUndefined : YGMeasureModeExactly;
            }

            YGConstrainMaxSizeForMode(currentRelativeChild, mainAxis, availableInnerMainDim, availableInnerWidth,
                    childMainMeasureMode, childMainSize);
            YGConstrainMaxSizeForMode(currentRelativeChild, crossAxis, availableInnerCrossDim, availableInnerWidth,
                    childCrossMeasureMode, childCrossSize);

            final boolean requiresStretchLayout = !YGNodeIsStyleDimDefined(currentRelativeChild, crossAxis,
                    availableInnerCrossDim) && YGNodeAlignItem(node,
                    currentRelativeChild) == YGAlignStretch && currentRelativeChild.marginLeadingValue(
                    crossAxis).unit != YGUnitAuto && currentRelativeChild.marginTrailingValue(
                    crossAxis).unit != YGUnitAuto;

            final @NotNull RefObject<Float> childWidth = isMainAxisRow ? childMainSize : childCrossSize;
            final @NotNull RefObject<Float> childHeight = !isMainAxisRow ? childMainSize : childCrossSize;

            final @NotNull YGMeasureMode childWidthMeasureMode = isMainAxisRow ? childMainMeasureMode : childCrossMeasureMode;
            final @NotNull YGMeasureMode childHeightMeasureMode = !isMainAxisRow ? childMainMeasureMode : childCrossMeasureMode;

            final boolean isLayoutPass = performLayout && !requiresStretchLayout;


            YGLayoutNodeInternal(currentRelativeChild, childWidth.argValue, childHeight.argValue,
                    node.getLayout().direction(),
                    childWidthMeasureMode, childHeightMeasureMode, availableInnerWidth, availableInnerHeight,
                    isLayoutPass, isLayoutPass ? LayoutPassReason.kFlexLayout : LayoutPassReason.kFlexMeasure, config,
                    layoutMarkerData, layoutContext, depth, generationCount);
            node.setLayoutHadOverflow(node.getLayout().hadOverflow() | currentRelativeChild.getLayout().hadOverflow());
        }
        return deltaFreeSpace;
    }

    public static void YGDistributeFreeSpaceFirstPass(@NotNull YGCollectFlexItemsRowValues collectedFlexItemsValues, final @NotNull YGFlexDirection mainAxis, final float mainAxisownerSize, final float availableInnerMainDim, final float availableInnerWidth) {
        float flexShrinkScaledFactor;
        float flexGrowFactor;
        float baseMainSize;
        float boundMainSize;
        float deltaFreeSpace = 0F;

        for (@NotNull var currentRelativeChild : collectedFlexItemsValues.relativeChildren) {
            float childFlexBasis = YGNodeBoundAxisWithinMinAndMax(currentRelativeChild, mainAxis,
                    currentRelativeChild.getLayout().computedFlexBasis, mainAxisownerSize).unwrap();

            if (collectedFlexItemsValues.remainingFreeSpace < 0F) {
                flexShrinkScaledFactor = -currentRelativeChild.resolveFlexShrink() * childFlexBasis;


                if (!YGFloatIsUndefined(flexShrinkScaledFactor) && flexShrinkScaledFactor != 0F) {
                    baseMainSize = childFlexBasis + collectedFlexItemsValues.remainingFreeSpace / collectedFlexItemsValues.totalFlexShrinkScaledFactors * flexShrinkScaledFactor;
                    boundMainSize = YGNodeBoundAxis(currentRelativeChild, mainAxis, baseMainSize, availableInnerMainDim,
                            availableInnerWidth);
                    if (!YGFloatIsUndefined(baseMainSize) && !YGFloatIsUndefined(
                            boundMainSize) && baseMainSize != boundMainSize) {


                        deltaFreeSpace += boundMainSize - childFlexBasis;
                        collectedFlexItemsValues.totalFlexShrinkScaledFactors -= (-currentRelativeChild.resolveFlexShrink() * currentRelativeChild.getLayout().computedFlexBasis.unwrap());
                    }
                }
            } else if (!YGFloatIsUndefined(
                    collectedFlexItemsValues.remainingFreeSpace) && collectedFlexItemsValues.remainingFreeSpace > 0F) {
                flexGrowFactor = currentRelativeChild.resolveFlexGrow();


                if (!YGFloatIsUndefined(flexGrowFactor) && flexGrowFactor != 0F) {
                    baseMainSize = childFlexBasis + collectedFlexItemsValues.remainingFreeSpace / collectedFlexItemsValues.totalFlexGrowFactors * flexGrowFactor;
                    boundMainSize = YGNodeBoundAxis(currentRelativeChild, mainAxis, baseMainSize, availableInnerMainDim,
                            availableInnerWidth);

                    if (!YGFloatIsUndefined(baseMainSize) && !YGFloatIsUndefined(
                            boundMainSize) && baseMainSize != boundMainSize) {


                        deltaFreeSpace += boundMainSize - childFlexBasis;
                        collectedFlexItemsValues.totalFlexGrowFactors -= flexGrowFactor;
                    }
                }
            }
        }
        collectedFlexItemsValues.remainingFreeSpace -= deltaFreeSpace;
    }

    public static void YGResolveFlexibleLength(@NotNull YGNode node, @NotNull YGCollectFlexItemsRowValues collectedFlexItemsValues, final @NotNull YGFlexDirection mainAxis, final @NotNull YGFlexDirection crossAxis, final float mainAxisownerSize, final float availableInnerMainDim, final float availableInnerCrossDim, final float availableInnerWidth, final float availableInnerHeight, final boolean flexBasisOverflows, final YGMeasureMode measureModeCrossDim, final boolean performLayout, YGConfig config, @NotNull LayoutData layoutMarkerData, final Object layoutContext, final Integer depth, final Integer generationCount) {
        final float originalFreeSpace = collectedFlexItemsValues.remainingFreeSpace;

        YGDistributeFreeSpaceFirstPass(collectedFlexItemsValues, mainAxis, mainAxisownerSize, availableInnerMainDim,
                availableInnerWidth);


        final float distributedFreeSpace = YGDistributeFreeSpaceSecondPass(collectedFlexItemsValues, node, mainAxis,
                crossAxis, mainAxisownerSize, availableInnerMainDim, availableInnerCrossDim, availableInnerWidth,
                availableInnerHeight, flexBasisOverflows, measureModeCrossDim, performLayout, config, layoutMarkerData,
                layoutContext, depth, generationCount);

        collectedFlexItemsValues.remainingFreeSpace = originalFreeSpace - distributedFreeSpace;
    }

    public static void YGJustifyMainAxis(@NotNull YGNode node, @NotNull YGCollectFlexItemsRowValues collectedFlexItemsValues, final Integer startOfLineIndex, final @NotNull YGFlexDirection mainAxis, final @NotNull YGFlexDirection crossAxis, final YGMeasureMode measureModeMainDim, final YGMeasureMode measureModeCrossDim, final float mainAxisownerSize, final float ownerWidth, final float availableInnerMainDim, final float availableInnerCrossDim, final float availableInnerWidth, final boolean performLayout, final Object layoutContext) {
        final var style = node.getStyle();
        final float leadingPaddingAndBorderMain = node.getLeadingPaddingAndBorder(mainAxis, ownerWidth).unwrap();
        final float trailingPaddingAndBorderMain = node.getTrailingPaddingAndBorder(mainAxis, ownerWidth).unwrap();


        if (measureModeMainDim == YGMeasureModeAtMost && collectedFlexItemsValues.remainingFreeSpace > 0F) {
            if (!style.minDimensions().getCompactValue(dim.get(mainAxis.getValue()).getValue())
                    .isUndefined() && !YGResolveValue(
                    style.minDimensions().getCompactValue(dim.get(mainAxis.getValue()).getValue()),
                    mainAxisownerSize).isUndefined()) {


                final float minAvailableMainDim = YGResolveValue(
                        style.minDimensions().getCompactValue(dim.get(mainAxis.getValue()).getValue()),
                        mainAxisownerSize).unwrap() - leadingPaddingAndBorderMain - trailingPaddingAndBorderMain;
                final float occupiedSpaceByChildNodes = availableInnerMainDim - collectedFlexItemsValues.remainingFreeSpace;
                collectedFlexItemsValues.remainingFreeSpace = YGFloatMax(0F,
                        minAvailableMainDim - occupiedSpaceByChildNodes);
            } else {
                collectedFlexItemsValues.remainingFreeSpace = 0F;
            }
        }

        int numberOfAutoMarginsOnCurrentLine = 0;
        for (Integer i = startOfLineIndex; i < collectedFlexItemsValues.endOfLineIndex; i++) {
            YGNode child = node.getChild(i);
            if (child.getStyle().positionType() != YGPositionTypeAbsolute) {
                if (child.marginLeadingValue(mainAxis).unit == YGUnitAuto) {
                    numberOfAutoMarginsOnCurrentLine++;
                }
                if (child.marginTrailingValue(mainAxis).unit == YGUnitAuto) {
                    numberOfAutoMarginsOnCurrentLine++;
                }
            }
        }


        float leadingMainDim = 0F;
        float betweenMainDim = 0F;
        final YGJustify justifyContent = node.getStyle().justifyContent();

        if (numberOfAutoMarginsOnCurrentLine == 0) {
            switch (justifyContent) {
                case YGJustifyCenter:
                    leadingMainDim = collectedFlexItemsValues.remainingFreeSpace / 2;
                    break;
                case YGJustifyFlexEnd:
                    leadingMainDim = collectedFlexItemsValues.remainingFreeSpace;
                    break;
                case YGJustifySpaceBetween:
                    if (collectedFlexItemsValues.itemsOnLine > 1) {
                        betweenMainDim = YGFloatMax(collectedFlexItemsValues.remainingFreeSpace,
                                0F) / (collectedFlexItemsValues.itemsOnLine - 1);
                    } else {
                        betweenMainDim = 0F;
                    }
                    break;
                case YGJustifySpaceEvenly:

                    betweenMainDim = collectedFlexItemsValues.remainingFreeSpace / (collectedFlexItemsValues.itemsOnLine + 1);
                    leadingMainDim = betweenMainDim;
                    break;
                case YGJustifySpaceAround:

                    betweenMainDim = collectedFlexItemsValues.remainingFreeSpace / collectedFlexItemsValues.itemsOnLine;
                    leadingMainDim = betweenMainDim / 2;
                    break;
                case YGJustifyFlexStart:
                    break;
            }
        }

        collectedFlexItemsValues.mainDim = leadingPaddingAndBorderMain + leadingMainDim;
        collectedFlexItemsValues.crossDim = 0F;

        float maxAscentForCurrentLine = 0F;
        float maxDescentForCurrentLine = 0F;
        boolean isNodeBaselineLayout = YGIsBaselineLayout(node);
        for (Integer i = startOfLineIndex; i < collectedFlexItemsValues.endOfLineIndex; i++) {
            YGNode child = node.getChild(i);
            final YGStyle childStyle = child.getStyle();
            final YGLayout childLayout = child.getLayout();
            if (childStyle.display() == YGDisplayNone) {
                continue;
            }
            if (childStyle.positionType() == YGPositionTypeAbsolute && child.isLeadingPositionDefined(
                    mainAxis)) {
                if (performLayout) {


                    child.setLayoutPosition(
                            child.getLeadingPosition(mainAxis, availableInnerMainDim).unwrap() + node.getLeadingBorder(
                                    mainAxis) + child.getLeadingMargin(mainAxis, availableInnerWidth).unwrap(),
                            pos.get(mainAxis.getValue()).getValue());
                }
            } else {


                if (childStyle.positionType() != YGPositionTypeAbsolute) {
                    if (child.marginLeadingValue(mainAxis).unit == YGUnitAuto) {
                        collectedFlexItemsValues.mainDim += collectedFlexItemsValues.remainingFreeSpace / numberOfAutoMarginsOnCurrentLine;
                    }

                    if (performLayout) {
                        child.setLayoutPosition(
                                childLayout.position.get(
                                        pos.get(mainAxis.getValue()).getValue()) + collectedFlexItemsValues.mainDim,
                                pos.get(mainAxis.getValue()).getValue());
                    }

                    if (child.marginTrailingValue(mainAxis).unit == YGUnitAuto) {
                        collectedFlexItemsValues.mainDim += collectedFlexItemsValues.remainingFreeSpace / numberOfAutoMarginsOnCurrentLine;
                    }
                    boolean canSkipFlex = !performLayout && measureModeCrossDim == YGMeasureModeExactly;
                    if (canSkipFlex) {


                        collectedFlexItemsValues.mainDim += betweenMainDim + child.getMarginForAxis(mainAxis,
                                availableInnerWidth).unwrap() + childLayout.computedFlexBasis.unwrap();
                        collectedFlexItemsValues.crossDim = availableInnerCrossDim;
                    } else {


                        collectedFlexItemsValues.mainDim += betweenMainDim + YGNodeDimWithMargin(child, mainAxis,
                                availableInnerWidth);

                        if (isNodeBaselineLayout) {


                            final float ascent = YGBaseline(child, layoutContext) + child.getLeadingMargin(
                                    YGFlexDirectionColumn, availableInnerWidth).unwrap();
                            final float descent = child.getLayout().measuredDimensions.get(
                                    YGDimensionHeight.getValue()) + child.getMarginForAxis(
                                    YGFlexDirectionColumn, availableInnerWidth).unwrap() - ascent;

                            maxAscentForCurrentLine = YGFloatMax(maxAscentForCurrentLine, ascent);
                            maxDescentForCurrentLine = YGFloatMax(maxDescentForCurrentLine, descent);
                        } else {


                            collectedFlexItemsValues.crossDim = YGFloatMax(collectedFlexItemsValues.crossDim,
                                    YGNodeDimWithMargin(child, crossAxis, availableInnerWidth));
                        }
                    }
                } else if (performLayout) {
                    child.setLayoutPosition(
                            childLayout.position.get(pos.get(mainAxis.getValue()).getValue()) + node.getLeadingBorder(
                                    mainAxis) + leadingMainDim, pos.get(mainAxis.getValue()).getValue());
                }
            }
        }
        collectedFlexItemsValues.mainDim += trailingPaddingAndBorderMain;

        if (isNodeBaselineLayout) {
            collectedFlexItemsValues.crossDim = maxAscentForCurrentLine + maxDescentForCurrentLine;
        }
    }

    //
// This is the main routine that implements a subset of the flexbox layout
// algorithm described in the W3C CSS documentation:
// https://www.w3.org/TR/CSS3-flexbox/.
//
// Limitations of this algorithm, compared to the full standard:
//  * Display property is always assumed to be 'flex' except for Text nodes,
//    which are assumed to be 'inline-flex'.
//  * The 'zIndex' property (or any form of z ordering) is not supported. Nodes
//    are stacked in document order.
//  * The 'order' property is not supported. The order of flex items is always
//    defined by document order.
//  * The 'visibility' property is always assumed to be 'visible'. Values of
//    'collapse' and 'hidden' are not supported.
//  * There is no support for forced breaks.
//  * It does not support vertical inline directions (top-to-bottom or
//    bottom-to-top text).
//
// Deviations from standard:
//  * Section 4.5 of the spec indicates that all flex items have a default
//    minimum main size. For text blocks, for example, this is the width of the
//    widest word. Calculating the minimum width is expensive, so we forego it
//    and assume a default minimum main size of 0.
//  * Min/Max sizes in the main axis are not honored when resolving flexible
//    lengths.
//  * The spec indicates that the default value for 'flexDirection' is 'row',
//    but the algorithm below assumes a default of 'column'.
//
// Input parameters:
//    - node: current node to be sized and layed out
//    - availableWidth & availableHeight: available size to be used for sizing
//      the node or YGUndefined if the size is not available; interpretation
//      depends on layout flags
//    - ownerDirection: the inline (text) direction within the owner
//      (left-to-right or right-to-left)
//    - widthMeasureMode: indicates the sizing rules for the width (see below
//      for explanation)
//    - heightMeasureMode: indicates the sizing rules for the height (see below
//      for explanation)
//    - performLayout: specifies whether the caller is interested in just the
//      dimensions of the node or it requires the entire node and its subtree to
//      be layed out (with final positions)
//
// Details:
//    This routine is called recursively to lay out subtrees of flexbox
//    elements. It uses the information in node.style, which is treated as a
//    read-only input. It is responsible for setting the layout.direction and
//    layout.measuredDimensions fields for the input node as well as the
//    layout.position and layout.lineIndex fields for its child nodes. The
//    layout.measuredDimensions field includes any border or padding for the
//    node but does not include margins.
//
//    The spec describes four different layout modes: "fill available", "max
//    content", "min content", and "fit content". Of these, we don't use "min
//    content" because we don't support default minimum main sizes (see above
//    for details). Each of our measure modes maps to a layout mode from the
//    spec (https://www.w3.org/TR/CSS3-sizing/#terms):
//      - YGMeasureModeUndefined: max content
//      - YGMeasureModeExactly: fill available
//      - YGMeasureModeAtMost: fit content
//
//    When calling YGNodelayoutImpl and YGLayoutNodeInternal, if the caller
//    passes an available size of undefined then it must also pass a measure
//    mode of YGMeasureModeUndefined in that dimension.
//
    private static void YGNodelayoutImpl(final YGNode node, final float availableWidth, final float availableHeight, final YGDirection ownerDirection, final YGMeasureMode widthMeasureMode, final YGMeasureMode heightMeasureMode, final float ownerWidth, final float ownerHeight, final boolean performLayout, final YGConfig config, LayoutData layoutMarkerData, final Object layoutContext, final int depth, final int generationCount, final LayoutPassReason reason) {
        YGAssertWithNode(node, !YGFloatIsUndefined(availableWidth) || widthMeasureMode == YGMeasureModeUndefined,
                "availableWidth is indefinite so widthMeasureMode must be " + "YGMeasureModeUndefined");
        YGAssertWithNode(node, !YGFloatIsUndefined(availableHeight) || heightMeasureMode == YGMeasureModeUndefined,
                "availableHeight is indefinite so heightMeasureMode must be " + "YGMeasureModeUndefined");

        if (performLayout) layoutMarkerData.layouts += 1;
        else layoutMarkerData.measures += 1;

        // Set the resolved resolution in the node's layout.
        final YGDirection direction = node.resolveDirection(ownerDirection);
        node.setLayoutDirection(direction);

        final YGFlexDirection flexRowDirection = YGResolveFlexDirection(YGFlexDirectionRow, direction);
        final YGFlexDirection flexColumnDirection = YGResolveFlexDirection(YGFlexDirectionColumn, direction);

        final YGEdge startEdge = direction == YGDirectionLTR ? YGEdgeLeft : YGEdgeRight;
        final YGEdge endEdge = direction == YGDirectionLTR ? YGEdgeRight : YGEdgeLeft;

        final float marginRowLeading = node.getLeadingMargin(flexRowDirection, ownerWidth).unwrap();
        node.setLayoutMargin(marginRowLeading, startEdge.getValue());
        final float marginRowTrailing = node.getTrailingMargin(flexRowDirection, ownerWidth).unwrap();
        node.setLayoutMargin(marginRowTrailing, endEdge.getValue());
        final float marginColumnLeading = node.getLeadingMargin(flexColumnDirection, ownerWidth).unwrap();
        node.setLayoutMargin(marginColumnLeading, YGEdgeTop.getValue());
        final float marginColumnTrailing = node.getTrailingMargin(flexColumnDirection, ownerWidth).unwrap();
        node.setLayoutMargin(marginColumnTrailing, YGEdgeBottom.getValue());

        final float marginAxisRow = marginRowLeading + marginRowTrailing;
        final float marginAxisColumn = marginColumnLeading + marginColumnTrailing;

        node.setLayoutBorder(node.getLeadingBorder(flexRowDirection), startEdge.getValue());
        node.setLayoutBorder(node.getTrailingBorder(flexRowDirection), endEdge.getValue());
        node.setLayoutBorder(node.getLeadingBorder(flexColumnDirection), YGEdgeTop.getValue());
        node.setLayoutBorder(node.getTrailingBorder(flexColumnDirection), YGEdgeBottom.getValue());

        node.setLayoutPadding(node.getLeadingPadding(flexRowDirection, ownerWidth).unwrap(), startEdge.getValue());
        node.setLayoutPadding(node.getTrailingPadding(flexRowDirection, ownerWidth).unwrap(), endEdge.getValue());
        node.setLayoutPadding(node.getLeadingPadding(flexColumnDirection, ownerWidth).unwrap(), YGEdgeTop.getValue());
        node.setLayoutPadding(node.getTrailingPadding(flexColumnDirection, ownerWidth).unwrap(),
                YGEdgeBottom.getValue());

        if (node.hasMeasureFunc()) {
            YGNodeWithMeasureFuncSetMeasuredDimensions(node, availableWidth - marginAxisRow,
                    availableHeight - marginAxisColumn, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight,
                    layoutMarkerData, layoutContext, reason);
            return;
        }

        final int childCount = YGNodeGetChildCount(node);
        if (childCount == 0) {
            YGNodeEmptyContainerSetMeasuredDimensions(node, availableWidth - marginAxisRow,
                    availableHeight - marginAxisColumn, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight);
            return;
        }

        // If we're not being asked to perform a full layout we can skip the algorithm
        // if we already know the size
        if (!performLayout && YGNodeFixedSizeSetMeasuredDimensions(node, availableWidth - marginAxisRow,
                availableHeight - marginAxisColumn, widthMeasureMode, heightMeasureMode, ownerWidth, ownerHeight)) {
            return;
        }

        // At this point we know we're going to perform work. Ensure that each child
        // has a mutable copy.
        node.cloneChildrenIfNeeded(layoutContext);
        // Reset layout flags, as they could have changed.
        node.setLayoutHadOverflow(false);

        // STEP 1: CALCULATE VALUES FOR REMAINDER OF ALGORITHM
        final YGFlexDirection mainAxis = YGResolveFlexDirection(node.getStyle().flexDirection(), direction);
        final YGFlexDirection crossAxis = YGFlexDirectionCross(mainAxis, direction);
        final boolean isMainAxisRow = YGFlexDirectionIsRow(mainAxis);
        final boolean isNodeFlexWrap = node.getStyle().flexWrap() != YGWrapNoWrap;

        final float mainAxisownerSize = isMainAxisRow ? ownerWidth : ownerHeight;
        final float crossAxisownerSize = isMainAxisRow ? ownerHeight : ownerWidth;

        final float paddingAndBorderAxisMain = YGNodePaddingAndBorderForAxis(node, mainAxis, ownerWidth);
        final float leadingPaddingAndBorderCross = node.getLeadingPaddingAndBorder(crossAxis, ownerWidth).unwrap();
        final float trailingPaddingAndBorderCross = node.getTrailingPaddingAndBorder(crossAxis, ownerWidth).unwrap();
        final float paddingAndBorderAxisCross = leadingPaddingAndBorderCross + trailingPaddingAndBorderCross;

        YGMeasureMode measureModeMainDim = isMainAxisRow ? widthMeasureMode : heightMeasureMode;
        YGMeasureMode measureModeCrossDim = isMainAxisRow ? heightMeasureMode : widthMeasureMode;

        final float paddingAndBorderAxisRow = isMainAxisRow ? paddingAndBorderAxisMain : paddingAndBorderAxisCross;
        final float paddingAndBorderAxisColumn = isMainAxisRow ? paddingAndBorderAxisCross : paddingAndBorderAxisMain;

        // STEP 2: DETERMINE AVAILABLE SIZE IN MAIN AND CROSS DIRECTIONS

        float availableInnerWidth = YGNodeCalculateAvailableInnerDim(node, YGDimensionWidth,
                availableWidth - marginAxisRow, paddingAndBorderAxisRow, ownerWidth);
        float availableInnerHeight = YGNodeCalculateAvailableInnerDim(node, YGDimensionHeight,
                availableHeight - marginAxisColumn, paddingAndBorderAxisColumn, ownerHeight);

        float availableInnerMainDim = isMainAxisRow ? availableInnerWidth : availableInnerHeight;
        final float availableInnerCrossDim = isMainAxisRow ? availableInnerHeight : availableInnerWidth;

        // STEP 3: DETERMINE FLEX BASIS FOR EACH ITEM

        float totalOuterFlexBasis = YGNodeComputeFlexBasisForChildren(node, availableInnerWidth, availableInnerHeight,
                widthMeasureMode, heightMeasureMode, direction, mainAxis, config, performLayout, layoutMarkerData,
                layoutContext, depth, generationCount);

        final boolean flexBasisOverflows = measureModeMainDim != YGMeasureModeUndefined && totalOuterFlexBasis > availableInnerMainDim;
        if (isNodeFlexWrap && flexBasisOverflows && measureModeMainDim == YGMeasureModeAtMost) {
            measureModeMainDim = YGMeasureModeExactly;
        }
        // STEP 4: COLLECT FLEX ITEMS INTO FLEX LINES

        // Indexes of children that represent the first and last items in the line.
        int startOfLineIndex = 0;
        int endOfLineIndex = 0;

        // Number of lines.
        int lineCount = 0;

        // Accumulated cross dimensions of all lines so far.
        float totalLineCrossDim = 0F;

        // Max main dimension of all the lines.
        float maxLineMainDim = 0F;
        YGCollectFlexItemsRowValues collectedFlexItemsValues = new YGCollectFlexItemsRowValues();
        for (; endOfLineIndex < childCount; lineCount++, startOfLineIndex = endOfLineIndex) {
            collectedFlexItemsValues = YGCalculateCollectFlexItemsRowValues(node, ownerDirection, mainAxisownerSize,
                    availableInnerWidth, availableInnerMainDim, startOfLineIndex, lineCount);
            endOfLineIndex = collectedFlexItemsValues.endOfLineIndex;

            // If we don't need to measure the cross axis, we can skip the entire flex
            // step.
            final boolean canSkipFlex = !performLayout && measureModeCrossDim == YGMeasureModeExactly;

            // STEP 5: RESOLVING FLEXIBLE LENGTHS ON MAIN AXIS
            // Calculate the remaining available space that needs to be allocated. If
            // the main dimension size isn't known, it is computed based on the line
            // length, so there's no more space left to distribute.

            boolean sizeBasedOnContent = false;
            // If we don't measure with exact main dimension we want to ensure we don't
            // violate min and max
            if (measureModeMainDim != YGMeasureModeExactly) {
                final var minDimensions = node.getStyle().minDimensions();
                final var maxDimensions = node.getStyle().maxDimensions();
                final float minInnerWidth = YGResolveValue(minDimensions.get(YGDimensionWidth.getValue()),
                        ownerWidth).unwrap() - paddingAndBorderAxisRow;
                final float maxInnerWidth = YGResolveValue(maxDimensions.get(YGDimensionWidth.getValue()),
                        ownerWidth).unwrap() - paddingAndBorderAxisRow;
                final float minInnerHeight = YGResolveValue(minDimensions.get(YGDimensionHeight.getValue()),
                        ownerHeight).unwrap() - paddingAndBorderAxisColumn;
                final float maxInnerHeight = YGResolveValue(maxDimensions.get(YGDimensionHeight.getValue()),
                        ownerHeight).unwrap() - paddingAndBorderAxisColumn;

                final float minInnerMainDim = isMainAxisRow ? minInnerWidth : minInnerHeight;
                final float maxInnerMainDim = isMainAxisRow ? maxInnerWidth : maxInnerHeight;

                if (!YGFloatIsUndefined(
                        minInnerMainDim) && collectedFlexItemsValues.sizeConsumedOnCurrentLine < minInnerMainDim) {
                    availableInnerMainDim = minInnerMainDim;
                } else if (!YGFloatIsUndefined(
                        maxInnerMainDim) && collectedFlexItemsValues.sizeConsumedOnCurrentLine > maxInnerMainDim) {
                    availableInnerMainDim = maxInnerMainDim;
                } else {
                    if (!node.getConfig().useLegacyStretchBehaviour && ((YGFloatIsUndefined(
                            collectedFlexItemsValues.totalFlexGrowFactors) && collectedFlexItemsValues.totalFlexGrowFactors == 0) || (YGFloatIsUndefined(
                            node.resolveFlexGrow()) && node.resolveFlexGrow() == 0))) {
                        // If we don't have any children to flex or we can't flex the node
                        // itself, space we've used is all space we need. Root node also
                        // should be shrunk to minimum
                        availableInnerMainDim = collectedFlexItemsValues.sizeConsumedOnCurrentLine;
                    }

                    if (node.getConfig().useLegacyStretchBehaviour) {
                        node.setLayoutDidUseLegacyFlag(true);
                    }
                    sizeBasedOnContent = !node.getConfig().useLegacyStretchBehaviour;
                }
            }

            if (!sizeBasedOnContent && !YGFloatIsUndefined(availableInnerMainDim)) {
                collectedFlexItemsValues.remainingFreeSpace = availableInnerMainDim - collectedFlexItemsValues.sizeConsumedOnCurrentLine;
            } else if (collectedFlexItemsValues.sizeConsumedOnCurrentLine < 0) {
                // availableInnerMainDim is indefinite which means the node is being sized
                // based on its content. sizeConsumedOnCurrentLine is negative which means
                // the node will allocate 0 points for its content. Consequently,
                // remainingFreeSpace is 0 - sizeConsumedOnCurrentLine.
                collectedFlexItemsValues.remainingFreeSpace = -collectedFlexItemsValues.sizeConsumedOnCurrentLine;
            }

            if (!canSkipFlex) {
                YGResolveFlexibleLength(node, collectedFlexItemsValues, mainAxis, crossAxis, mainAxisownerSize,
                        availableInnerMainDim, availableInnerCrossDim, availableInnerWidth, availableInnerHeight,
                        flexBasisOverflows, measureModeCrossDim, performLayout, config, layoutMarkerData, layoutContext,
                        depth, generationCount);
            }

            node.setLayoutHadOverflow(
                    node.getLayout().hadOverflow() | (collectedFlexItemsValues.remainingFreeSpace < 0));

            // STEP 6: MAIN-AXIS JUSTIFICATION & CROSS-AXIS SIZE DETERMINATION

            // At this point, all the children have their dimensions set in the main
            // axis. Their dimensions are also set in the cross axis with the exception
            // of items that are aligned "stretch". We need to compute these stretch
            // values and set the final positions.

            YGJustifyMainAxis(node, collectedFlexItemsValues, startOfLineIndex, mainAxis, crossAxis, measureModeMainDim,
                    measureModeCrossDim, mainAxisownerSize, ownerWidth, availableInnerMainDim, availableInnerCrossDim,
                    availableInnerWidth, performLayout, layoutContext);

            float containerCrossAxis = availableInnerCrossDim;
            if (measureModeCrossDim == YGMeasureModeUndefined || measureModeCrossDim == YGMeasureModeAtMost) {
                // Compute the cross axis from the max cross dimension of the children.
                containerCrossAxis = YGNodeBoundAxis(node, crossAxis,
                        collectedFlexItemsValues.crossDim + paddingAndBorderAxisCross, crossAxisownerSize,
                        ownerWidth) - paddingAndBorderAxisCross;
            }

            // If there's no flex wrap, the cross dimension is defined by the container.
            if (!isNodeFlexWrap && measureModeCrossDim == YGMeasureModeExactly) {
                collectedFlexItemsValues.crossDim = availableInnerCrossDim;
            }

            // Clamp to the min/max size specified on the container.
            collectedFlexItemsValues.crossDim = YGNodeBoundAxis(node, crossAxis,
                    collectedFlexItemsValues.crossDim + paddingAndBorderAxisCross, crossAxisownerSize,
                    ownerWidth) - paddingAndBorderAxisCross;

            // STEP 7: CROSS-AXIS ALIGNMENT
            // We can skip child alignment if we're just measuring the container.
            if (performLayout) {
                for (int i = startOfLineIndex; i < endOfLineIndex; i++) {
                    final YGNode child = node.getChild(i);
                    if (child.getStyle().display() == YGDisplayNone) {
                        continue;
                    }
                    if (child.getStyle().positionType() == YGPositionTypeAbsolute) {
                        // If the child is absolutely positioned and has a
                        // top/left/bottom/right set, override all the previously computed
                        // positions to set it correctly.
                        final boolean isChildLeadingPosDefined = child.isLeadingPositionDefined(crossAxis);
                        if (isChildLeadingPosDefined) {
                            child.setLayoutPosition(child.getLeadingPosition(crossAxis, availableInnerCrossDim)
                                            .unwrap() + node.getLeadingBorder(crossAxis) + child.getLeadingMargin(crossAxis,
                                            availableInnerWidth).unwrap(),
                                    pos.get(crossAxis.getValue()).getValue());
                        }
                        // If leading position is not defined or calculations result in Nan,
                        // default to border + margin
                        if (!isChildLeadingPosDefined || YGFloatIsUndefined(child.getLayout().position.get(pos.get(
                                crossAxis.getValue()).getValue()))) {
                            child.setLayoutPosition(node.getLeadingBorder(crossAxis) + child.getLeadingMargin(crossAxis,
                                    availableInnerWidth).unwrap(), pos.get(
                                    crossAxis.getValue()).getValue());
                        }
                    } else {
                        float leadingCrossDim = leadingPaddingAndBorderCross;

                        // For a relative children, we're either using alignItems (owner) or
                        // alignSelf (child) in order to determine the position in the cross
                        // axis
                        final YGAlign alignItem = YGNodeAlignItem(node, child);

                        // If the child uses align stretch, we need to lay it out one more
                        // time, this time forcing the cross-axis size to be the computed
                        // cross size for the current line.
                        if (alignItem == YGAlignStretch && child.marginLeadingValue(
                                crossAxis).unit != YGUnitAuto && child.marginTrailingValue(
                                crossAxis).unit != YGUnitAuto) {
                            // If the child defines a definite size for its cross axis, there's
                            // no need to stretch.
                            if (!YGNodeIsStyleDimDefined(child, crossAxis, availableInnerCrossDim)) {
                                RefObject<Float> childMainSize = new RefObject<>(
                                        child.getLayout().measuredDimensions.get(
                                                dim.get(mainAxis.getValue()).getValue()));
                                final var childStyle = child.getStyle();
                                RefObject<Float> childCrossSize = new RefObject<>(
                                        !childStyle.aspectRatio().isUndefined() ? child.getMarginForAxis(crossAxis,
                                                        availableInnerWidth)
                                                .unwrap() + (isMainAxisRow ? childMainSize.argValue / childStyle.aspectRatio()
                                                .unwrap() : childMainSize.argValue * childStyle.aspectRatio()
                                                .unwrap()) : collectedFlexItemsValues.crossDim);

                                childMainSize.argValue += child.getMarginForAxis(mainAxis, availableInnerWidth)
                                        .unwrap();

                                YGConstrainMaxSizeForMode(child, mainAxis, availableInnerMainDim, availableInnerWidth,
                                        YGMeasureModeExactly, childMainSize);
                                YGConstrainMaxSizeForMode(child, crossAxis, availableInnerCrossDim, availableInnerWidth,
                                        YGMeasureModeExactly, childCrossSize);

                                final float childWidth = isMainAxisRow ? childMainSize.argValue : childCrossSize.argValue;
                                final float childHeight = !isMainAxisRow ? childMainSize.argValue : childCrossSize.argValue;

                                var alignContent = node.getStyle().alignContent();
                                var crossAxisDoesNotGrow = alignContent != YGAlignStretch && isNodeFlexWrap;
                                final YGMeasureMode childWidthMeasureMode = YGFloatIsUndefined(
                                        childWidth) || (!isMainAxisRow && crossAxisDoesNotGrow) ? YGMeasureModeUndefined : YGMeasureModeExactly;
                                final YGMeasureMode childHeightMeasureMode = YGFloatIsUndefined(
                                        childHeight) || (isMainAxisRow && crossAxisDoesNotGrow) ? YGMeasureModeUndefined : YGMeasureModeExactly;

                                YGLayoutNodeInternal(child, childWidth, childHeight, direction, childWidthMeasureMode,
                                        childHeightMeasureMode, availableInnerWidth, availableInnerHeight, true,
                                        LayoutPassReason.kStretch, config, layoutMarkerData, layoutContext, depth,
                                        generationCount);
                            }
                        } else {
                            final float remainingCrossDim = containerCrossAxis - YGNodeDimWithMargin(child, crossAxis,
                                    availableInnerWidth);

                            if (child.marginLeadingValue(crossAxis).unit == YGUnitAuto && child.marginTrailingValue(
                                    crossAxis).unit == YGUnitAuto) {
                                leadingCrossDim += YGFloatMax(0.0f, remainingCrossDim / 2);
                            } else if (child.marginTrailingValue(crossAxis).unit == YGUnitAuto) {
                                // No-Op
                            } else if (child.marginLeadingValue(crossAxis).unit == YGUnitAuto) {
                                leadingCrossDim += YGFloatMax(0.0f, remainingCrossDim);
                            } else if (alignItem == YGAlignFlexStart) {
                                // No-Op
                            } else if (alignItem == YGAlignCenter) {
                                leadingCrossDim += remainingCrossDim / 2;
                            } else {
                                leadingCrossDim += remainingCrossDim;
                            }
                        }
                        // And we apply the position
                        child.setLayoutPosition(
                                child.getLayout().position.get(
                                        pos.get(crossAxis.getValue()).getValue()) + totalLineCrossDim + leadingCrossDim,
                                pos.get(
                                        crossAxis.getValue()).getValue());
                    }
                }
            }

            totalLineCrossDim += collectedFlexItemsValues.crossDim;
            maxLineMainDim = YGFloatMax(maxLineMainDim, collectedFlexItemsValues.mainDim);
        }

        // STEP 8: MULTI-LINE CONTENT ALIGNMENT
        // currentLead stores the size of the cross dim
        if (performLayout && (isNodeFlexWrap || YGIsBaselineLayout(node))) {
            float crossDimLead = 0F;
            float currentLead = leadingPaddingAndBorderCross;
            if (!YGFloatIsUndefined(availableInnerCrossDim)) {
                final float remainingAlignContentDim = availableInnerCrossDim - totalLineCrossDim;
                switch (node.getStyle().alignContent()) {
                    case YGAlignFlexEnd:
                        currentLead += remainingAlignContentDim;
                        break;
                    case YGAlignCenter:
                        currentLead += remainingAlignContentDim / 2;
                        break;
                    case YGAlignStretch:
                        if (availableInnerCrossDim > totalLineCrossDim) {
                            crossDimLead = remainingAlignContentDim / lineCount;
                        }
                        break;
                    case YGAlignSpaceAround:
                        if (availableInnerCrossDim > totalLineCrossDim) {
                            currentLead += remainingAlignContentDim / (2 * lineCount);
                            if (lineCount > 1) {
                                crossDimLead = remainingAlignContentDim / lineCount;
                            }
                        } else {
                            currentLead += remainingAlignContentDim / 2;
                        }
                        break;
                    case YGAlignSpaceBetween:
                        if (availableInnerCrossDim > totalLineCrossDim && lineCount > 1) {
                            crossDimLead = remainingAlignContentDim / (lineCount - 1);
                        }
                        break;
                    case YGAlignAuto:
                    case YGAlignFlexStart:
                    case YGAlignBaseline:
                        break;
                }
            }
            int endIndex = 0;
            for (int i = 0; i < lineCount; i++) {
                final int startIndex = endIndex;
                int ii;

                // compute the line's height and find the endIndex
                float lineHeight = 0F;
                float maxAscentForCurrentLine = 0F;
                float maxDescentForCurrentLine = 0F;
                for (ii = startIndex; ii < childCount; ii++) {
                    final YGNode child = node.getChild(ii);
                    if (child.getStyle().display() == YGDisplayNone) {
                        continue;
                    }
                    if (child.getStyle().positionType() != YGPositionTypeAbsolute) {
                        if (child.getLineIndex() != i) {
                            break;
                        }
                        if (YGNodeIsLayoutDimDefined(child, crossAxis)) {
                            lineHeight = YGFloatMax(lineHeight, child.getLayout().measuredDimensions.get(dim.get(
                                    crossAxis.getValue()).getValue()) + child.getMarginForAxis(crossAxis,
                                    availableInnerWidth).unwrap());
                        }
                        if (YGNodeAlignItem(node, child) == YGAlignBaseline) {
                            final float ascent = YGBaseline(child, layoutContext) + child.getLeadingMargin(
                                    YGFlexDirectionColumn, availableInnerWidth).unwrap();
                            final float descent = child.getLayout().measuredDimensions.get(
                                    YGDimensionHeight.getValue()) + child.getMarginForAxis(YGFlexDirectionColumn,
                                    availableInnerWidth).unwrap() - ascent;
                            maxAscentForCurrentLine = YGFloatMax(maxAscentForCurrentLine, ascent);
                            maxDescentForCurrentLine = YGFloatMax(maxDescentForCurrentLine, descent);
                            lineHeight = YGFloatMax(lineHeight, maxAscentForCurrentLine + maxDescentForCurrentLine);
                        }
                    }
                }
                endIndex = ii;
                lineHeight += crossDimLead;

                if (performLayout) {
                    for (ii = startIndex; ii < endIndex; ii++) {
                        final YGNode child = node.getChild(ii);
                        if (child.getStyle().display() == YGDisplayNone) {
                            continue;
                        }
                        if (child.getStyle().positionType() != YGPositionTypeAbsolute) {
                            switch (YGNodeAlignItem(node, child)) {
                                case YGAlignFlexStart: {
                                    child.setLayoutPosition(
                                            currentLead + child.getLeadingMargin(crossAxis, availableInnerWidth)
                                                    .unwrap(), pos.get(
                                                    crossAxis.getValue()).getValue());
                                    break;
                                }
                                case YGAlignFlexEnd: {
                                    child.setLayoutPosition(
                                            currentLead + lineHeight - child.getTrailingMargin(crossAxis,
                                                            availableInnerWidth)
                                                    .unwrap() - child.getLayout().measuredDimensions.get(
                                                    dim.get(
                                                            crossAxis.getValue()).getValue()),
                                            pos.get(crossAxis.getValue()).getValue());
                                    break;
                                }
                                case YGAlignCenter: {
                                    float childHeight = child.getLayout().measuredDimensions.get(dim.get(
                                            crossAxis.getValue()).getValue());

                                    child.setLayoutPosition(currentLead + (lineHeight - childHeight) / 2, pos.get(
                                            crossAxis.getValue()).getValue());
                                    break;
                                }
                                case YGAlignStretch: {
                                    child.setLayoutPosition(
                                            currentLead + child.getLeadingMargin(crossAxis, availableInnerWidth)
                                                    .unwrap(), pos.get(
                                                    crossAxis.getValue()).getValue());

                                    // Remeasure child with the line height as it as been only
                                    // measured with the owners height yet.
                                    if (!YGNodeIsStyleDimDefined(child, crossAxis, availableInnerCrossDim)) {
                                        final float childWidth = isMainAxisRow ? (child.getLayout().measuredDimensions.get(
                                                YGDimensionWidth.getValue()) + child.getMarginForAxis(mainAxis,
                                                availableInnerWidth).unwrap()) : lineHeight;

                                        final float childHeight = !isMainAxisRow ? (child.getLayout().measuredDimensions.get(
                                                YGDimensionHeight.getValue()) + child.getMarginForAxis(crossAxis,
                                                availableInnerWidth).unwrap()) : lineHeight;

                                        if (!(YGFloatsEqual(childWidth,
                                                child.getLayout().measuredDimensions.get(
                                                        YGDimensionWidth.getValue())) && YGFloatsEqual(childHeight,
                                                child.getLayout().measuredDimensions.get(
                                                        YGDimensionHeight.getValue())))) {
                                            YGLayoutNodeInternal(child, childWidth, childHeight, direction,
                                                    YGMeasureModeExactly, YGMeasureModeExactly, availableInnerWidth,
                                                    availableInnerHeight, true, LayoutPassReason.kMultilineStretch,
                                                    config, layoutMarkerData, layoutContext, depth, generationCount);
                                        }
                                    }
                                    break;
                                }
                                case YGAlignBaseline: {
                                    child.setLayoutPosition(currentLead + maxAscentForCurrentLine - YGBaseline(child,
                                            layoutContext) + child.getLeadingPosition(YGFlexDirectionColumn,
                                            availableInnerCrossDim).unwrap(), YGEdgeTop.getValue());

                                    break;
                                }
                                case YGAlignAuto:
                                case YGAlignSpaceBetween:
                                case YGAlignSpaceAround:
                                    break;
                            }
                        }
                    }
                }
                currentLead += lineHeight;
            }
        }

        // STEP 9: COMPUTING FINAL DIMENSIONS

        node.setLayoutMeasuredDimension(
                YGNodeBoundAxis(node, YGFlexDirectionRow, availableWidth - marginAxisRow, ownerWidth, ownerWidth),
                YGDimensionWidth.getValue());

        node.setLayoutMeasuredDimension(
                YGNodeBoundAxis(node, YGFlexDirectionColumn, availableHeight - marginAxisColumn, ownerHeight,
                        ownerWidth), YGDimensionHeight.getValue());

        // If the user didn't specify a width or height for the node, set the
        // dimensions based on the children.
        if (measureModeMainDim == YGMeasureModeUndefined || (node.getStyle()
                .overflow() != YGOverflowScroll && measureModeMainDim == YGMeasureModeAtMost)) {
            // Clamp the size to the min/max size, if specified, and make sure it
            // doesn't go below the padding and border amount.
            node.setLayoutMeasuredDimension(
                    YGNodeBoundAxis(node, mainAxis, maxLineMainDim, mainAxisownerSize, ownerWidth), dim.get(
                            mainAxis.getValue()).getValue());

        } else if (measureModeMainDim == YGMeasureModeAtMost && node.getStyle().overflow() == YGOverflowScroll) {
            node.setLayoutMeasuredDimension(YGFloatMax(YGFloatMin(availableInnerMainDim + paddingAndBorderAxisMain,
                    YGNodeBoundAxisWithinMinAndMax(node, mainAxis, new YGFloatOptional((maxLineMainDim)),
                            mainAxisownerSize).unwrap()), paddingAndBorderAxisMain), dim.get(
                    mainAxis.getValue()).getValue());
        }

        if (measureModeCrossDim == YGMeasureModeUndefined || (node.getStyle()
                .overflow() != YGOverflowScroll && measureModeCrossDim == YGMeasureModeAtMost)) {
            // Clamp the size to the min/max size, if specified, and make sure it
            // doesn't go below the padding and border amount.
            node.setLayoutMeasuredDimension(
                    YGNodeBoundAxis(node, crossAxis, totalLineCrossDim + paddingAndBorderAxisCross, crossAxisownerSize,
                            ownerWidth), dim.get(
                            crossAxis.getValue()).getValue());

        } else if (measureModeCrossDim == YGMeasureModeAtMost && node.getStyle().overflow() == YGOverflowScroll) {
            node.setLayoutMeasuredDimension(YGFloatMax(YGFloatMin(availableInnerCrossDim + paddingAndBorderAxisCross,
                    YGNodeBoundAxisWithinMinAndMax(node, crossAxis,
                            new YGFloatOptional((totalLineCrossDim + paddingAndBorderAxisCross)),
                            crossAxisownerSize).unwrap()), paddingAndBorderAxisCross), dim.get(
                    crossAxis.getValue()).getValue());
        }

        // As we only wrapped in normal direction yet, we need to reverse the
        // positions on wrap-reverse.
        if (performLayout && node.getStyle().flexWrap() == YGWrapWrapReverse) {
            for (int i = 0; i < childCount; i++) {
                final YGNode child = YGNodeGetChild(node, i);
                if (child.getStyle().positionType() != YGPositionTypeAbsolute) {
                    child.setLayoutPosition(
                            node.getLayout().measuredDimensions.get(
                                    dim.get(crossAxis.getValue()).getValue()) - child.getLayout().position.get(
                                    pos.get(
                                                    crossAxis.getValue())
                                            .getValue()) - child.getLayout().measuredDimensions.get(
                                    dim.get(
                                            crossAxis.getValue()).getValue()), pos.get(
                                    crossAxis.getValue()).getValue());
                }
            }
        }

        if (performLayout) {
            // STEP 10: SIZING AND POSITIONING ABSOLUTE CHILDREN
            for (var child : node.getChildren()) {
                if (child.getStyle().display() == YGDisplayNone || child.getStyle()
                        .positionType() != YGPositionTypeAbsolute) {
                    continue;
                }
                YGNodeAbsoluteLayoutChild(node, child, availableInnerWidth,
                        isMainAxisRow ? measureModeMainDim : measureModeCrossDim, availableInnerHeight, direction,
                        config, layoutMarkerData, layoutContext, depth, generationCount);
            }

            // STEP 11: SETTING TRAILING POSITIONS FOR CHILDREN
            final boolean needsMainTrailingPos = mainAxis == YGFlexDirectionRowReverse || mainAxis == YGFlexDirectionColumnReverse;
            final boolean needsCrossTrailingPos = crossAxis == YGFlexDirectionRowReverse || crossAxis == YGFlexDirectionColumnReverse;

            // Set trailing position if necessary.
            if (needsMainTrailingPos || needsCrossTrailingPos) {
                for (int i = 0; i < childCount; i++) {
                    final YGNode child = node.getChild(i);
                    if (child.getStyle().display() == YGDisplayNone) {
                        continue;
                    }
                    if (needsMainTrailingPos) {
                        YGNodeSetChildTrailingPosition(node, child, mainAxis);
                    }

                    if (needsCrossTrailingPos) {
                        YGNodeSetChildTrailingPosition(node, child, crossAxis);
                    }
                }
            }
        }
    }

    public static @NotNull String YGSpacer(final int level) {
        final int spacerLen = spacer.length();
        if (level > spacerLen) {
            return spacer.substring(0, 0);
        } else {
            return spacer.substring(spacerLen - level);
        }
    }

    public static String YGMeasureModeName(final @NotNull YGMeasureMode mode, final boolean performLayout) {
        var N = YGMeasureMode.SIZE;
        String @NotNull [] kMeasureModeNames = {"UNDEFINED", "EXACTLY", "AT_MOST"};
        String @NotNull [] kLayoutModeNames = {"LAY_UNDEFINED", "LAY_EXACTLY", "LAY_AT_MOST"};

        if (mode.getValue() >= N) {
            return "";
        }

        return performLayout ? kLayoutModeNames[(mode.getValue())] : kMeasureModeNames[(mode.getValue())];
    }

    public static boolean YGMeasureModeSizeIsExactAndMatchesOldMeasuredSize(YGMeasureMode sizeMode, float size, float lastComputedSize) {
        return sizeMode == YGMeasureModeExactly && YGFloatsEqual(size, lastComputedSize);
    }

    public static boolean YGMeasureModeOldSizeIsUnspecifiedAndStillFits(YGMeasureMode sizeMode, float size, YGMeasureMode lastSizeMode, float lastComputedSize) {
        return sizeMode == YGMeasureModeAtMost && lastSizeMode == YGMeasureModeUndefined && (size >= lastComputedSize || YGFloatsEqual(
                size, lastComputedSize));
    }

    public static boolean YGMeasureModeNewMeasureSizeIsStricterAndStillValid(YGMeasureMode sizeMode, float size, YGMeasureMode lastSizeMode, float lastSize, float lastComputedSize) {
        return lastSizeMode == YGMeasureModeAtMost && sizeMode == YGMeasureModeAtMost && !YGFloatIsUndefined(
                lastSize) && !YGFloatIsUndefined(size) && !YGFloatIsUndefined(
                lastComputedSize) && lastSize > size && (lastComputedSize <= size || YGFloatsEqual(size,
                lastComputedSize));
    }

    public static void YGRoundToPixelGrid(@NotNull YGNode node, final double pointScaleFactor, final double absoluteLeft, final double absoluteTop) {
        if (pointScaleFactor == 0.0f) {
            return;
        }

        final double nodeLeft = node.getLayout().position.get(YGEdgeLeft.getValue());
        final double nodeTop = node.getLayout().position.get(YGEdgeTop.getValue());

        final double nodeWidth = node.getLayout().dimensions.get(YGDimensionWidth.getValue());
        final double nodeHeight = node.getLayout().dimensions.get(YGDimensionHeight.getValue());

        final double absoluteNodeLeft = absoluteLeft + nodeLeft;
        final double absoluteNodeTop = absoluteTop + nodeTop;

        final double absoluteNodeRight = absoluteNodeLeft + nodeWidth;
        final double absoluteNodeBottom = absoluteNodeTop + nodeHeight;


        final boolean textRounding = node.getNodeType() == YGNodeType.YGNodeTypeText;

        node.setLayoutPosition(YGRoundValueToPixelGrid(nodeLeft, pointScaleFactor, false, textRounding),
                YGEdgeLeft.getValue());

        node.setLayoutPosition(YGRoundValueToPixelGrid(nodeTop, pointScaleFactor, false, textRounding),
                YGEdgeTop.getValue());


        final boolean hasFractionalWidth = !YGDoubleEqual(nodeWidth * pointScaleFactor % 1.0, 0) && !YGDoubleEqual(
                nodeWidth * pointScaleFactor % 1.0, 1.0);
        final boolean hasFractionalHeight = !YGDoubleEqual(nodeHeight * pointScaleFactor % 1.0,
                0) && !YGDoubleEqual(nodeHeight * pointScaleFactor % 1.0, 1.0);

        node.setLayoutDimension(
                YGRoundValueToPixelGrid(absoluteNodeRight, pointScaleFactor, (textRounding && hasFractionalWidth),
                        (textRounding && !hasFractionalWidth)) - YGRoundValueToPixelGrid(absoluteNodeLeft,
                        pointScaleFactor, false, textRounding), YGDimensionWidth.getValue());

        node.setLayoutDimension(
                YGRoundValueToPixelGrid(absoluteNodeBottom, pointScaleFactor, (textRounding && hasFractionalHeight),
                        (textRounding && !hasFractionalHeight)) - YGRoundValueToPixelGrid(absoluteNodeTop,
                        pointScaleFactor, false, textRounding), YGDimensionHeight.getValue());

        final @NotNull Integer childCount = YGNodeGetChildCount(node);
        for (@NotNull Integer i = 0; i < childCount; i++) {
            YGRoundToPixelGrid(YGNodeGetChild(node, i), pointScaleFactor, absoluteNodeLeft, absoluteNodeTop);
        }
    }

    public static void unsetUseLegacyFlagRecursively(@NotNull YGNode node) {
        if (node.getConfig() != null) {
            node.getConfig().useLegacyStretchBehaviour = false;
        }
        for (@NotNull var child : node.getChildren()) {
            unsetUseLegacyFlagRecursively(child);
        }
    }

    public static void YGNodeCalculateLayoutWithContext(@NotNull YGNode node, final float ownerWidth, final float ownerHeight, final @NotNull YGDirection ownerDirection, Object layoutContext) {

        /* Event.LayoutPassStart */
        Event.publish(node, new LayoutPassStartEventData(layoutContext));
        @NotNull LayoutData markerData = new LayoutData();

        gCurrentGenerationCount.incrementAndGet();
        node.resolveDimension();
        float width;
        @NotNull YGMeasureMode widthMeasureMode;
        @NotNull final var maxDimensions = node.getStyle().maxDimensions();
        if (YGNodeIsStyleDimDefined(node, YGFlexDirectionRow, ownerWidth)) {
            width = (YGResolveValue(
                    node.getResolvedDimension(dim.get(YGFlexDirectionRow.getValue()).getValue()),
                    ownerWidth).unwrap() + node.getMarginForAxis(YGFlexDirectionRow, ownerWidth)
                    .unwrap());
            widthMeasureMode = YGMeasureModeExactly;
        } else if (!YGResolveValue(maxDimensions.get(YGDimensionWidth.getValue()),
                ownerWidth).isUndefined()) {
            width = YGResolveValue(maxDimensions.get(YGDimensionWidth.getValue()), ownerWidth).unwrap();
            widthMeasureMode = YGMeasureModeAtMost;
        } else {
            width = ownerWidth;
            widthMeasureMode = YGFloatIsUndefined(
                    width) ? YGMeasureModeUndefined : YGMeasureModeExactly;
        }

        float height;
        @NotNull YGMeasureMode heightMeasureMode;
        if (YGNodeIsStyleDimDefined(node, YGFlexDirectionColumn, ownerHeight)) {
            height = (YGResolveValue(
                    node.getResolvedDimension(dim.get(YGFlexDirectionColumn.getValue()).getValue()),
                    ownerHeight).unwrap() + node.getMarginForAxis(YGFlexDirectionColumn, ownerWidth)
                    .unwrap());
            heightMeasureMode = YGMeasureModeExactly;
        } else if (!YGResolveValue(maxDimensions.get(YGDimensionHeight.getValue()),
                ownerHeight).isUndefined()) {
            height = YGResolveValue(maxDimensions.get(YGDimensionHeight.getValue()), ownerHeight).unwrap();
            heightMeasureMode = YGMeasureModeAtMost;
        } else {
            height = ownerHeight;
            heightMeasureMode = YGFloatIsUndefined(
                    height) ? YGMeasureModeUndefined : YGMeasureModeExactly;
        }
        if (YGLayoutNodeInternal(node, width, height, ownerDirection, widthMeasureMode, heightMeasureMode, ownerWidth,
                ownerHeight, true, LayoutPassReason.kInitial, node.getConfig(), markerData, layoutContext, 0,
                gCurrentGenerationCount.get())) {
            node.setPosition(node.getLayout().direction(), ownerWidth, ownerHeight, ownerWidth);
            if (node.getConfig() != null) {
                YGRoundToPixelGrid(node, node.getConfig().pointScaleFactor, 0.0f, 0.0f);
            }
        }

        /* Event.LayoutPassEnd */
        Event.publish(node, new LayoutPassEndEventData(layoutContext, markerData));
        if (node.getConfig() != null && node.getConfig().shouldDiffLayoutWithoutLegacyStretchBehaviour && node.didUseLegacyFlag()) {
            @NotNull YGNode nodeWithoutLegacyFlag = YGNodeDeepClone(node);
            nodeWithoutLegacyFlag.resolveDimension();

            nodeWithoutLegacyFlag.markDirtyAndPropogateDownwards();
            gCurrentGenerationCount.incrementAndGet();

            unsetUseLegacyFlagRecursively(nodeWithoutLegacyFlag);
            @NotNull LayoutData layoutMarkerData = new LayoutData();
            if (YGLayoutNodeInternal(nodeWithoutLegacyFlag, width, height, ownerDirection, widthMeasureMode,
                    heightMeasureMode, ownerWidth, ownerHeight, true, LayoutPassReason.kInitial,
                    nodeWithoutLegacyFlag.getConfig(), layoutMarkerData, layoutContext, 0,
                    gCurrentGenerationCount.get())) {
                nodeWithoutLegacyFlag.setPosition(nodeWithoutLegacyFlag.getLayout().direction(), ownerWidth,
                        ownerHeight, ownerWidth);
                YGRoundToPixelGrid(nodeWithoutLegacyFlag, nodeWithoutLegacyFlag.getConfig().pointScaleFactor, 0.0f,
                        0.0f);


                var neededLegacyStretchBehaviour = !nodeWithoutLegacyFlag.isLayoutTreeEqualToNode(node);
                node.setLayoutDoesLegacyFlagAffectsLayout(neededLegacyStretchBehaviour);
            }
            YGConfigFreeRecursive(nodeWithoutLegacyFlag);
            YGNodeFreeRecursive(nodeWithoutLegacyFlag);
        }
    }

    public static void YGTraverseChildrenPreOrder(final @NotNull ArrayList<YGNode> children, @NotNull NodeTraverseDelegate f) {
        for (@NotNull YGNode node : children) {
            f.invoke(node);
            YGTraverseChildrenPreOrder(node.getChildren(), f);
        }
    }

    public static void YGTraversePreOrder(final @Nullable YGNode node, @NotNull NodeTraverseDelegate f) {
        if (node == null) {
            return;
        }
        f.invoke(node);
        YGTraverseChildrenPreOrder(node.getChildren(), f);
    }

    public static @NotNull YGFloatOptional plus(@NotNull YGFloatOptional lhs, @NotNull YGFloatOptional rhs) {
        return new YGFloatOptional(lhs.unwrap() + rhs.unwrap());
    }

    public static boolean greaterThan(@NotNull YGFloatOptional lhs, @NotNull YGFloatOptional rhs) {
        return lhs.unwrap() > rhs.unwrap();
    }

    public static boolean lessThan(@NotNull YGFloatOptional lhs, @NotNull YGFloatOptional rhs) {
        return lhs.unwrap() < rhs.unwrap();
    }

    public static boolean greaterThanOrEqualTo(@NotNull YGFloatOptional lhs, @NotNull YGFloatOptional rhs) {
        return lhs.unwrap() > rhs.unwrap() || lhs.unwrap() == rhs.unwrap();
    }

    public static boolean lessThanOrEqualTo(@NotNull YGFloatOptional lhs, @NotNull YGFloatOptional rhs) {
        return lhs.unwrap() < rhs.unwrap() || lhs.unwrap() == rhs.unwrap();
    }

    @FunctionalInterface
    interface NodeTraverseDelegate {
        void invoke(YGNode node);
    }
}
