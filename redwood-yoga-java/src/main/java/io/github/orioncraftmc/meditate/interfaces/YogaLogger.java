/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.orioncraftmc.meditate.interfaces;


import io.github.orioncraftmc.meditate.enums.YogaLogLevel;
import io.github.orioncraftmc.meditate.internal.YGConfig;
import io.github.orioncraftmc.meditate.internal.YGNode;
import io.github.orioncraftmc.meditate.internal.enums.YGLogLevel;
import io.github.orioncraftmc.meditate.internal.interfaces.YGLogger;

/**
 * Interface for receiving logs from native layer. Use by setting YogaNode.setLogger(myLogger);
 * See YogaLogLevel for the different log levels.
 */
public interface YogaLogger extends YGLogger {
    void log(YogaLogLevel level, String message);

    @Override
    default int invoke(YGConfig config, YGNode node, YGLogLevel level, String format, Object... args) {
        log(YogaLogLevel.fromInt(level.getValue()), String.format(format, args));
        return 0;
    }
}
