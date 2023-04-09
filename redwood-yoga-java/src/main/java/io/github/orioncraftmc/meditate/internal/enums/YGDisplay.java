package io.github.orioncraftmc.meditate.internal.enums;

public enum YGDisplay {
    YGDisplayFlex,
    YGDisplayNone;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGDisplay forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
