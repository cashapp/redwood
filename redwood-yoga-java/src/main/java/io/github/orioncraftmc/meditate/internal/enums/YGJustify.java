package io.github.orioncraftmc.meditate.internal.enums;

public enum YGJustify {
    YGJustifyFlexStart,
    YGJustifyCenter,
    YGJustifyFlexEnd,
    YGJustifySpaceBetween,
    YGJustifySpaceAround,
    YGJustifySpaceEvenly;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGJustify forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
