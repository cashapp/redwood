package io.github.orioncraftmc.meditate.internal.enums;

public enum YGOverflow {
    YGOverflowVisible,
    YGOverflowHidden,
    YGOverflowScroll;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGOverflow forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
