package io.github.orioncraftmc.meditate.internal.interfaces;

import io.github.orioncraftmc.meditate.internal.YGNode;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public
interface YGCloneNodeFunc {
    @NotNull YGNode invoke(YGNode oldNode, YGNode owner, int childIndex);
}
//ORIGINAL LINE: template <typename T, typename NeedsUpdate, typename Update>
