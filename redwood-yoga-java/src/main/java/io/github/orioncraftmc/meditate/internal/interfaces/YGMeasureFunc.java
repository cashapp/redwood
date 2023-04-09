package io.github.orioncraftmc.meditate.internal.interfaces;

import io.github.orioncraftmc.meditate.internal.YGNode;
import io.github.orioncraftmc.meditate.internal.YGSize;
import io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public
interface YGMeasureFunc {
    @NotNull YGSize invoke(YGNode node, float width, YGMeasureMode widthMode, float height, YGMeasureMode heightMode);
}
