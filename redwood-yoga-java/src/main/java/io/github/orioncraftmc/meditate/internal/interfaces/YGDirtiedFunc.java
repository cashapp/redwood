package io.github.orioncraftmc.meditate.internal.interfaces;

import io.github.orioncraftmc.meditate.internal.YGNode;

@FunctionalInterface
public
interface YGDirtiedFunc {
    void invoke(YGNode node);
}
