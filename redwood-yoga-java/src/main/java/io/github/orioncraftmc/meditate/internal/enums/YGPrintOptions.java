package io.github.orioncraftmc.meditate.internal.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum YGPrintOptions {
    YGPrintOptionsLayout(1),
    YGPrintOptionsStyle(2),
    YGPrintOptionsChildren(4);

    private final int value;

    YGPrintOptions(int value) {
        this.value = value;
    }

    public static @Nullable YGPrintOptions forValue(int value) {
        for (@NotNull YGPrintOptions options : values()) {
            if (options.value == value) {
                return options;
            }
        }

        return null;
    }

    public int getValue() {
        return value;
    }
}
