package io.github.orioncraftmc.meditate.internal;

import static io.github.orioncraftmc.meditate.internal.GlobalMembers.isUndefined;
import io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode;
import org.jetbrains.annotations.NotNull;

public class YGCachedMeasurement {
    public float availableWidth;
    public float availableHeight;
    public YGMeasureMode widthMeasureMode;
    public YGMeasureMode heightMeasureMode;

    public float computedWidth;
    public float computedHeight;

    public YGCachedMeasurement() {
        this.availableWidth = -1F;
        this.availableHeight = -1F;
        this.widthMeasureMode = YGMeasureMode.YGMeasureModeUndefined;
        this.heightMeasureMode = YGMeasureMode.YGMeasureModeUndefined;
        this.computedWidth = -1F;
        this.computedHeight = -1F;
    }

    public boolean equalsTo(@NotNull YGCachedMeasurement measurement) {
        boolean isEqual = widthMeasureMode == measurement.widthMeasureMode && heightMeasureMode == measurement.heightMeasureMode;

        if (!isUndefined(availableWidth) || !isUndefined(measurement.availableWidth)) {
            isEqual = isEqual && availableWidth == measurement.availableWidth;
        }
        if (!isUndefined(availableHeight) || !isUndefined(measurement.availableHeight)) {
            isEqual = isEqual && availableHeight == measurement.availableHeight;
        }
        if (!isUndefined(computedWidth) || !isUndefined(measurement.computedWidth)) {
            isEqual = isEqual && computedWidth == measurement.computedWidth;
        }
        if (!isUndefined(computedHeight) || !isUndefined(measurement.computedHeight)) {
            isEqual = isEqual && computedHeight == measurement.computedHeight;
        }

        return isEqual;
    }
}
