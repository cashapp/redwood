package io.github.orioncraftmc.meditate.internal.enums;

public enum YGDirection {
    YGDirectionInherit,
    YGDirectionLTR,
    YGDirectionRTL;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGDirection forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
