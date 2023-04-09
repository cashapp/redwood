package io.github.orioncraftmc.meditate.internal.enums;

public enum YGAlign //Type originates from: YGEnums.h
{
    YGAlignAuto,
    YGAlignFlexStart,
    YGAlignCenter,
    YGAlignFlexEnd,
    YGAlignStretch,
    YGAlignBaseline,
    YGAlignSpaceBetween,
    YGAlignSpaceAround;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGAlign forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
