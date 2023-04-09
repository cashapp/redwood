package io.github.orioncraftmc.meditate.internal.enums;

public enum YGNodeType {
    YGNodeTypeDefault,
    YGNodeTypeText;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGNodeType forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
