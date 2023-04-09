package io.github.orioncraftmc.meditate.internal.enums;

public enum YGWrap //Type originates from: YGEnums.h
{
    YGWrapNoWrap,
    YGWrapWrap,
    YGWrapWrapReverse;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGWrap forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
