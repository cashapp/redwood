package io.github.orioncraftmc.meditate.internal.interfaces;

import io.github.orioncraftmc.meditate.internal.YGNode;

@FunctionalInterface
public
interface YGNodeCleanupFunc {
    void invoke(YGNode node);
}
