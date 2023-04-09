package io.github.orioncraftmc.meditate.internal;

import static io.github.orioncraftmc.meditate.internal.GlobalMembers.YGFloatArrayEqual;
import static io.github.orioncraftmc.meditate.internal.GlobalMembers.YGUndefined;
import static io.github.orioncraftmc.meditate.internal.detail.GlobalMembers.*;
import io.github.orioncraftmc.meditate.internal.enums.YGDirection;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public class YGLayout {

    // This value was chosen based on empirical data:
    // 98% of analyzed layouts require less than 8 entries.
    public static final int YG_MAX_CACHED_RESULT_COUNT = 8;
    private static final int directionOffset = 0;
    private static final int didUseLegacyFlagOffset = directionOffset + bitWidthFn(YGDirection.class);
    private static final int doesLegacyStretchFlagAffectsLayoutOffset = didUseLegacyFlagOffset + 1;
    private static final int hadOverflowOffset = doesLegacyStretchFlagAffectsLayoutOffset + 1;
    private final Map<Object, Object> flags = new HashMap<>();
    public final @NotNull ArrayList<Float> position = createEmptyFloatArray();
    public final @NotNull ArrayList<Float> dimensions = new ArrayList<>(Arrays.asList(YGUndefined, YGUndefined));
    public final @NotNull ArrayList<Float> margin = createEmptyFloatArray();
    public final @NotNull ArrayList<Float> border = createEmptyFloatArray();
    public final @NotNull ArrayList<Float> padding = createEmptyFloatArray();
    public int computedFlexBasisGeneration = 0;
    public YGFloatOptional computedFlexBasis = new YGFloatOptional();
    public int generationCount = 0;
    public YGDirection lastOwnerDirection = YGDirection.YGDirectionInherit;
    public int nextCachedMeasurementsIndex = 0;
    public final @NotNull ArrayList<YGCachedMeasurement> cachedMeasurements = new ArrayList<>(YG_MAX_CACHED_RESULT_COUNT);
    public final @NotNull ArrayList<Float> measuredDimensions = new ArrayList<>(Arrays.asList(YGUndefined, YGUndefined));
    public final @NotNull YGCachedMeasurement cachedLayout = new YGCachedMeasurement();

    public YGLayout() {
        for (int i = 0; i < YG_MAX_CACHED_RESULT_COUNT; i++) {
            cachedMeasurements.add(new YGCachedMeasurement());
        }
    }

    @NotNull
    private ArrayList<Float> createEmptyFloatArray() {
        return new ArrayList<>(List.of(0f, 0f, 0f, 0f));
    }

    public final YGDirection direction() {
        return getEnumData(YGDirection.class, flags, directionOffset);
    }

    public final void setDirection(@NotNull YGDirection direction) {
        setEnumData(YGDirection.class, flags, directionOffset, direction);
    }

    public final boolean didUseLegacyFlag() {
        return getBooleanData(flags, didUseLegacyFlagOffset);
    }

    public final void setDidUseLegacyFlag(boolean val) {
        setBooleanData(flags, didUseLegacyFlagOffset, val);
    }

    public final boolean doesLegacyStretchFlagAffectsLayout() {
        return getBooleanData(flags, doesLegacyStretchFlagAffectsLayoutOffset);
    }

    public final void setDoesLegacyStretchFlagAffectsLayout(boolean val) {
        setBooleanData(flags, doesLegacyStretchFlagAffectsLayoutOffset, val);
    }

    public final boolean hadOverflow() {
        return getBooleanData(flags, hadOverflowOffset);
    }

    public final void setHadOverflow(boolean hadOverflow) {
        setBooleanData(flags, hadOverflowOffset, hadOverflow);
    }

    public boolean equalsTo(@NotNull YGLayout layout) //Method definition originates from: YGLayout.cpp
    {
        boolean isEqual = YGFloatArrayEqual(position, layout.position) && YGFloatArrayEqual(dimensions,
                layout.dimensions) && YGFloatArrayEqual(margin, layout.margin) && YGFloatArrayEqual(border,
                layout.border) && YGFloatArrayEqual(padding,
                layout.padding) && direction() == layout.direction() && hadOverflow() == layout.hadOverflow() && lastOwnerDirection == layout.lastOwnerDirection && nextCachedMeasurementsIndex == layout.nextCachedMeasurementsIndex && cachedLayout.equalsTo(
                layout.cachedLayout) && computedFlexBasis == layout.computedFlexBasis;

        for (int i = 0; i < YG_MAX_CACHED_RESULT_COUNT && isEqual; ++i) { //TODO: Verify if this is correct
            isEqual = cachedMeasurements.get(i).equalsTo(layout.cachedMeasurements.get(i));
        }

        if (!GlobalMembers.isUndefined(measuredDimensions.get(0)) || !GlobalMembers.isUndefined(
                layout.measuredDimensions.get(0))) {
            isEqual = isEqual && (Objects.equals(measuredDimensions.get(0), layout.measuredDimensions.get(0)));
        }
        if (!GlobalMembers.isUndefined(measuredDimensions.get(1)) || !GlobalMembers.isUndefined(
                layout.measuredDimensions.get(1))) {
            isEqual = isEqual && (Objects.equals(measuredDimensions.get(1), layout.measuredDimensions.get(1)));
        }

        return isEqual;
    }

    public boolean notEqualsTo(@NotNull YGLayout layout) {
        return !(this.equalsTo(layout));
    }
}
