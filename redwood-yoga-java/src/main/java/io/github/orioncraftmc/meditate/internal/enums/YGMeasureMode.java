package io.github.orioncraftmc.meditate.internal.enums;

public enum YGMeasureMode {
    YGMeasureModeUndefined,
    YGMeasureModeExactly,
    YGMeasureModeAtMost;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGMeasureMode forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
