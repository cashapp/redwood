package io.github.orioncraftmc.meditate.internal.interfaces;

import io.github.orioncraftmc.meditate.internal.YGConfig;
import io.github.orioncraftmc.meditate.internal.YGNode;
import io.github.orioncraftmc.meditate.internal.enums.YGLogLevel;

@FunctionalInterface
public interface YGLogger {
    int invoke(YGConfig config, YGNode node, YGLogLevel level, String format, Object... args);
}
