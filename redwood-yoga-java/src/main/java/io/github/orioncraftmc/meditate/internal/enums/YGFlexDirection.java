package io.github.orioncraftmc.meditate.internal.enums;

public enum YGFlexDirection {
    YGFlexDirectionColumn,
    YGFlexDirectionColumnReverse,
    YGFlexDirectionRow,
    YGFlexDirectionRowReverse;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGFlexDirection forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
