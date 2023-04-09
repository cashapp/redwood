package io.github.orioncraftmc.meditate.internal;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class YGCollectFlexItemsRowValues //Type originates from: Utils.h
{
    public int itemsOnLine;
    public float sizeConsumedOnCurrentLine;
    public float totalFlexGrowFactors;
    public float totalFlexShrinkScaledFactors;
    public int endOfLineIndex;
    public final @NotNull ArrayList<YGNode> relativeChildren = new ArrayList<>();
    public float remainingFreeSpace;
    public float mainDim;
    public float crossDim;
}
